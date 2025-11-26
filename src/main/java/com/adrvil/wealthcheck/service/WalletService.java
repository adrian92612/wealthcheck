package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.BadRequestException;
import com.adrvil.wealthcheck.common.exception.ResourceNotFound;
import com.adrvil.wealthcheck.converter.WalletDtoMapper;
import com.adrvil.wealthcheck.dto.request.WalletReq;
import com.adrvil.wealthcheck.dto.response.WalletRes;
import com.adrvil.wealthcheck.entity.TransactionEntity;
import com.adrvil.wealthcheck.entity.WalletEntity;
import com.adrvil.wealthcheck.enums.CacheName;
import com.adrvil.wealthcheck.enums.TransactionType;
import com.adrvil.wealthcheck.mapper.CategoryMapper;
import com.adrvil.wealthcheck.mapper.TransactionMapper;
import com.adrvil.wealthcheck.mapper.WalletMapper;
import com.adrvil.wealthcheck.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final AccountService accountService;
    private final WalletMapper walletMapper;
    private final CacheUtil cacheUtil;
    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;

    @Transactional
    public WalletRes createWallet(WalletReq walletDtoReq) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Creating wallet for user: {}, name: {}", userId, walletDtoReq.name());
        BigDecimal initialBalance = walletDtoReq.balance();

        if (initialBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Initial balance should be greater than zero");
        }

        WalletEntity wallet = WalletDtoMapper.toEntity(userId, walletDtoReq);
        walletMapper.insert(wallet);

        Long initialBalanceCategoryId = categoryMapper.getCategoryIdByName(userId, "Initial Balance");
        TransactionEntity initialTransaction = TransactionEntity.builder()
                .title(walletDtoReq.name() + " initial balance")
                .notes("(System generated)")
                .amount(initialBalance)
                .userId(userId)
                .toWalletId(wallet.getId())
                .categoryId(initialBalanceCategoryId)
                .type(TransactionType.INCOME)
                .transactionDate(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .softDeleted(false)
                .build();

        transactionMapper.insert(initialTransaction);
        accountService.finishOnboarding(userId);

        cacheUtil.evict(CacheName.USER_WALLETS.getValue(), String.valueOf(userId));
        cacheUtil.evictOverviewCaches(userId);

        log.info("Wallet created successfully - ID: {}, User: {}, Name: {}, Initial Balance: {}",
                wallet.getId(), userId, walletDtoReq.name(), initialBalance);

        return WalletDtoMapper.toDto(wallet);
    }

    public WalletRes getWalletById(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = userId + ":" + id;

        WalletRes cached = cacheUtil.get(CacheName.WALLET.getValue(), cacheKey);
        if (cached != null) {
            return cached;
        }

        log.debug("Fetching wallet - ID: {}, User: {}", id, userId);

        WalletEntity wallet = walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Wallet");
                });

        log.debug("Wallet found - ID: {}, Name: {}, Balance: {}", id, wallet.getName(), wallet.getBalance());

        WalletRes walletRes = WalletDtoMapper.toDto(wallet);

        cacheUtil.put(CacheName.WALLET.getValue(), cacheKey, walletRes);

        return walletRes;
    }

    public List<WalletRes> getAllWallets() {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        List<WalletRes> cached = cacheUtil.get(CacheName.USER_WALLETS.getValue(), String.valueOf(userId));
        if (cached != null) {
            return cached;
        }

        log.debug("Fetching all wallets for user: {}", userId);

        List<WalletEntity> walletEntityList = walletMapper.findWalletListByUserId(userId, false);

        log.info("Returning {} wallets for user: {}", walletEntityList.size(), userId);

        List<WalletRes> walletResList = walletEntityList.stream()
                .map(WalletDtoMapper::toDto)
                .toList();

        cacheUtil.put(CacheName.USER_WALLETS.getValue(), String.valueOf(userId), walletResList);

        return walletResList;
    }

    public List<WalletRes> getAllSoftDeletedWallets() {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        List<WalletRes> cached = cacheUtil.get(CacheName.DELETED_USER_WALLETS.getValue(), String.valueOf(userId));
        if (cached != null) {
            return cached;
        }

        log.debug("Fetching all soft deleted wallets for user: {}", userId);

        List<WalletEntity> walletEntityList = walletMapper.findWalletListByUserId(userId, true);

        log.info("Returning {} soft deleted wallets for user: {}", walletEntityList.size(), userId);

        List<WalletRes> walletResList = walletEntityList.stream()
                .map(WalletDtoMapper::toDto)
                .toList();

        cacheUtil.put(CacheName.DELETED_USER_WALLETS.getValue(), String.valueOf(userId), walletResList);

        return walletResList;
    }

    public WalletRes restoreWallet(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Restoring wallet - ID: {}, User: {}", id, userId);

        Boolean isSoftDeleted = walletMapper.isSoftDeleted(userId, id);
        if (isSoftDeleted == null) {
            log.warn("Wallet not found for restoration - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Category");
        }

        if (!isSoftDeleted) {
            log.warn("Restore rejected - Category is NOT soft deleted. ID: {}, User: {}", id, userId);
            throw new IllegalStateException("Category is not soft deleted");
        }

        if (walletMapper.restoreWallet(id, userId) == 0) {
            log.warn("Wallet restore failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Wallet");
        }

        walletMapper.restoreWallet(id, userId);
        BigDecimal netBalance = transactionMapper.calculateNetBalanceForWallet(id, userId);
        walletMapper.updateBalance(id, userId, netBalance);

        evictAllWalletCaches(userId, id);

        return WalletDtoMapper.toDto(walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Wallet")));
    }

    public WalletRes updateWallet(Long id, WalletReq walletDtoReq) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Updating wallet - ID: {}, User: {}, New name: {}, New balance: {}",
                id, userId, walletDtoReq.name(), walletDtoReq.balance());

        int updated = walletMapper.updateWallet(id, userId, walletDtoReq);
        if (updated == 0) {
            log.warn("Wallet update failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Wallet");
        }

        evictAllWalletCaches(userId, id);

        log.info("Wallet updated successfully - ID: {}, User: {}", id, userId);

        return WalletDtoMapper.toDto(walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Wallet")));
    }

    public WalletRes softDelete(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Soft deleting wallet - ID: {}, User: {}", id, userId);

        WalletEntity wallet = walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for deletion - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Wallet");
                });

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot delete wallet with non-zero balance. Transfer balance first.");
        }

        if (walletMapper.softDelete(id, userId) == 0) {
            log.warn("Wallet soft delete failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Wallet");
        }

        evictAllWalletCaches(userId, id);

        log.info("Wallet soft deleted successfully - ID: {}, User: {}, Name: {}",
                id, userId, wallet.getName());

        return WalletDtoMapper.toDto(wallet);
    }

    public void permanentDeleteWallet(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug(
                "Permanently deleting wallet - ID: {}, User: {}",
                id, userId
        );

        Boolean isSoftDeleted = walletMapper.isSoftDeleted(userId, id);

        if (isSoftDeleted == null) {
            log.warn("Category not found for permanent delete - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Category");
        }

        if (!isSoftDeleted) {
            log.warn("Permanent delete rejected - Category is NOT soft deleted. ID: {}, User: {}", id, userId);
            throw new IllegalStateException("Category is not soft deleted");
        }

        if (walletMapper.permanentDeleteWallet(userId, id) == 0) {
            log.error(
                    "Permanent delete FAILED - No rows affected. Possible concurrent operation. ID: {}, User: {}",
                    id, userId
            );
            throw new ResourceNotFound("Category");
        }

        evictAllWalletCaches(userId, id);

        log.info(
                "Transaction permanently deleted - ID: {}, User: {}",
                id, userId
        );
    }

    //    Helper
    private void evictAllWalletCaches(Long userId, Long walletId) {
        cacheUtil.evictWalletCaches(userId, walletId);
        cacheUtil.evict(CacheName.USER_WALLETS.getValue(), String.valueOf(userId));
        cacheUtil.evict(CacheName.DELETED_USER_WALLETS.getValue(), String.valueOf(userId));
        cacheUtil.evictOverviewCaches(userId);
    }
}

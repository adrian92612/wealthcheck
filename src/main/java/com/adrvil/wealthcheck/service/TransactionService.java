package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.*;
import com.adrvil.wealthcheck.converter.TransactionDtoMapper;
import com.adrvil.wealthcheck.dto.TransactionFilterDto;
import com.adrvil.wealthcheck.dto.request.TransactionReq;
import com.adrvil.wealthcheck.dto.response.TransactionFilterRes;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.entity.CategoryEntity;
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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;
    private final WalletMapper walletMapper;
    private final CategoryMapper categoryMapper;
    private final CacheUtil cacheUtil;

    @Transactional
    public TransactionRes createTransaction(TransactionReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Creating transaction for user: {}, type: {}, amount: {}", userId, req.type(), req.amount());
        validateTransactionReq(req);
        validateCategoryType(userId, req.categoryId(), req.type());

        TransactionEntity transactionEntity = TransactionDtoMapper.toEntity(userId, req);


        WalletEntity fromWallet = fetchWallet(req.fromWalletId(), userId);
        WalletEntity toWallet = fetchWallet(req.toWalletId(), userId);

        log.debug("Processing balance change - Type: {}, From: {}, To: {}, Amount: {}",
                req.type(), req.fromWalletId(), req.toWalletId(), req.amount());

        processBalanceChange(userId, req.type(), fromWallet, toWallet, req.amount());

        transactionMapper.insert(transactionEntity);

        cacheUtil.evictOverviewCaches(userId);


        log.info("Transaction created successfully - ID: {}, User: {}, Type: {}, Amount: {}",
                transactionEntity.getId(), userId, req.type(), req.amount());
        return transactionMapper.findResByIdAndUserId(transactionEntity.getId(), userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));
    }

    @Transactional
    public TransactionRes updateTransaction(Long id, TransactionReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Updating transaction - ID: {}, User: {}, New Type: {}, New Amount: {}",
                id, userId, req.type(), req.amount());

        validateTransactionReq(req);
        validateCategoryType(userId, req.categoryId(), req.type());

        TransactionEntity existing = transactionMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));

        log.debug("Found existing transaction - Type: {}, Amount: {}, From: {}, To: {}",
                existing.getType(), existing.getAmount(), existing.getFromWalletId(), existing.getToWalletId());

        WalletEntity oldFrom = fetchWallet(existing.getFromWalletId(), userId);
        WalletEntity oldTo = fetchWallet(existing.getToWalletId(), userId);

        WalletEntity newFrom = fetchWallet(req.fromWalletId(), userId);
        WalletEntity newTo = fetchWallet(req.toWalletId(), userId);

        log.debug("Reverting old balance - Type: {}, Amount: {}", existing.getType(), existing.getAmount());

        processBalanceRevert(userId, existing.getType(), oldFrom, oldTo, existing.getAmount());

        log.debug("Applying new balance - Type: {}, Amount: {}", req.type(), req.amount());

        processBalanceChange(userId, req.type(), newFrom, newTo, req.amount());

        existing.setFromWalletId(req.fromWalletId());
        existing.setToWalletId(req.toWalletId());
        existing.setCategoryId(req.categoryId());
        existing.setTitle(req.title());
        existing.setNotes(req.notes());
        existing.setAmount(req.amount());
        existing.setType(req.type());

        int updated = transactionMapper.update(existing);
        if (updated == 0) {
            log.warn("Transaction update failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Transaction");
        }

        cacheUtil.evict(CacheName.TRANSACTION.getValue(), userId + ":" + id);
        cacheUtil.evictWalletCaches(userId, id);
        cacheUtil.evictOverviewCaches(userId);


        log.info("Transaction updated successfully - ID: {}, User: {}, Type: {}, Amount: {}",
                id, userId, req.type(), req.amount());
        return transactionMapper.findResByIdAndUserId(existing.getId(), userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));
    }

    public TransactionRes getTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = userId + ":" + id;

        TransactionRes cached = cacheUtil.get(CacheName.TRANSACTION.getValue(), cacheKey);
        if (cached != null) {
            return cached;
        }

        log.debug("Fetching transaction - ID: {}, User: {}", id, userId);

        TransactionRes transaction = transactionMapper.findResByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Transaction");
                });

        cacheUtil.put(CacheName.TRANSACTION.getValue(), cacheKey, transaction);

        log.debug("Transaction found - ID: {}, Type: {}, Amount: {}", id, transaction.type(), transaction.amount());
        return transaction;
    }

    public TransactionFilterRes getAllTransactions(TransactionFilterDto filter) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Fetching all transactions for user: {}, filters: {}", userId, filter);

        long transactionCount = transactionMapper.countTransactions(userId, filter);
        List<TransactionRes> transactionResList = transactionMapper.findTransactions(userId, filter);

        log.info("Returning {} transactions for user: {} with filters: {}",
                transactionCount, userId, filter);

        return TransactionFilterRes.of(transactionResList, filter, transactionCount);
    }

    @Transactional
    public TransactionRes deleteTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Soft deleting transaction - ID: {}, User: {}", id, userId);

        TransactionRes existingRes = transactionMapper.findResByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for deletion - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Transaction");
                });

        log.debug("Found transaction to delete - Type: {}, Amount: {}, From: {}, To: {}",
                existingRes.type(), existingRes.amount(), existingRes.fromWalletId(), existingRes.toWalletId());

        WalletEntity oldFrom = fetchWallet(existingRes.fromWalletId(), userId);
        WalletEntity oldTo = fetchWallet(existingRes.toWalletId(), userId);

        log.debug("Reverting balance for deletion - Type: {}, Amount: {}", existingRes.type(), existingRes.amount());

        processBalanceRevert(userId, existingRes.type(), oldFrom, oldTo, existingRes.amount());

        int deleted = transactionMapper.softDelete(userId, id);
        if (deleted == 0) {
            log.warn("Transaction soft delete failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Transaction");
        }

        cacheUtil.evict(CacheName.TRANSACTION.getValue(), userId + ":" + id);
        cacheUtil.evictWalletCaches(userId, id);
        cacheUtil.evictOverviewCaches(userId);


        log.info("Transaction soft deleted successfully - ID: {}, User: {}, Type: {}, Amount: {}",
                id, userId, existingRes.type(), existingRes.amount());

        return existingRes;
    }


    // --- helper methods ---

    private WalletEntity fetchWallet(Long walletId, Long userId) {
        if (walletId == null || userId == null) return null;
        return walletMapper.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFound("Wallet"));
    }

    private void processBalanceChange(Long userId, TransactionType type, WalletEntity fromWallet,
                                      WalletEntity toWallet, BigDecimal amount) {
        switch (type) {
            case EXPENSE -> decreaseBalance(userId, fromWallet, amount);
            case INCOME -> increaseBalance(userId, toWallet, amount);
            case TRANSFER -> {
                decreaseBalance(userId, fromWallet, amount);
                increaseBalance(userId, toWallet, amount);
            }
            default -> throw new UnsupportedTransactionTypeException();
        }
    }

    private void processBalanceRevert(Long userId, TransactionType type, WalletEntity oldFrom,
                                      WalletEntity oldTo, BigDecimal amount) {
        switch (type) {
            case EXPENSE -> increaseBalance(userId, oldFrom, amount);
            case INCOME -> decreaseBalance(userId, oldTo, amount);
            case TRANSFER -> {
                increaseBalance(userId, oldFrom, amount);
                decreaseBalance(userId, oldTo, amount);
            }
            default -> throw new UnsupportedTransactionTypeException();
        }
    }

    private void decreaseBalance(Long userId, WalletEntity wallet, BigDecimal amount) {
        if (wallet == null || userId == null) throw new IllegalArgumentException("Wallet and userId required for expense/transfer");
        int updated = walletMapper.decreaseBalance(userId, wallet.getId(), amount);
        if (updated == 0) throw new InsufficientBalanceException("Insufficient balance (concurrent-safe)");
    }

    private void increaseBalance(Long userId, WalletEntity wallet, BigDecimal amount) {
        if (wallet == null) throw new IllegalArgumentException("Wallet and userId required for income/transfer");
        int updated = walletMapper.increaseBalance(userId, wallet.getId(), amount);
        if (updated == 0) throw new TransactionProcessingException("Failed to credit wallet");
    }

    private void validateTransactionReq(TransactionReq req) {
        switch (req.type()) {
            case EXPENSE -> {
                if (req.fromWalletId() == null) throw new InvalidTransactionRequestException("Expense requires a fromWalletId");
                if (req.toWalletId() != null) throw new InvalidTransactionRequestException("Expense cannot have a toWalletId");
                if (req.categoryId() == null) throw new InvalidTransactionRequestException("Expense requires a categoryId");
            }
            case INCOME -> {
                if (req.toWalletId() == null) throw new InvalidTransactionRequestException("Income requires a toWalletId");
                if (req.fromWalletId() != null) throw new InvalidTransactionRequestException("Income cannot have a fromWalletId");
                if (req.categoryId() == null) throw new InvalidTransactionRequestException("Income requires a categoryId");
            }
            case TRANSFER -> {
                if (req.fromWalletId() == null || req.toWalletId() == null)
                    throw new InvalidTransactionRequestException("Transfer requires both fromWalletId and toWalletId");
                if (req.categoryId() != null) throw new InvalidTransactionRequestException("Transfer cannot have a categoryId");
            }
            default -> throw new UnsupportedTransactionTypeException();
        }
    }

    private void validateCategoryType(Long userId, Long categoryId, TransactionType transactionType) {
        if (categoryId == null || userId == null) return;
        CategoryEntity category = categoryMapper.getCategoryByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFound("Category"));

        if (!category.getType().name().equals(transactionType.name())) {
            throw new InvalidTransactionRequestException(
                    "Category type " + category.getType() + " does not match transaction type " + transactionType
            );
        }
    }
}

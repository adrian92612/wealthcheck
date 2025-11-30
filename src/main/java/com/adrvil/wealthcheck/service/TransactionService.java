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
import java.util.Objects;

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

        validateTransactionDate(req, fromWallet, toWallet);

        log.debug("Processing balance change - Type: {}, From: {}, To: {}, Amount: {}",
                req.type(), req.fromWalletId(), req.toWalletId(), req.amount());

        processBalanceChange(userId, req.type(), fromWallet, toWallet, req.amount());
        transactionMapper.insert(transactionEntity);

        evictTransactionCaches(userId, transactionEntity.getId(), fromWallet, toWallet);
        log.info("Transaction created successfully - ID: {}, User: {}, Type: {}, Amount: {}",
                transactionEntity.getId(), userId, req.type(), req.amount());
        return transactionMapper.findResByIdAndUserId(transactionEntity.getId(), userId, false)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));
    }

    @Transactional
    public TransactionRes updateTransaction(Long id, TransactionReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Updating transaction - ID: {}, User: {}, New Type: {}, New Amount: {}",
                id, userId, req.type(), req.amount());

        validateTransactionReq(req);
        validateCategoryType(userId, req.categoryId(), req.type());

        TransactionEntity existing = transactionMapper.findByIdAndUserId(id, userId, false)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));

        log.debug("Found existing transaction - Type: {}, Amount: {}, From: {}, To: {}",
                existing.getType(), existing.getAmount(), existing.getFromWalletId(), existing.getToWalletId());

        WalletEntity oldFrom = fetchWallet(existing.getFromWalletId(), userId);
        WalletEntity oldTo = fetchWallet(existing.getToWalletId(), userId);

        WalletEntity newFrom = fetchWallet(req.fromWalletId(), userId);
        WalletEntity newTo = fetchWallet(req.toWalletId(), userId);

        validateTransactionDate(req, newFrom, newTo);

        if (!existing.getType().equals(req.type())) {
            throw new BadRequestException("Transaction type cannot be changed.");
        }

        if (!Objects.equals(existing.getFromWalletId(), req.fromWalletId())
                || !Objects.equals(existing.getToWalletId(), req.toWalletId())) {
            throw new BadRequestException("Cannot change source/destination wallet.");
        }


        log.debug(
                "Updating transaction {} -> Delta Apply | User: {} | OldType: {} | NewType: {} | OldAmount: {} | NewAmount: {} | OldFrom: {} | OldTo: {} | NewFrom: {} | NewTo: {}",
                existing.getId(),
                userId,
                existing.getType(), req.type(),
                existing.getAmount(), req.amount(),
                oldFrom != null ? oldFrom.getId() : null,
                oldTo != null ? oldTo.getId() : null,
                newFrom != null ? newFrom.getId() : null,
                newTo != null ? newTo.getId() : null
        );


        applyDelta(existing.getType(),oldFrom,oldTo,newFrom,newTo,existing.getAmount(),req.amount(),userId);

        existing.setFromWalletId(req.fromWalletId());
        existing.setToWalletId(req.toWalletId());
        existing.setCategoryId(req.categoryId());
        existing.setTitle(req.title());
        existing.setNotes(req.notes());
        existing.setAmount(req.amount());
        existing.setType(req.type());
        existing.setTransactionDate(req.transactionDate());

        int updated = transactionMapper.update(existing);
        if (updated == 0) {
            log.warn("Transaction update failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Transaction");
        }

        evictTransactionCaches(userId, id, newFrom, newTo);


        log.info("Transaction updated successfully - ID: {}, User: {}, Type: {}, Amount: {}",
                id, userId, req.type(), req.amount());
        return getTransaction(id);
    }

    public TransactionRes getTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Fetching transaction - ID: {}, User: {}", id, userId);

        TransactionRes transaction = transactionMapper.findResByIdAndUserId(id, userId, false)
                .orElseThrow(() -> {
                    log.warn("Transaction not found - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Transaction");
                });

        log.debug("Transaction found - ID: {}, Type: {}, Amount: {}", id, transaction.type(), transaction.amount());
        return transaction;
    }

    public TransactionFilterRes getAllTransactions(TransactionFilterDto filter) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        boolean softDeleted = false;
        log.debug("Fetching all transactions for user: {}, filters: {}", userId, filter);
        return getTransactionList(userId, filter, softDeleted);
    }

    public TransactionFilterRes getAllSoftDeletedTransactions(TransactionFilterDto filter) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        boolean softDeleted = true;
        log.debug("Fetching all soft deleted transactions for user: {}, filters: {}", userId, filter);
        return getTransactionList(userId, filter, softDeleted);
    }

    @Transactional
    public TransactionRes deleteTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Soft deleting transaction - ID: {}, User: {}", id, userId);

        TransactionRes existingRes = getTransaction(id);

        Long fromId = existingRes.fromWalletId();
        Long toId = existingRes.toWalletId();

        WalletEntity oldFrom = null;
        WalletEntity oldTo = null;

        if (fromId != null) {
            oldFrom = walletMapper.findByIdAndUserId(fromId, userId).orElse(null);
        }
        if (toId != null) {
            oldTo = walletMapper.findByIdAndUserId(toId, userId).orElse(null);
        }

        log.debug("Reverting balance for deletion - Type: {}, Amount: {}", existingRes.type(), existingRes.amount());

        switch (existingRes.type()) {
            case EXPENSE:
                if (oldFrom != null) increaseBalance(userId, oldFrom, existingRes.amount());
                break;
            case INCOME:
                if (oldTo != null) decreaseBalance(userId, oldTo, existingRes.amount());
                break;
            case TRANSFER:
                if (oldFrom != null) increaseBalance(userId, oldFrom, existingRes.amount());
                if (oldTo != null) decreaseBalance(userId, oldTo, existingRes.amount());
                break;
        }

        if (transactionMapper.softDelete(userId, id) == 0) {
            log.warn("Transaction soft delete failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Transaction");
        }

        evictTransactionCaches(userId, id, oldFrom, oldTo);

        log.info("Transaction soft deleted successfully - ID: {}, User: {}, Type: {}, Amount: {}",
                id, userId, existingRes.type(), existingRes.amount());

        return existingRes;
    }

    @Transactional
    public TransactionRes restoreTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Restoring soft-deleted transaction - ID: {}, User: {}", id, userId);

        TransactionRes existingRes = transactionMapper.findResByIdAndUserId(id, userId, true)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));

        Boolean isSoftDeleted = transactionMapper.isSoftDeleted(userId, id);

        if (isSoftDeleted == null) {
            log.warn("Transaction not found for restoration - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Transaction");
        }
        if (!isSoftDeleted) {
            log.warn("Restore rejected - Transaction is NOT soft deleted. ID: {}, User: {}", id, userId);
            throw new IllegalStateException("Transaction is not soft deleted");
        }

        Long fromId = existingRes.fromWalletId();
        Long toId = existingRes.toWalletId();

        WalletEntity oldFrom = null;
        WalletEntity oldTo = null;

        if (fromId != null) {
            oldFrom = walletMapper.findByIdAndUserId(fromId, userId).orElse(null);
        }
        if (toId != null) {
            oldTo = walletMapper.findByIdAndUserId(toId, userId).orElse(null);
        }

        log.debug("Applying original balance changes for restoration - Type: {}, Amount: {}", existingRes.type(), existingRes.amount());

        switch (existingRes.type()) {
            case EXPENSE:
                if (oldFrom != null) decreaseBalance(userId, oldFrom, existingRes.amount());
                break;
            case INCOME:
                if (oldTo != null) increaseBalance(userId, oldTo, existingRes.amount());
                break;
            case TRANSFER:
                if (oldFrom != null) decreaseBalance(userId, oldFrom, existingRes.amount());
                if (oldTo != null) increaseBalance(userId, oldTo, existingRes.amount());
                break;
        }

        if (transactionMapper.restoreTransaction(userId, id) == 0) {
            throw new ResourceNotFound("Transaction");
        }

        evictTransactionCaches(userId, id, oldFrom, oldTo);
        log.info("Transaction restored successfully - ID: {}, User: {}, Type: {}, Amount: {}",
                id, userId, existingRes.type(), existingRes.amount());
        return existingRes;
    }

    @Transactional
    public void permanentDeleteTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Permanently deleting transaction - ID: {}, User: {}", id, userId);

        Boolean isSoftDeleted = transactionMapper.isSoftDeleted(userId, id);

        if (isSoftDeleted == null) {
            log.warn("Transaction not found for permanent delete - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Transaction");
        }
        if (!isSoftDeleted) {
            log.warn("Permanent delete rejected - Transaction is NOT soft deleted. ID: {}, User: {}", id, userId);
            throw new IllegalStateException("Transaction is not soft deleted");
        }

        if (transactionMapper.permanentDeleteTransaction(userId, id) == 0) {
            log.error("Permanent delete FAILED - No rows affected. Possible concurrent operation. ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Transaction");
        }

        evictTransactionCaches(userId, id, null, null);
        log.info("Transaction permanently deleted - ID: {}, User: {}", id, userId);
    }


    // --- helper methods ---

    private void evictTransactionCaches(Long userId, Long transactionId, WalletEntity fromWallet, WalletEntity toWallet) {
        cacheUtil.evict(CacheName.TRANSACTION.getValue(), userId + ":" + transactionId);
        cacheUtil.evictOverviewCaches(userId);

        if (fromWallet != null) cacheUtil.evictWalletCaches(userId, fromWallet.getId());
        if (toWallet != null) cacheUtil.evictWalletCaches(userId, toWallet.getId());
    }

    private TransactionFilterRes getTransactionList(Long userId,
                                                    TransactionFilterDto filter,
                                                    boolean softDeleted) {

        long transactionCount = transactionMapper.countTransactions(userId, filter, softDeleted);
        List<TransactionRes> transactionResList = transactionMapper.findTransactions(userId, filter, softDeleted);

        log.info("Returning {} transactions for user: {} with filters: {}",
                transactionCount, userId, filter);

        return TransactionFilterRes.of(transactionResList, filter, transactionCount);
    }

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

    private void validateTransactionDate(TransactionReq req, WalletEntity fromWallet, WalletEntity toWallet) {
        if (req.transactionDate() == null) {
            throw new InvalidTransactionRequestException("Transaction must have a date");
        }

        if (fromWallet != null) {
            if (req.transactionDate().isBefore(fromWallet.getCreatedAt())) {
                throw new InvalidTransactionRequestException(
                        "Transaction date is earlier than the source wallet's creation date"
                );
            }
        }

        if (toWallet != null) {
            if (req.transactionDate().isBefore(toWallet.getCreatedAt())) {
                throw new InvalidTransactionRequestException(
                        "Transaction date is earlier than the destination wallet's creation date"
                );
            }
        }
    }

    private BigDecimal computeDelta(TransactionType type, BigDecimal oldAmount, BigDecimal newAmount) {
        return switch (type) {
            case INCOME -> newAmount.subtract(oldAmount);
            case EXPENSE -> oldAmount.subtract(newAmount);
            case TRANSFER -> null;
        };
    }

    private void applyTransferDelta(Long userId,
                                    WalletEntity from,
                                    WalletEntity to,
                                    BigDecimal oldAmount,
                                    BigDecimal newAmount) {

        BigDecimal fromDelta = oldAmount.subtract(newAmount);
        BigDecimal toDelta = newAmount.subtract(oldAmount);

        if (fromDelta.compareTo(BigDecimal.ZERO) > 0)
            increaseBalance(userId, from, fromDelta);
        else if (fromDelta.compareTo(BigDecimal.ZERO) < 0)
            decreaseBalance(userId, from, fromDelta.abs());

        if (toDelta.compareTo(BigDecimal.ZERO) > 0)
            increaseBalance(userId, to, toDelta);
        else if (toDelta.compareTo(BigDecimal.ZERO) < 0)
            decreaseBalance(userId, to, toDelta.abs());
    }

    private void applyDelta(TransactionType type,
                            WalletEntity oldFrom,
                            WalletEntity oldTo,
                            WalletEntity newFrom,
                            WalletEntity newTo,
                            BigDecimal oldAmount,
                            BigDecimal newAmount,
                            Long userId) {


        if (type == TransactionType.TRANSFER) {
            applyTransferDelta(userId, oldFrom, oldTo, oldAmount, newAmount);
            return;
        }

        BigDecimal delta = computeDelta(type, oldAmount, newAmount);
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) return;

        switch (type) {
            case INCOME -> {
                WalletEntity wallet = (newTo != null) ? newTo : oldTo;

                if (delta.compareTo(BigDecimal.ZERO) > 0)
                    increaseBalance(userId, wallet, delta);
                else
                    decreaseBalance(userId, wallet, delta.abs());
            }

            case EXPENSE -> {
                WalletEntity wallet = (newFrom != null) ? newFrom : oldFrom;

                if (delta.compareTo(BigDecimal.ZERO) > 0)
                    increaseBalance(userId, wallet, delta);
                else
                    decreaseBalance(userId, wallet, delta.abs());
            }
        }
    }

}

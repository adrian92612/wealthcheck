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
import com.adrvil.wealthcheck.enums.TransactionType;
import com.adrvil.wealthcheck.mapper.CategoryMapper;
import com.adrvil.wealthcheck.mapper.TransactionMapper;
import com.adrvil.wealthcheck.mapper.WalletMapper;
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

    @Transactional
    public TransactionRes createTransaction(TransactionReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        validateTransactionReq(req);
        validateCategoryType(userId, req.categoryId(), req.type());

        TransactionEntity transactionEntity = TransactionDtoMapper.toEntity(userId, req);


        WalletEntity fromWallet = fetchWallet(req.fromWalletId(), userId);
        WalletEntity toWallet = fetchWallet(req.toWalletId(), userId);

        processBalanceChange(userId, req.type(), fromWallet, toWallet, req.amount());

        transactionMapper.insert(transactionEntity);
        return transactionMapper.findResByIdAndUserId(transactionEntity.getId(), userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));
    }

    @Transactional
    public TransactionRes updateTransaction(Long id, TransactionReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        validateTransactionReq(req);
        validateCategoryType(userId, req.categoryId(), req.type());

        TransactionEntity existing = transactionMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));

        WalletEntity oldFrom = fetchWallet(existing.getFromWalletId(), userId);
        WalletEntity oldTo = fetchWallet(existing.getToWalletId(), userId);

        WalletEntity newFrom = fetchWallet(req.fromWalletId(), userId);
        WalletEntity newTo = fetchWallet(req.toWalletId(), userId);

        processBalanceRevert(userId, existing.getType(), oldFrom, oldTo, existing.getAmount());

        processBalanceChange(userId, req.type(), newFrom, newTo, req.amount());

        existing.setFromWalletId(req.fromWalletId());
        existing.setToWalletId(req.toWalletId());
        existing.setCategoryId(req.categoryId());
        existing.setTitle(req.title());
        existing.setNotes(req.notes());
        existing.setAmount(req.amount());
        existing.setType(req.type());

        int updated = transactionMapper.update(existing);
        if (updated == 0) throw new ResourceNotFound("Transaction");

        return transactionMapper.findResByIdAndUserId(existing.getId(), userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));
    }

    public TransactionRes getTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        return transactionMapper.findResByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));
    }

    public TransactionFilterRes getAllTransactions(TransactionFilterDto filter) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        long transactionCount = transactionMapper.countTransactions(userId, filter);
        List<TransactionRes> transactionResList = transactionMapper.findTransactions(userId, filter);

        return TransactionFilterRes.of(transactionResList, filter, transactionCount);
    }

    @Transactional
    public TransactionRes deleteTransaction(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        TransactionRes existingRes = transactionMapper.findResByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Transaction"));

        WalletEntity oldFrom = fetchWallet(existingRes.fromWalletId(), userId);
        WalletEntity oldTo = fetchWallet(existingRes.toWalletId(), userId);

        processBalanceRevert(userId, existingRes.type(), oldFrom, oldTo, existingRes.amount());

        int deleted = transactionMapper.softDelete(userId, id);
        if (deleted == 0) throw new ResourceNotFound("Transaction");

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

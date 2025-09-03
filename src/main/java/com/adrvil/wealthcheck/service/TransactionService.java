package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.TransactionCreationException;
import com.adrvil.wealthcheck.converter.TransactionDtoMapper;
import com.adrvil.wealthcheck.dto.request.TransactionReq;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.entity.TransactionEntity;
import com.adrvil.wealthcheck.entity.WalletEntity;
import com.adrvil.wealthcheck.enums.TransactionType;
import com.adrvil.wealthcheck.mapper.TransactionMapper;
import com.adrvil.wealthcheck.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.dao.DataAccessException;
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

    @Transactional
    public TransactionRes createTransaction(TransactionReq req) throws NotFoundException {
        validateTransactionReq(req);
        Long userId = accountService.getCurrentAccountIdOrThrow();
        TransactionEntity transactionEntity = TransactionDtoMapper.toEntity(userId, req);

        WalletEntity fromWallet = req.fromWalletId() != null
                ? walletMapper.findByIdAndUserId(req.fromWalletId(), userId)
                .orElseThrow(() -> new TransactionCreationException("Wallet not found"))
                : null;
        WalletEntity toWallet = req.toWalletId() != null
                ? walletMapper.findByIdAndUserId(req.toWalletId(), userId)
                .orElseThrow(() -> new TransactionCreationException("Wallet not found"))
                : null;

        processBalanceChange(userId, req.type(), fromWallet, toWallet, req.amount());

        try {
            transactionMapper.insert(transactionEntity);
        } catch (PersistenceException | DataAccessException e) {
            throw new TransactionCreationException("Unable to create transaction", e);
        }

        return transactionMapper.findResByIdAndUserId(transactionEntity.getId(), userId)
                .orElseThrow(() -> new TransactionCreationException("Transaction not found"));
    }

    @Transactional
    public TransactionRes updateTransaction(Long txId, TransactionReq req) throws NotFoundException {
        validateTransactionReq(req);
        Long userId = accountService.getCurrentAccountIdOrThrow();

        TransactionEntity existing = transactionMapper.findByIdAndUserId(txId, userId)
                .orElseThrow(() -> new TransactionCreationException("Transaction not found"));

        WalletEntity oldFrom = fetchWallet(existing.getFromWalletId(), userId, "old fromWallet");
        WalletEntity oldTo = fetchWallet(existing.getToWalletId(), userId, "old toWallet");

        WalletEntity newFrom = fetchWallet(req.fromWalletId(), userId, "new fromWallet");
        WalletEntity newTo = fetchWallet(req.toWalletId(), userId, "new toWallet");

        processBalanceRevert(userId, existing.getType(), oldFrom, oldTo, existing.getAmount());

        processBalanceChange(userId, req.type(), newFrom, newTo, req.amount());

        existing.setFromWalletId(req.fromWalletId());
        existing.setToWalletId(req.toWalletId());
        existing.setCategoryId(req.categoryId());
        existing.setTitle(req.title());
        existing.setNotes(req.notes());
        existing.setAmount(req.amount());
        existing.setType(req.type());

        try {
            int updated = transactionMapper.update(existing);
            if (updated == 0) throw new TransactionCreationException("Unable to update transaction");
        } catch (PersistenceException | DataAccessException e) {
            throw new TransactionCreationException("Unable to update transaction", e);
        }

        return transactionMapper.findResByIdAndUserId(existing.getId(), userId)
                .orElseThrow(() -> new TransactionCreationException("Transaction not found"));
    }

    public TransactionRes getTransaction(Long id) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        return transactionMapper.findResByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionCreationException("Transaction not found"));
    }

    public List<TransactionRes> getAllTransactions() throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        return transactionMapper.findAllResByUserId(userId);
    }

    @Transactional
    public TransactionRes deleteTransaction(Long id) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        TransactionRes existingRes = transactionMapper.findResByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionCreationException("Transaction not found"));

        WalletEntity oldFrom = fetchWallet(existingRes.fromWalletId(), userId, "old fromWallet");
        WalletEntity oldTo = fetchWallet(existingRes.toWalletId(), userId, "old toWallet");

        processBalanceRevert(userId, existingRes.type(), oldFrom, oldTo, existingRes.amount());

        int deleted = transactionMapper.softDelete(userId, id);
        if (deleted == 0) throw new TransactionCreationException("Unable to delete transaction");

        return existingRes;
    }


    // --- helper methods ---

    private WalletEntity fetchWallet(Long walletId, Long userId, String name) {
        if (walletId == null) return null;
        return walletMapper.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new TransactionCreationException(name + " not found"));
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
            default -> throw new TransactionCreationException("Unsupported transaction type");
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
            default -> throw new TransactionCreationException("Unsupported transaction type");
        }
    }

    private void decreaseBalance(Long userId, WalletEntity wallet, BigDecimal amount) {
        if (wallet == null) throw new TransactionCreationException("Wallet required for expense/transfer");
        int updated = walletMapper.decreaseBalance(userId, wallet.getId(), amount);
        if (updated == 0) throw new TransactionCreationException("Insufficient balance (concurrent-safe)");
    }

    private void increaseBalance(Long userId, WalletEntity wallet, BigDecimal amount) {
        if (wallet == null) throw new TransactionCreationException("Wallet required for income/transfer");
        int updated = walletMapper.increaseBalance(userId, wallet.getId(), amount);
        if (updated == 0) throw new TransactionCreationException("Failed to credit wallet");
    }

    private void validateTransactionReq(TransactionReq req) {
        switch (req.type()) {
            case EXPENSE -> {
                if (req.fromWalletId() == null) throw new TransactionCreationException("Expense requires a fromWalletId");
                if (req.toWalletId() != null) throw new TransactionCreationException("Expense cannot have a toWalletId");
                if (req.categoryId() == null) throw new TransactionCreationException("Expense requires a categoryId");
            }
            case INCOME -> {
                if (req.toWalletId() == null) throw new TransactionCreationException("Income requires a toWalletId");
                if (req.fromWalletId() != null) throw new TransactionCreationException("Income cannot have a fromWalletId");
                if (req.categoryId() == null) throw new TransactionCreationException("Income requires a categoryId");
            }
            case TRANSFER -> {
                if (req.fromWalletId() == null || req.toWalletId() == null)
                    throw new TransactionCreationException("Transfer requires both fromWalletId and toWalletId");
                if (req.categoryId() != null) throw new TransactionCreationException("Transfer cannot have a categoryId");
            }
            default -> throw new TransactionCreationException("Unsupported transaction type");
        }
    }
}

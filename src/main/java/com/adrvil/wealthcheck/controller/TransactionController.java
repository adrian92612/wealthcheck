package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.common.exception.TransactionCreationException;
import com.adrvil.wealthcheck.dto.request.TransactionReq;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping()
    public ApiResponseEntity<List<TransactionRes>> getAllTransactions() {
        try {
            List<TransactionRes> transactionResList = transactionService.getAllTransactions();
            return ApiResponseEntity.success(HttpStatus.OK, "Transaction List", transactionResList);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (TransactionCreationException e) {
            return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<TransactionRes> getTransactionById(@PathVariable Long id) {
        try {
            TransactionRes transactionRes = transactionService.getTransaction(id);
            return ApiResponseEntity.success(HttpStatus.OK, "Transaction found", transactionRes);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (TransactionCreationException e) {
            return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    @PostMapping()
    public ApiResponseEntity<TransactionRes> createTransaction(@Valid @RequestBody TransactionReq req) {
        try {
            TransactionRes transactionRes = transactionService.createTransaction(req);
            return ApiResponseEntity.success(HttpStatus.CREATED, "Transaction created", transactionRes);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (TransactionCreationException e) {
            return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    @PutMapping("/{id}")
    public ApiResponseEntity<TransactionRes> updateTransaction(@Valid @RequestBody TransactionReq req,
                                                               @PathVariable Long id) {
        try {
            TransactionRes updatedTransaction = transactionService.updateTransaction(id, req);
            return ApiResponseEntity.success(HttpStatus.OK, "Transaction created", updatedTransaction);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (TransactionCreationException e) {
            return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponseEntity<TransactionRes> deleteTransaction(@PathVariable Long id) {
        try {
            TransactionRes deletedTransaction = transactionService.deleteTransaction(id);
            return ApiResponseEntity.success(HttpStatus.OK, "Transaction deleted", deletedTransaction);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (TransactionCreationException e) {
            return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }
}

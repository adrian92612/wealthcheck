package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.dto.TransactionFilterDto;
import com.adrvil.wealthcheck.dto.request.TransactionReq;
import com.adrvil.wealthcheck.dto.response.TransactionFilterRes;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ApiResponseEntity<TransactionFilterRes> getAllTransactions(TransactionFilterDto filter) {
        return ApiResponseEntity.success(HttpStatus.OK, "Transaction List", transactionService.getAllTransactions(filter));
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<TransactionRes> getTransactionById(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Transaction found", transactionService.getTransaction(id));
    }

    @PostMapping
    public ApiResponseEntity<TransactionRes> createTransaction(@Valid @RequestBody TransactionReq req) {
        return ApiResponseEntity.success(HttpStatus.CREATED, "Transaction created", transactionService.createTransaction(req));
    }

    @PutMapping("/{id}")
    public ApiResponseEntity<TransactionRes> updateTransaction(@Valid @RequestBody TransactionReq req,
                                                               @PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Transaction updated", transactionService.updateTransaction(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponseEntity<TransactionRes> deleteTransaction(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Transaction deleted", transactionService.deleteTransaction(id));
    }
}

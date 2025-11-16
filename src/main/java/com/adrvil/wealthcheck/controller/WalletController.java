package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.dto.request.WalletReq;
import com.adrvil.wealthcheck.dto.response.WalletRes;
import com.adrvil.wealthcheck.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;

    @GetMapping()
    public ApiResponseEntity<List<WalletRes>> getAllWalletsFromUser() {
        return ApiResponseEntity.success(HttpStatus.OK, "Wallet list found", walletService.getAllWallets());
    }

    @PostMapping()
    public ApiResponseEntity<WalletRes> createWallet(@Valid @RequestBody WalletReq walletReq) {
        return ApiResponseEntity.success(HttpStatus.CREATED, "Wallet created", walletService.createWallet(walletReq));
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<WalletRes> getWalletById(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Wallet found", walletService.getWalletById(id));
    }

    @PutMapping("/{id}")
    public ApiResponseEntity<WalletRes> updateWallet(@PathVariable Long id,
                                                     @Valid @RequestBody WalletReq walletReq) {
        return ApiResponseEntity.success(HttpStatus.OK, "Wallet updated", walletService.updateWallet(id, walletReq));
    }

    @DeleteMapping("/{id}")
    public ApiResponseEntity<WalletRes> deleteWallet(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Wallet deleted", walletService.softDelete(id));
    }

    @GetMapping("/deleted")
    public ApiResponseEntity<List<WalletRes>> getAllSoftDeletedWallets() {
        return ApiResponseEntity.success(HttpStatus.OK, "Deleted Wallet list", walletService.getAllSoftDeletedWallets());
    }

    @PutMapping("/restore/{id}")
    public ApiResponseEntity<WalletRes> restoreWallet(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Wallet restored", walletService.restoreWallet(id));
    }

    @DeleteMapping("/permanent-delete/{id}")
    public ApiResponseEntity<Void> deletePermanentCategory(@PathVariable Long id) {
        walletService.permanentDeleteWallet(id);
        return ApiResponseEntity.success(HttpStatus.OK, "Wallet permanently deleted", null);
    }
}

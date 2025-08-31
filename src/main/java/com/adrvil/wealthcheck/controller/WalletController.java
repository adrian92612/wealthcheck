package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.common.exception.WalletCreationException;
import com.adrvil.wealthcheck.dto.request.WalletReq;
import com.adrvil.wealthcheck.dto.response.WalletRes;
import com.adrvil.wealthcheck.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/all")
    public ApiResponseEntity<List<WalletRes>> getAllWalletsFromUser() {
        try {
            List<WalletRes> walletResList = walletService.getAllWallets();
            return ApiResponseEntity.success(HttpStatus.OK, "Wallet list found", walletResList);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        }
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<WalletRes> getWalletById(@PathVariable Long id) {
        try {
            WalletRes walletRes = walletService.getWalletById(id);
            return ApiResponseEntity.success(HttpStatus.OK, "Wallet found", walletRes);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        }
    }

    @PutMapping("/{id}")
    public ApiResponseEntity<WalletRes> updateWallet(@PathVariable Long id,
                                                     @Valid @RequestBody WalletReq walletReq) {
        try {
            WalletRes walletRes = walletService.updateWallet(id, walletReq);
            return ApiResponseEntity.success(HttpStatus.OK, "Wallet updated", walletRes);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        }
    }

    @PostMapping()
    public ApiResponseEntity<WalletRes> createWallet(
            @Valid @RequestBody WalletReq walletReq
    ) {
        try {
            WalletRes wallet = walletService.createWallet(walletReq);
            return ApiResponseEntity.success(HttpStatus.CREATED, "Wallet created", wallet);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (WalletCreationException e) {
            return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }
}

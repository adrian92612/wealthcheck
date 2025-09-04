package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.ResourceNotFound;
import com.adrvil.wealthcheck.converter.WalletDtoMapper;
import com.adrvil.wealthcheck.dto.request.WalletReq;
import com.adrvil.wealthcheck.dto.response.WalletRes;
import com.adrvil.wealthcheck.entity.WalletEntity;
import com.adrvil.wealthcheck.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final AccountService accountService;
    private final WalletMapper walletMapper;

    public WalletRes createWallet(WalletReq walletDtoReq) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        WalletEntity wallet = WalletDtoMapper.toEntity(userId, walletDtoReq);
        walletMapper.insert(wallet);
        return WalletDtoMapper.toDto(wallet);
    }

    public WalletRes getWalletById(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        return WalletDtoMapper.toDto(walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Wallet")));
    }

    public List<WalletRes> getAllWallets() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        List<WalletEntity> walletEntityList = walletMapper.findByUserId(userId);
        log.debug("WALLET LIST: {}", walletEntityList);

        return walletEntityList.stream()
                .map(WalletDtoMapper::toDto)
                .toList();
    }

    public WalletRes updateWallet(Long id, WalletReq walletDtoReq) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        int i = walletMapper.updateWallet(id, userId, walletDtoReq);
        if (i == 0) throw new ResourceNotFound("Wallet");
        return WalletDtoMapper.toDto(walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Wallet")));
    }

    public WalletRes softDelete(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        WalletEntity wallet = walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Wallet"));
        int deleted = walletMapper.softDelete(id, userId);
        if (deleted == 0) throw new ResourceNotFound("Wallet");
        return WalletDtoMapper.toDto(wallet);
    }
}

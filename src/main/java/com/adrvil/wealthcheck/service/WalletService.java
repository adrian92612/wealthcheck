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

        log.debug("Creating wallet for user: {}, name: {}", userId, walletDtoReq.name());

        WalletEntity wallet = WalletDtoMapper.toEntity(userId, walletDtoReq);
        walletMapper.insert(wallet);

        log.info("Wallet created successfully - ID: {}, User: {}, Name: {}",
                wallet.getId(), userId, walletDtoReq.name());

        return WalletDtoMapper.toDto(wallet);
    }

    public WalletRes getWalletById(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Fetching wallet - ID: {}, User: {}", id, userId);

        WalletEntity wallet = walletMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Wallet");
                });

        log.debug("Wallet found - ID: {}, Name: {}, Balance: {}", id, wallet.getName(), wallet.getBalance());
        return WalletDtoMapper.toDto(wallet);
    }

    public List<WalletRes> getAllWallets() {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Fetching all wallets for user: {}", userId);

        List<WalletEntity> walletEntityList = walletMapper.findByUserId(userId);

        log.info("Returning {} wallets for user: {}", walletEntityList.size(), userId);

        return walletEntityList.stream()
                .map(WalletDtoMapper::toDto)
                .toList();
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

        int deleted = walletMapper.softDelete(id, userId);
        if (deleted == 0) {
            log.warn("Wallet soft delete failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Wallet");
        }

        log.info("Wallet soft deleted successfully - ID: {}, User: {}, Name: {}",
                id, userId, wallet.getName());

        return WalletDtoMapper.toDto(wallet);
    }
}

package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.WalletCreationException;
import com.adrvil.wealthcheck.converter.WalletDtoMapper;
import com.adrvil.wealthcheck.dto.request.WalletReq;
import com.adrvil.wealthcheck.dto.response.WalletRes;
import com.adrvil.wealthcheck.entity.AccountEntity;
import com.adrvil.wealthcheck.entity.WalletEntity;
import com.adrvil.wealthcheck.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final AccountService accountService;
    private final WalletMapper walletMapper;

    public WalletRes createWallet(WalletReq walletDtoReq) throws NotFoundException {
        AccountEntity account = accountService.getAccountByUserId(walletDtoReq.userId());
        if (account == null) {
            throw new NotFoundException("Account not found");
        }
        WalletEntity wallet = WalletDtoMapper.toEntity(walletDtoReq);
        try {
            walletMapper.insert(wallet);
        } catch (PersistenceException | DataAccessException e) {
            throw new WalletCreationException("Database insert failed", e);
        }
        return WalletDtoMapper.toDto(wallet);
    }

    public WalletRes getWalletById(Long id) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        return WalletDtoMapper.toDto(walletMapper.findByIdAndUserId(id, userId));
    }

    public List<WalletRes> getAllWallets() throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        List<WalletEntity> walletEntityList = walletMapper.findByUserId(userId);
        log.debug("WALLET LIST: {}", walletEntityList);

        return walletEntityList.stream()
                .map(WalletDtoMapper::toDto)
                .toList();
    }

    public WalletRes updateWallet(Long id, WalletReq walletDtoReq) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        int i = walletMapper.updateWallet(id,userId,walletDtoReq);
        if (i == 1) {
            return WalletDtoMapper.toDto(walletMapper.findByIdAndUserId(id, userId));
        }
        throw new NotFoundException("Unable to update wallet");
    }
}

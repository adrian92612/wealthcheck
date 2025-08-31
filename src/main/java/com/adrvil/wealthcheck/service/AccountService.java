package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.dto.GoogleUserDto;
import com.adrvil.wealthcheck.entity.AccountEntity;
import com.adrvil.wealthcheck.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;

    public AccountEntity createAccount(GoogleUserDto googleUserDto) {
        AccountEntity account = AccountEntity.builder()
                .name(googleUserDto.name())
                .email(googleUserDto.email())
                .avatarUrl(googleUserDto.picture())
                .isActive(true)
                .createdAt(new Date().toInstant())
                .updatedAt(new Date().toInstant())
                .build();

        accountMapper.insert(account);
        return account;
    }


}

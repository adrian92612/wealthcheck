package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.dto.GoogleUserDto;
import com.adrvil.wealthcheck.entity.AccountEntity;
import com.adrvil.wealthcheck.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public Long getCurrentAccountIdOrThrow() throws NotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new NotFoundException("No authenticated user found");
        }
        Long userId = accountMapper.getUserIdByEmail(auth.getName());
        if (userId == null) {
            throw new NotFoundException("Account not found");
        }
        return userId;
    }

    public AccountEntity getAccountByUserId(Long userId) {
        return accountMapper.findByUserId(userId);
    }

}

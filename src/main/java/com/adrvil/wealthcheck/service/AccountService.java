package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.AccountNotAuthenticatedException;
import com.adrvil.wealthcheck.common.exception.ResourceNotFound;
import com.adrvil.wealthcheck.dto.GoogleUserDto;
import com.adrvil.wealthcheck.entity.AccountEntity;
import com.adrvil.wealthcheck.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.debug("Creating account for Google user - Email: {}, Name: {}, Provider ID: {}",
                googleUserDto.email(), googleUserDto.name(), googleUserDto.id());

        AccountEntity account = AccountEntity.builder()
                .name(googleUserDto.name())
                .email(googleUserDto.email())
                .provider("google")
                .providerId(googleUserDto.id())
                .avatarUrl(googleUserDto.picture())
                .createdAt(new Date().toInstant())
                .updatedAt(new Date().toInstant())
                .build();

        accountMapper.insert(account);

        log.info("Account created successfully - ID: {}, Email: {}, Name: {}",
                account.getId(), account.getEmail(), account.getName());

        return account;
    }

    public Long getCurrentAccountIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            log.warn("Authentication is null - no security context found");
            throw new AccountNotAuthenticatedException("Account not authenticated");
        }

        if (auth.getPrincipal() == null) {
            log.warn("Authentication principal is null - Name: {}", auth.getName());
            throw new AccountNotAuthenticatedException("Account not authenticated");
        }

        String email = auth.getName();
        log.debug("Looking up user ID for authenticated email: {}", email);

        Long userId = accountMapper.getUserIdByEmail(email);

        if (userId == null) {
            log.warn("User ID not found for email: {}", email);
            throw new ResourceNotFound("Account");
        }

        log.debug("Found user ID: {} for email: {}", userId, email);
        return userId;
    }

    public void finishOnboarding(Long userId) {
        AccountEntity account = accountMapper.findById(userId);

        if (account == null) {
            log.warn("User with id not found: {}", userId);
            throw new ResourceNotFound("Account");
        }

        if (!account.isNewUser()) return;

        accountMapper.setIsNewUser(userId, false);
    }

}

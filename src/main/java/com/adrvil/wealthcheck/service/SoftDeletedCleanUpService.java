package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.mapper.SoftDeletedCleanUpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoftDeletedCleanUpService {
    private final SoftDeletedCleanUpMapper mapper;

    @Scheduled(cron = "0 0 3 * * ?", zone = "Asia/Manila")
    public void cleanSoftDeleted() {
        int deletedTxns = mapper.removeTransactions();
        int deletedWallets = mapper.removeWallets();
        int deletedCategories = mapper.removeCategories();
        log.info("Soft delete cleanup complete. Deleted {} old records.", deletedTxns);
        log.info("Soft delete cleanup complete. Deleted {} old records.", deletedWallets);
        log.info("Soft delete cleanup complete. Deleted {} old records.", deletedCategories);
    }
}

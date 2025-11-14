package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.mapper.SoftDeletedCleanUpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoftDeletedCleanUpService {
    private final SoftDeletedCleanUpMapper mapper;

    @Scheduled(cron = "0 0 3 * * ?", zone = "Asia/Manila")
    public void cleanSoftDeleted() {
        CompletableFuture.runAsync(() -> {
            try {
                int deletedTxns = mapper.removeTransactions();
                log.info("Soft delete cleanup complete. Deleted {} old transaction records.", deletedTxns);
            } catch (Exception e) {
                log.error("Failed to delete old transactions");
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                int deletedWallets = mapper.removeWallets();
                log.info("Soft delete cleanup complete. Deleted {} old wallet records.", deletedWallets);
            } catch (Exception e) {
                log.error("Failed to delete old wallets");
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                int deletedCategories = mapper.removeCategories();
                log.info("Soft delete cleanup complete. Deleted {} old category records.", deletedCategories);
            } catch (Exception e) {
                log.error("Failed to delete old categories");
            }
        });


    }
}

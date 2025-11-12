package com.adrvil.wealthcheck.mapper;

import org.apache.ibatis.annotations.Delete;

public interface SoftDeletedCleanUpMapper {

    @Delete("""
            DELETE FROM transactions
            WHERE soft_deleted = true
                AND updated_at < (NOW() AT TIME ZONE 'Asia/Manila' - INTERVAL '7 days')
            """)
    int removeTransactions();

    @Delete("""
            DELETE FROM wallet
            WHERE soft_deleted = true
                AND updated_at < (NOW() AT TIME ZONE 'Asia/Manila' - INTERVAL '7 days')
            """)
    int removeWallets();

    @Delete("""
            DELETE FROM category
            WHERE soft_deleted = true
                AND updated_at < (NOW() AT TIME ZONE 'Asia/Manila' - INTERVAL '7 days')
            """)
    int removeCategories();
}

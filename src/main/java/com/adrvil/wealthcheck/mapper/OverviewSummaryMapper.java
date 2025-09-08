package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.TopTransactionsDto;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface OverviewSummaryMapper {
    @Select("SELECT COALESCE(SUM(balance), 0) FROM wallet")
    BigDecimal getTotalBalance();

    @Select("""
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE type = 'INCOME'
                  AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE)
            """)
    BigDecimal getIncomeThisMonth();

    @Select("""
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE type = 'EXPENSE'
                  AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE)
            """)
    BigDecimal getExpenseThisMonth();

    @Select("""
                SELECT t.type AS type,
                       c.name AS name,
                       t.amount,
                       t.created_at
                FROM transactions t
                JOIN category c ON c.id = t.category_id
                WHERE t.type = 'INCOME'
                  AND DATE_TRUNC('month', t.created_at) = DATE_TRUNC('month', CURRENT_DATE)
                ORDER BY t.amount DESC
                LIMIT 3
            """)
    List<TopTransactionsDto> getTopIncomeTransactions();

    @Select("""
                SELECT t.type AS type,
                       c.name AS name,
                       t.amount,
                       t.created_at
                FROM transactions t
                JOIN category c ON c.id = t.category_id
                WHERE t.type = 'EXPENSE'
                  AND DATE_TRUNC('month', t.created_at) = DATE_TRUNC('month', CURRENT_DATE)
                ORDER BY t.amount DESC
                LIMIT 3
            """)
    List<TopTransactionsDto> getTopExpenseTransactions();

    @Select("""
                SELECT type AS type, title AS name, amount
                FROM transactions
                ORDER BY created_at DESC
                LIMIT 10
            """)
    List<TopTransactionsDto> getRecentTransactions();


}

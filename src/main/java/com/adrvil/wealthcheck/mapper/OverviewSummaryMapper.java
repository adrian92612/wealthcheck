package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.CategoryNameProjection;
import com.adrvil.wealthcheck.dto.RecentTransactionsDto;
import com.adrvil.wealthcheck.dto.TopTransactionsDbDto;
import com.adrvil.wealthcheck.enums.TransactionType;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface OverviewSummaryMapper {
    @Select("SELECT COALESCE(SUM(balance), 0) FROM wallet WHERE user_id = #{userId}")
    BigDecimal getTotalBalance(Long userId);

    @Select("""
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE type = #{type}::transaction_type
                    AND created_at >= DATE_TRUNC('month', CURRENT_DATE)
                    AND created_at < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
                    AND user_id = #{userId}
            """)
    BigDecimal getThisMonthIncomeOrExpense(@Param("userId") Long userId,
                                           @Param("type") TransactionType type);

    @Select("""
                SELECT type,
                       category_id AS categoryId,
                       amount
                FROM transactions
                WHERE user_id = #{userId}
                    AND type = #{type}::transaction_type
                    AND created_at >= DATE_TRUNC('month', CURRENT_DATE)
                    AND created_at < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
                ORDER BY amount DESC
                LIMIT #{limit}
            """)
    List<TopTransactionsDbDto> getTopTransactions(Long userId,
                                                  TransactionType type,
                                                  int limit);

    @Select("""
                SELECT type, title, amount
                FROM transactions
                WHERE user_id = #{userId}
                ORDER BY created_at DESC
                LIMIT 5
            """)
    List<RecentTransactionsDto> getRecentTransactions(Long userId);

    @MapKey("id")
    List<CategoryNameProjection> findCategoryNamesByIds(@Param("userId") Long userId,
                                                        @Param("ids") Set<Long> ids);

}

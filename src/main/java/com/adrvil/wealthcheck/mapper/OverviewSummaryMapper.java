package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.CategoryNameProjection;
import com.adrvil.wealthcheck.dto.TransactionForNetDto;
import com.adrvil.wealthcheck.dto.response.CategoryPieRes;
import com.adrvil.wealthcheck.entity.MoneyGoalEntity;
import com.adrvil.wealthcheck.enums.TransactionType;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OverviewSummaryMapper {
    @Select("""
                SELECT COALESCE(SUM(balance), 0)
                FROM wallet
                WHERE user_id = #{userId}
                    AND soft_deleted = false
            """)
    BigDecimal getTotalBalance(Long userId);

    @Select("""
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE type = #{type}::transaction_type
                    AND transaction_date >= DATE_TRUNC('month', CURRENT_DATE)
                    AND transaction_date < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
                    AND user_id = #{userId}
                    AND soft_deleted = false
            """)
    BigDecimal getThisMonthIncomeOrExpense(@Param("userId") Long userId,
                                           @Param("type") TransactionType type);

//    @Select("""
//                SELECT type,
//                       category_id AS categoryId,
//                       amount
//                FROM transactions
//                WHERE user_id = #{userId}
//                    AND type = #{type}::transaction_type
//                    AND created_at >= DATE_TRUNC('month', CURRENT_DATE)
//                    AND created_at < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
//                    AND soft_deleted = false
//                ORDER BY amount DESC
//                LIMIT #{limit}
//            """)
//    List<TopTransactionsDbDto> getTopTransactions(Long userId,
//                                                  TransactionType type,
//                                                  int limit);

//    @Select("""
//                SELECT type, title, amount
//                FROM transactions
//                WHERE user_id = #{userId}
//                    AND soft_deleted = false
//                ORDER BY created_at DESC
//                LIMIT 5
//            """)
//    List<RecentTransactionsDto> getRecentTransactions(Long userId);

    @MapKey("id")
    List<CategoryNameProjection> findCategoryNamesByIds(@Param("userId") Long userId,
                                                        @Param("ids") Set<Long> ids);

    @Select("""
            SELECT
                amount,
                type,
                created_at
            FROM transactions
            WHERE user_id = #{userId}
                AND type in ('INCOME','EXPENSE')
                AND created_at >= #{startDate}
                AND created_at < #{endDate}
                AND soft_deleted = FALSE
            """)
    List<TransactionForNetDto> getTransactionForNetList(Long userId, LocalDate startDate, LocalDate endDate);

    @Select("""
            SELECT
                c.name AS name,
                SUM(t.amount) AS total_amount
            FROM transactions t
            JOIN category c ON t.category_id = c.id
            WHERE t.user_id = #{userId}
              AND t.type = #{type}::transaction_type
              AND t.created_at >= #{startDate}
              AND t.created_at < #{endDate}
              AND t.soft_deleted = FALSE
            GROUP BY c.name
            ORDER BY total_amount DESC
            """)
    List<CategoryPieRes> getTopCategories(Long userId, TransactionType type, LocalDate startDate, LocalDate endDate);

    @Select("""
            SELECT
                name,
                amount
            FROM goal
            WHERE user_id = #{userId} AND soft_deleted = FALSE;
            """)
    Optional<MoneyGoalEntity> getMoneyGoalByUserId(Long userId);

    @Insert("""
            INSERT INTO goal (
                name,
                amount,
                user_id,
                created_at,
                updated_at,
                soft_deleted)
            VALUES (
                #{name},
                #{amount},
                #{userId},
                #{createdAt},
                #{updatedAt},
                #{softDeleted})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void createMoneyGoal(MoneyGoalEntity entity);

    @Update("""
            UPDATE goal
            SET name = #{name},
                amount = #{amount},
                updated_at = NOW()
            WHERE user_id = #{userId} AND soft_deleted = FALSE;
            """)
    int updateMoneyGoal(MoneyGoalEntity entity);

    @Update("""
            UPDATE goal
            SET soft_deleted = TRUE,
                updated_at = NOW()
            WHERE user_id = #{userId} AND soft_deleted = FALSE;
            """)
    int softDeleteMoneyGoal(Long userId, Long id);

    @Update("""
            UPDATE goal
            SET soft_deleted = FALSE,
                updated_at = NOW()
            WHERE user_id = #{userId} AND soft_deleted = TRUE;
            """)
    int restoreMoneyGoal(Long userId, Long id);

    @Delete("""
            DELETE FROM goal
            WHERE user_id = #{userId} AND id = #{id}
            """)
    int permanentDeleteMoneyGoal(Long userId, Long id);

}

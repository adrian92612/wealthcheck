package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.TransactionFilterDto;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.entity.TransactionEntity;
import com.adrvil.wealthcheck.enums.TransactionType;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionMapper {

    @Insert("""
                INSERT INTO transactions (
                     title,
                     notes,
                     amount,
                     user_id,
                     from_wallet_id,
                     to_wallet_id,
                     category_id,
                     type,
                     transaction_date,
                     soft_deleted,
                     created_at,
                     updated_at)
                VALUES (
                    #{title},
                    #{notes},
                    #{amount},
                    #{userId},
                    #{fromWalletId},
                    #{toWalletId},
                    #{categoryId},
                    #{type}::transaction_type,
                    #{transactionDate},
                    #{softDeleted},
                    #{createdAt},
                    #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TransactionEntity transaction);

    @Update("""
                UPDATE transactions
                SET
                    from_wallet_id = #{fromWalletId},
                    to_wallet_id = #{toWalletId},
                    category_id = #{categoryId},
                    title = #{title},
                    notes = #{notes},
                    amount = #{amount},
                    type = #{type}::transaction_type,
                    transaction_date = #{transactionDate},
                    updated_at = NOW()
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int update(TransactionEntity transaction);

    @Select("""
                SELECT t.id,
                       t.title,
                       t.notes,
                       t.amount,
                       t.from_wallet_id,
                       t.to_wallet_id,
                       t.category_id,
                       fw.name AS fromWalletName,
                       tw.name AS toWalletName,
                       c.name AS categoryName,
                       c.icon AS categoryIcon,
                       t.type,
                       t.transaction_date,
                       t.created_at,
                       t.updated_at
                FROM transactions t
                LEFT JOIN wallet fw
                    ON t.from_wallet_id = fw.id
                    AND fw.user_id = #{userId}
                    AND fw.soft_deleted = FALSE
                LEFT JOIN wallet tw
                    ON t.to_wallet_id = tw.id
                    AND tw.user_id = #{userId}
                    AND tw.soft_deleted = FALSE
                LEFT JOIN category c
                    ON t.category_id = c.id
                    AND c.user_id = #{userId}
                    AND c.soft_deleted = FALSE
                WHERE t.id = #{id} AND t.user_id = #{userId} AND t.soft_deleted = #{softDeleted}
            """)
    Optional<TransactionRes> findResByIdAndUserId(Long id, Long userId, boolean softDeleted);

    @Select("""
                SELECT * FROM transactions
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = #{softDeleted}
            """)
    Optional<TransactionEntity> findByIdAndUserId(Long id, Long userId, boolean softDeleted);

    @Update("""
            UPDATE transactions
            SET soft_deleted = TRUE,
                updated_at = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int softDelete(Long userId, Long id);

    @Update("""
            UPDATE transactions
            SET soft_deleted = FALSE,
                updated_at = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = TRUE
            """)
    int restoreTransaction(Long userId, Long id);

    @Select("SELECT soft_deleted FROM transactions WHERE user_id = #{userId} AND id = #{id}")
    Boolean isSoftDeleted(@Param("userId") Long userId, @Param("id") Long id);

    @Delete("""
            DELETE FROM transactions
            WHERE user_id = #{userId}
                AND id = #{id}
                AND soft_deleted = true
            """)
    int permanentDeleteTransaction(Long userId, Long id);


    long countTransactions(@Param("userId") Long userId,
                           @Param("filter") TransactionFilterDto filter,
                           @Param("softDeleted") boolean softDeleted);

    List<TransactionRes> findTransactions(@Param("userId") Long userId,
                                          @Param("filter") TransactionFilterDto filter,
                                          @Param("softDeleted") boolean softDeleted);

    @Select("""
            SELECT
                t.id,
                t.title,
                t.notes,
                t.amount,
                t.from_wallet_id AS fromWalletId,
                t.to_wallet_id AS toWalletId,
                t.category_id AS categoryId,
                fw.name AS fromWalletName,
                tw.name AS toWalletName,
                c.name AS categoryName,
                c.icon AS categoryIcon,
                t.type,
                t.transaction_date,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt
                FROM transactions t
                LEFT JOIN wallet fw
                    ON t.from_wallet_id = fw.id
                    AND fw.user_id = #{userId}
                    AND fw.soft_deleted = FALSE
                LEFT JOIN wallet tw
                    ON t.to_wallet_id = tw.id
                    AND tw.user_id = #{userId}
                    AND tw.soft_deleted = FALSE
                LEFT JOIN category c
                    ON t.category_id = c.id
                    AND c.user_id = #{userId}
                    AND c.soft_deleted = FALSE
                WHERE t.user_id = #{userId}
                AND t.soft_deleted = FALSE
                ORDER BY t.transaction_date DESC
                LIMIT #{limit}
            """)
    List<TransactionRes> getRecentTransactions(Long userId, int limit);

    @Select("""
            SELECT
                t.id,
                t.title,
                t.notes,
                t.amount,
                t.from_wallet_id AS fromWalletId,
                t.to_wallet_id AS toWalletId,
                t.category_id AS categoryId,
                fw.name AS fromWalletName,
                tw.name AS toWalletName,
                c.name AS categoryName,
                c.icon AS categoryIcon,
                t.type,
                t.transaction_date,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt
                FROM transactions t
                LEFT JOIN wallet fw
                    ON t.from_wallet_id = fw.id
                    AND fw.user_id = #{userId}
                    AND fw.soft_deleted = FALSE
                LEFT JOIN wallet tw
                    ON t.to_wallet_id = tw.id
                    AND tw.user_id = #{userId}
                    AND tw.soft_deleted = FALSE
                LEFT JOIN category c
                    ON t.category_id = c.id
                    AND c.user_id = #{userId}
                    AND c.soft_deleted = FALSE
                WHERE t.user_id = #{userId}
                AND t.type = #{txnType}::transaction_type
                AND t.soft_deleted = FALSE
                ORDER BY t.amount DESC
                LIMIT #{limit}
            """)
    List<TransactionRes> getTopTransactions(Long userId, TransactionType txnType, int limit);


    @Select("""
            SELECT
                 COALESCE(SUM(
                     CASE
                         WHEN type = 'INCOME' AND to_wallet_id = #{walletId}
                             THEN amount
                         WHEN type = 'EXPENSE' AND from_wallet_id = #{walletId}
                             THEN -amount
                         WHEN type = 'TRANSFER' AND to_wallet_id = #{walletId}
                             THEN amount
                         WHEN type = 'TRANSFER' AND from_wallet_id = #{walletId}
                             THEN -amount
                         ELSE 0
                     END
                 ), 0) AS net_balance_change
             FROM transactions
             WHERE user_id = #{userId}
               AND soft_deleted = FALSE
               AND (
                     from_wallet_id = #{walletId}
                     OR to_wallet_id = #{walletId}
                   );
            """)
    BigDecimal calculateNetBalanceForWallet(Long walletId, Long userId);


}

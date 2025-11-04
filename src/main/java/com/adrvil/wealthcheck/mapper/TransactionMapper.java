package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.TransactionFilterDto;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.entity.TransactionEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

public interface TransactionMapper {

    @Insert("""
                INSERT INTO transactions (
                     user_id,
                     from_wallet_id,
                     to_wallet_id,
                     category_id,
                     title,
                     notes,
                     amount,
                     type,
                     soft_deleted,
                     created_at,
                     updated_at)
                VALUES (
                    #{userId},
                    #{fromWalletId},
                    #{toWalletId},
                    #{categoryId},
                    #{title},
                    #{notes},
                    #{amount},
                    CAST(#{type} AS transaction_type),
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
                    type = CAST(#{type} AS transaction_type),
                    updated_at = NOW()
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int update(TransactionEntity transaction);

    @Select("""
                SELECT t.id,
                       t.from_wallet_id,
                       t.to_wallet_id,
                       t.category_id,
                       fw.name AS fromWalletName,
                       tw.name AS toWalletName,
                       c.name AS categoryName,
                       c.icon AS categoryIcon,
                       t.title,
                       t.notes,
                       t.amount,
                       t.type,
                       t.created_at,
                       t.updated_at
                FROM transactions t
                LEFT JOIN wallet fw ON t.from_wallet_id = fw.id
                LEFT JOIN wallet tw ON t.to_wallet_id = tw.id
                LEFT JOIN category c ON t.category_id = c.id
                WHERE t.id = #{id} AND t.user_id = #{userId} AND t.soft_deleted = FALSE
            """)
    Optional<TransactionRes> findResByIdAndUserId(Long id, Long userId);

    @Select("""
                SELECT * FROM transactions
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    Optional<TransactionEntity> findByIdAndUserId(Long id, Long userId);

//    @Select("""
//            SELECT t.id,
//                   t.from_wallet_id,
//                   t.to_wallet_id,
//                   t.category_id,
//                   fw.name AS fromWalletName,
//                   tw.name AS toWalletName,
//                   c.name AS categoryName,
//                   c.icon AS categoryIcon,
//                   t.title,
//                   t.notes,
//                   t.amount,
//                   t.type,
//                   t.created_at,
//                   t.updated_at
//            FROM transactions t
//            LEFT JOIN wallet fw ON t.from_wallet_id = fw.id
//            LEFT JOIN wallet tw ON t.to_wallet_id = tw.id
//            LEFT JOIN category c ON t.category_id = c.id
//            WHERE t.user_id = #{userId} AND t.soft_deleted = FALSE
//            """)
//    List<TransactionRes> findAllResByUserId(Long userId);

    @Update("""
            UPDATE transactions
            SET soft_deleted = TRUE
            WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int softDelete(Long userId, Long id);

    long countTransactions(@Param("userId") Long userId,
                           @Param("filter") TransactionFilterDto filter);

    List<TransactionRes> findTransactions(@Param("userId") Long userId,
                                          @Param("filter") TransactionFilterDto filter);

}

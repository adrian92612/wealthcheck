package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.request.WalletReq;
import com.adrvil.wealthcheck.entity.WalletEntity;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletMapper {

    @Insert("""
                INSERT INTO wallet(
                    name,
                    user_id,
                    balance,
                    created_at,
                    updated_at)
                VALUES(
                    #{name},
                    #{userId},
                    #{balance},
                    #{createdAt},
                    #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WalletEntity wallet);

    @Select("""
                SELECT
                    id,
                    name,
                    user_id,
                    balance,
                    created_at,
                    updated_at
                FROM wallet
                WHERE user_id = #{userId} AND soft_deleted = #{softDeleted}
                ORDER BY created_at DESC
            """)
    List<WalletEntity> findWalletListByUserId(Long userId, boolean softDeleted);

//    @Select("""
//            SELECT id
//            FROM wallet
//            WHERE id = #{id}
//                AND user_id = #{userId}
//                AND soft_deleted = #{softDeleted}
//            """)
//    WalletEntity findWalletByUserIdAndId(Long userId, Long id, boolean softDeleted);

    @Select("""
                SELECT
                    id,
                    name,
                    user_id,
                    balance,
                    created_at,
                    updated_at
                FROM wallet
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    Optional<WalletEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
                UPDATE wallet
                SET name = #{req.name}, balance = #{req.balance}, updated_at = NOW()
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int updateWallet(Long id, Long userId, WalletReq req);

    @Update("""
                UPDATE wallet
                SET balance = #{balance}, updated_at = NOW()
                WHERE id = #{walletId} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    void updateBalance(Long walletId, Long userId, BigDecimal balance);


    @Update("""
                UPDATE wallet
                SET balance = balance - #{amount}, updated_at = NOW()
                WHERE id = #{walletId}
                    AND user_id = #{userId}
                    AND balance >= #{amount}
                    AND soft_deleted = FALSE
            """)
    int decreaseBalance(Long userId, Long walletId, BigDecimal amount);

    @Update("""
                UPDATE wallet
                SET balance = balance + #{amount}, updated_at = NOW()
                WHERE id = #{walletId}
                    AND user_id = #{userId}
                    AND soft_deleted = FALSE
            """)
    int increaseBalance(Long userId, Long walletId, BigDecimal amount);

    @Update("""
            UPDATE wallet
            SET soft_deleted = TRUE, updated_at = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int softDelete(Long id, Long userId);

    @Update("""
                UPDATE wallet
                SET
                    soft_deleted = FALSE,
                    updated_at = NOW()
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = TRUE
            """)
    int restoreWallet(Long id, Long userId);

    @Select("SELECT soft_deleted FROM wallet WHERE user_id = #{userId} AND id = #{id}")
    Boolean isSoftDeleted(@Param("userId") Long userId, @Param("id") Long id);

    @Delete("""
            DELETE FROM wallet
            WHERE user_id = #{userId}
                AND id = #{id}
                AND soft_deleted = true
            """)
    int permanentDeleteWallet(Long userId, Long id);
}

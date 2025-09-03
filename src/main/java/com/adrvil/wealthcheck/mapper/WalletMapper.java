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

    @Select("SELECT * FROM wallet WHERE user_id = #{userId}")
    List<WalletEntity> findByUserId(Long userId);

    @Select("SELECT * FROM wallet WHERE id = #{id} AND user_id = #{userId}")
    Optional<WalletEntity> findByIdAndUserId(Long id, Long userId);

    @Update("""
                UPDATE wallet
                SET name = #{req.name}, balance = #{req.balance}, updated_at = NOW()
                WHERE id = #{id} AND user_id = #{userId}
            """)
    int updateWallet(Long id, Long userId, WalletReq req);

    @Update("""
                UPDATE wallet SET balance = balance - #{amount}, updated_at = NOW()
                WHERE id = #{walletId} AND user_id = #{userId} AND balance >= #{amount}
            """)
    int decreaseBalance(Long userId, Long walletId, BigDecimal amount);

    @Update("""
                UPDATE wallet SET balance = balance + #{amount}, updated_at = NOW()
                WHERE id = #{walletId} AND user_id = #{userId}
            """)
    int increaseBalance(Long userId, Long walletId, BigDecimal amount);


}

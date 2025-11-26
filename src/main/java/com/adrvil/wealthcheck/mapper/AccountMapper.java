package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.entity.AccountEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AccountMapper {

    @Insert("""
                INSERT INTO account(
                    name,
                    email,
                    provider,
                    provider_id,
                    avatar_url,
                    created_at,
                    updated_at)
                VALUES (
                    #{name},
                    #{email},
                    #{provider},
                    #{providerId},
                    #{avatarUrl},
                    #{createdAt},
                    #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AccountEntity account);

    @Select("SELECT * FROM account WHERE email = #{email}")
    AccountEntity findByEmail(String email);

    @Select("SELECT * FROM account WHERE id = #{userId}")
    AccountEntity findById(Long userId);

    @Select("SELECT id FROM account WHERE email = #{email}")
    Long getUserIdByEmail(String email);

    @Update("""
            UPDATE account
            SET is_new_user = #{isNewUser}, updated_at = NOW()
            WHERE id = #{userId}
            """)
    void setIsNewUser(Long userId, boolean isNewUser);
}

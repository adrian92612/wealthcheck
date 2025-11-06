package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.entity.AccountEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface AccountMapper {

    @Insert("""
                INSERT INTO account(
                    name,
                    email,
                    provider_id,
                    avatar_url,
                    is_active,
                    created_at,
                    updated_at)
                VALUES (
                    #{name},
                    #{email},
                    #{providerId},
                    #{avatarUrl},
                    #{isActive},
                    #{createdAt},
                    #{updatedAt})
            """)
    void insert(AccountEntity account);

    @Select("SELECT * FROM account WHERE email = #{email}")
    AccountEntity findByEmail(String email);

    @Select("SELECT id FROM account WHERE email = #{email}")
    Long getUserIdByEmail(String email);
}

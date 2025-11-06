package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.entity.CategoryEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

public interface CategoryMapper {

    @Insert("""
            INSERT INTO category (
                user_id,
                name,
                description,
                type,
                icon,
                soft_deleted,
                created_at,
                updated_at)
            VALUES (
                #{userId},
                #{name},
                #{description},
                #{type}::category_type,
                #{icon},
                #{softDeleted},
                #{createdAt},
                #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCategory(CategoryEntity category);

    @Select("""
                SELECT
                    id,
                    user_id,
                    name,
                    description,
                    type,
                    icon,
                    created_at,
                    updated_at
                FROM category
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    Optional<CategoryEntity> getCategoryByIdAndUserId(Long id, Long userId);

    @Select("""
                SELECT
                    id,
                    user_id,
                    name,
                    description,
                    type,
                    icon,
                    created_at,
                    updated_at
                FROM category
                WHERE user_id = #{userId} AND soft_deleted = FALSE
                ORDER BY created_at DESC
            """)
    List<CategoryEntity> getAllCategoryByUserId(Long userId);

    @Update("""
                UPDATE category
                SET
                    name = #{req.name},
                    description = #{req.description},
                    type = #{req.type}::category_type,
                    icon = #{req.icon},
                    updated_at = NOW()
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int updateCategory(Long id, Long userId, CategoryReq req);

    @Update("""
                UPDATE category
                SET
                    soft_deleted = TRUE,
                    updated_at = NOW()
                WHERE id = #{id} AND user_id = #{userId} AND soft_deleted = FALSE
            """)
    int softDeleteCategory(Long id, Long userId);
}

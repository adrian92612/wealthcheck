package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.entity.CategoryEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CategoryMapper {

    @Select("""
        SELECT * FROM category
        WHERE id = #{id} AND user_id = #{userId} AND is_active = TRUE
    """)
    CategoryEntity getCategoryByIdAndUserId(Long id, Long userId);

    @Select("""
        SELECT * FROM category
        WHERE user_id = #{userId} AND is_active = TRUE
    """)
    List<CategoryEntity> getAllCategoryByUserId(Long userId);

    @Insert("""
        INSERT INTO category (user_id, name, description, type, icon, is_active, created_at, updated_at)
        VALUES (#{userId}, #{name}, #{description}, #{type}, #{icon}, #{isActive}, #{createdAt}, #{updatedAt})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCategory(CategoryEntity category);

    @Update("""
        UPDATE category
        SET name = #{req.name}, description = #{req.description}, type = #{req.type}, icon = #{req.icon}, updated_at = NOW()
        WHERE id = #{id} AND user_id = #{userId}
    """)
    int updateCategory(Long id, Long userId, CategoryReq req);

    @Update("""
        UPDATE category
        SET is_active = FALSE, updated_at = NOW()
        WHERE id = #{id} AND user_id = #{userId}
    """)
    int softDeleteCategory(Long id, Long userId);
}

package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.entity.CategoryEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE id = #{id} AND user_id = #{userId}")
    CategoryEntity getCategoryByIdAndUserId(Long id, Long userId);

    @Select("SELECT * FROM category WHERE user_id = #{userId}")
    List<CategoryEntity> getAllCategoryByUserId(Long userId);

    @Insert("""
        INSERT INTO category (user_id, name, description, type, icon, is_active, created_at, updated_at)
        VALUES (#{userId}, #{name}, #{description}, #{type}, #{icon}, #{isActive}, #{createdAt}, #{updatedAt})
""")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCategory(CategoryEntity category);

}

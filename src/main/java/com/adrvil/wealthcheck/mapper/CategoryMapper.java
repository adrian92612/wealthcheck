package com.adrvil.wealthcheck.mapper;

import com.adrvil.wealthcheck.entity.CategoryEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE id = #{id} AND user_id = #{userId}")
    CategoryEntity getCategoryByIdAndUserId(Long id, Long userId);

    @Select("SELECT * FROM category WHERE user_id = #{userId}")
    List<CategoryEntity> getAllCategoryByUserId(Long userId);


}

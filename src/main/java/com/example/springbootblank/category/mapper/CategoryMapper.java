package com.example.springbootblank.category.mapper;

import com.example.springbootblank.category.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<DishCategory> listByShopId(@Param("shopId") Long shopId);

    int insertCategory(DishCategory category);

    int updateCategory(@Param("id") Long id, @Param("category") DishCategory category);

    int deleteCategory(@Param("id") Long id);

    int countDishByCategory(@Param("categoryId") Long categoryId);
}

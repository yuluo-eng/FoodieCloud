package com.example.springbootblank.dish.mapper;

import com.example.springbootblank.dish.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishMapper {

    long countMerchantDishes(@Param("shopId") Long shopId,
                             @Param("categoryId") Long categoryId,
                             @Param("dishName") String dishName,
                             @Param("status") Integer status);

    List<Dish> listMerchantDishes(@Param("shopId") Long shopId,
                                  @Param("categoryId") Long categoryId,
                                  @Param("dishName") String dishName,
                                  @Param("status") Integer status,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    int insertDish(Dish dish);

    int updateDish(@Param("id") Long id, @Param("dish") Dish dish);

    int deleteDish(@Param("id") Long id);

    int updateDishStatus(@Param("id") Long id, @Param("status") Integer status);

    List<Dish> listUserDishes(@Param("shopId") Long shopId,
                              @Param("categoryId") Long categoryId);
}

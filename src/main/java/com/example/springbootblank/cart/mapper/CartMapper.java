package com.example.springbootblank.cart.mapper;

import com.example.springbootblank.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CartMapper {

    List<Map<String, Object>> listCartItems(@Param("userId") Long userId);

    CartItem findByUserAndDish(@Param("userId") Long userId, @Param("dishId") Long dishId);

    int insertCartItem(CartItem cartItem);

    int updateCartQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    int deleteByUserAndDish(@Param("userId") Long userId, @Param("dishId") Long dishId);

    int updateSelectedByUserAndDish(@Param("userId") Long userId,
                                    @Param("dishId") Long dishId,
                                    @Param("selected") Integer selected);

    int clearByUser(@Param("userId") Long userId);

    List<Map<String, Object>> listSelectedCartForOrder(@Param("userId") Long userId);

    int clearSelectedCart(@Param("userId") Long userId);
}

package com.example.springbootblank.cart.mapper;

import com.example.springbootblank.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 购物车表 {@code cart_item} 的 MyBatis 访问层。
 * SQL 定义见 {@code resources/mapper/CartMapper.xml}。
 */
@Mapper
public interface CartMapper {

    /** 联表 dish 返回展示用字段（不含 shopId）。 */
    List<Map<String, Object>> listCartItems(@Param("userId") Long userId);

    /** 按用户 + 菜品查唯一购物车行（加购合并数量时用）。 */
    CartItem findByUserAndDish(@Param("userId") Long userId, @Param("dishId") Long dishId);

    int insertCartItem(CartItem cartItem);

    int updateCartQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    int deleteByUserAndDish(@Param("userId") Long userId, @Param("dishId") Long dishId);

    int updateSelectedByUserAndDish(@Param("userId") Long userId,
                                    @Param("dishId") Long dishId,
                                    @Param("selected") Integer selected);

    int clearByUser(@Param("userId") Long userId);

    /**
     * 供下单使用：仅返回 selected=1 的项，并带上 dish.shop_id 供订单模块校验同店。
     * 由 {@code OrderServiceImpl} 调用，非 CartController 暴露。
     */
    List<Map<String, Object>> listSelectedCartForOrder(@Param("userId") Long userId);

    /** 下单成功后删除已勾选条目，未勾选保留。 */
    int clearSelectedCart(@Param("userId") Long userId);
}

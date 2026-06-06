package com.example.springbootblank.cart.service;

import com.example.springbootblank.cart.dto.CartAddRequest;
import com.example.springbootblank.cart.dto.CartQuantityUpdateRequest;
import com.example.springbootblank.cart.dto.CartSelectedUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * 购物车业务接口（用户端）。
 * <p>
 * 购物车按「用户 + 菜品」维度存储，表 {@code cart_item}。
 * 下单模块（{@code OrderServiceImpl}）会读取 {@code selected=1} 的条目生成订单。
 */
public interface CartService {

    /** 列出当前用户购物车，返回 Map 含 dishId、dishName、unitPrice、quantity、selected。 */
    List<Map<String, Object>> listCart(String authorization);

    /** 加购：校验菜品可售后写入或累加数量，并快照当时单价到 unit_price。 */
    void addToCart(String authorization, CartAddRequest req);

    /** 修改某菜品数量。 */
    void updateQuantity(String authorization, Long dishId, CartQuantityUpdateRequest req);

    /** 删除某菜品条目。 */
    void deleteItem(String authorization, Long dishId);

    /** 更新勾选状态：0 未选，1 已选（结算时使用）。 */
    void updateSelected(String authorization, Long dishId, CartSelectedUpdateRequest req);

    /** 清空该用户全部购物车项。 */
    void clear(String authorization);
}

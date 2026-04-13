package com.example.springbootblank.cart.service;

import com.example.springbootblank.cart.dto.CartAddRequest;
import com.example.springbootblank.cart.dto.CartQuantityUpdateRequest;
import com.example.springbootblank.cart.dto.CartSelectedUpdateRequest;

import java.util.List;
import java.util.Map;

public interface CartService {

    List<Map<String, Object>> listCart(String authorization);

    void addToCart(String authorization, CartAddRequest req);

    void updateQuantity(String authorization, Long dishId, CartQuantityUpdateRequest req);

    void deleteItem(String authorization, Long dishId);

    void updateSelected(String authorization, Long dishId, CartSelectedUpdateRequest req);

    void clear(String authorization);
}

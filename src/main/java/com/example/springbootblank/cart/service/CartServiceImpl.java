package com.example.springbootblank.cart.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.cart.dto.CartAddRequest;
import com.example.springbootblank.cart.dto.CartQuantityUpdateRequest;
import com.example.springbootblank.cart.dto.CartSelectedUpdateRequest;
import com.example.springbootblank.cart.entity.CartItem;
import com.example.springbootblank.cart.mapper.CartMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.dish.mapper.DishMapper;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private final JwtService jwtService;
    private final CartMapper cartMapper;
    private final DishMapper dishMapper;

    public CartServiceImpl(JwtService jwtService, CartMapper cartMapper, DishMapper dishMapper) {
        this.jwtService = jwtService;
        this.cartMapper = cartMapper;
        this.dishMapper = dishMapper;
    }

    @Override
    public List<Map<String, Object>> listCart(String authorization) {
        Long userId = resolveUserId(authorization);
        return cartMapper.listCartItems(userId);
    }

    @Override
    public void addToCart(String authorization, CartAddRequest req) {
        Long userId = resolveUserId(authorization);
        if (req.dishId() == null || req.quantity() == null || req.quantity() <= 0) {
            throw new BusinessException(400, "参数错误");
        }

        var dishList = dishMapper.listUserDishes(1L, null).stream().filter(d -> d.getId().equals(req.dishId())).toList();
        if (dishList.isEmpty()) {
            throw new BusinessException(400, "菜品不存在或已下架");
        }

        CartItem exist = cartMapper.findByUserAndDish(userId, req.dishId());
        if (exist == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setDishId(req.dishId());
            item.setQuantity(req.quantity());
            item.setUnitPrice(dishList.get(0).getPrice());
            item.setSelected(1);
            cartMapper.insertCartItem(item);
        } else {
            cartMapper.updateCartQuantity(exist.getId(), exist.getQuantity() + req.quantity());
        }
    }

    @Override
    public void updateQuantity(String authorization, Long dishId, CartQuantityUpdateRequest req) {
        Long userId = resolveUserId(authorization);
        if (req.quantity() == null || req.quantity() <= 0) {
            throw new BusinessException(400, "数量必须大于0");
        }
        CartItem exist = cartMapper.findByUserAndDish(userId, dishId);
        if (exist == null) {
            throw new BusinessException(404, "购物车项不存在");
        }
        cartMapper.updateCartQuantity(exist.getId(), req.quantity());
    }

    @Override
    public void deleteItem(String authorization, Long dishId) {
        Long userId = resolveUserId(authorization);
        cartMapper.deleteByUserAndDish(userId, dishId);
    }

    @Override
    public void updateSelected(String authorization, Long dishId, CartSelectedUpdateRequest req) {
        Long userId = resolveUserId(authorization);
        if (req.selected() == null || (req.selected() != 0 && req.selected() != 1)) {
            throw new BusinessException(400, "selected 仅支持 0/1");
        }
        CartItem exist = cartMapper.findByUserAndDish(userId, dishId);
        if (exist == null) {
            throw new BusinessException(404, "购物车项不存在");
        }
        cartMapper.updateSelectedByUserAndDish(userId, dishId, req.selected());
    }

    @Override
    public void clear(String authorization) {
        Long userId = resolveUserId(authorization);
        cartMapper.clearByUser(userId);
    }

    private Long resolveUserId(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        try {
            var principal = jwtService.parse(token);
            if (!JwtService.TYPE_USER.equals(principal.type())) {
                throw new BusinessException(403, "无权限");
            }
            return principal.id();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
    }
}

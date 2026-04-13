package com.example.springbootblank.dish.service;

import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.dish.dto.DishCreateRequest;
import com.example.springbootblank.dish.dto.DishStatusUpdateRequest;
import com.example.springbootblank.dish.dto.DishUpdateRequest;
import com.example.springbootblank.dish.entity.Dish;
import com.example.springbootblank.dish.mapper.DishMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final MerchantAuthGuard merchantAuthGuard;

    public DishServiceImpl(DishMapper dishMapper, MerchantAuthGuard merchantAuthGuard) {
        this.dishMapper = dishMapper;
        this.merchantAuthGuard = merchantAuthGuard;
    }

    @Override
    public Map<String, Object> merchantDishes(String authorization, int page, int pageSize, Long shopId, Long categoryId, String dishName, Integer status) {
        ensureDishOperator(authorization);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safePageSize;

        String keyword = StringUtils.hasText(dishName) ? dishName.trim() : null;
        long total = dishMapper.countMerchantDishes(shopId, categoryId, keyword, status);
        List<Dish> records = dishMapper.listMerchantDishes(shopId, categoryId, keyword, status, offset, safePageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("page", safePage);
        data.put("pageSize", safePageSize);
        data.put("total", total);
        data.put("records", records);
        return data;
    }

    @Override
    public Map<String, Object> createDish(String authorization, DishCreateRequest req) {
        ensureDishOperator(authorization);
        Dish dish = new Dish();
        dish.setShopId(req.shopId());
        dish.setCategoryId(req.categoryId());
        dish.setDishName(req.dishName());
        dish.setPrice(req.price());
        dish.setImageUrl(req.imageUrl());
        dish.setDescription(req.description());
        dish.setStock(req.stock() == null ? 0 : req.stock());
        dish.setStatus(req.status() == null ? 1 : req.status());
        dishMapper.insertDish(dish);
        return Map.of("id", dish.getId());
    }

    @Override
    public void updateDish(String authorization, Long id, DishUpdateRequest req) {
        ensureDishOperator(authorization);
        Dish dish = new Dish();
        dish.setShopId(req.shopId());
        dish.setCategoryId(req.categoryId());
        dish.setDishName(req.dishName());
        dish.setPrice(req.price());
        dish.setImageUrl(req.imageUrl());
        dish.setDescription(req.description());
        dish.setStock(req.stock());
        dish.setStatus(req.status());
        dishMapper.updateDish(id, dish);
    }

    @Override
    public void deleteDish(String authorization, Long id) {
        ensureDishOperator(authorization);
        dishMapper.deleteDish(id);
    }

    @Override
    public void updateDishStatus(String authorization, Long id, DishStatusUpdateRequest req) {
        ensureDishOperator(authorization);
        dishMapper.updateDishStatus(id, req.status());
    }

    @Override
    public List<Map<String, Object>> userDishes(Long shopId, Long categoryId) {
        return dishMapper.listUserDishes(shopId, categoryId).stream().map(d -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", d.getId());
            item.put("shopId", d.getShopId());
            item.put("categoryId", d.getCategoryId());
            item.put("dishName", d.getDishName());
            item.put("price", d.getPrice());
            item.put("imageUrl", d.getImageUrl());
            item.put("description", d.getDescription());
            item.put("stock", d.getStock());
            item.put("status", d.getStatus());
            return item;
        }).toList();
    }

    private void ensureDishOperator(String authorization) {
        merchantAuthGuard.requireEmployeeRole(authorization, "SUPER_ADMIN", "SHOP_MANAGER", "STAFF");
    }
}

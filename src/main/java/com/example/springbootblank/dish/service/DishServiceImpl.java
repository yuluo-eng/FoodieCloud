package com.example.springbootblank.dish.service;

import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.dish.dto.DishCreateRequest;
import com.example.springbootblank.dish.dto.DishStatusUpdateRequest;
import com.example.springbootblank.dish.dto.DishUpdateRequest;
import com.example.springbootblank.dish.entity.Dish;
import com.example.springbootblank.dish.mapper.DishMapper;
import com.example.springbootblank.log.service.OpLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DishServiceImpl implements DishService {

    private static final Logger log = LoggerFactory.getLogger(DishServiceImpl.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final DishMapper dishMapper;
    private final MerchantAuthGuard merchantAuthGuard;
    private final OpLogService opLogService;

    public DishServiceImpl(DishMapper dishMapper, MerchantAuthGuard merchantAuthGuard,
                           OpLogService opLogService) {
        this.dishMapper = dishMapper;
        this.merchantAuthGuard = merchantAuthGuard;
        this.opLogService = opLogService;
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
        opLogService.log("EMPLOYEE", null, "DISH", "CREATE",
                "新建菜品: " + dish.getDishName() + ", ID=" + dish.getId());
        return Map.of("id", dish.getId());
    }

    @Override
    public void updateDish(String authorization, Long id, DishUpdateRequest req) {
        ensureDishOperator(authorization);

        Dish oldDish = dishMapper.findById(id);
        String oldImage = oldDish != null ? oldDish.getImageUrl() : null;

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
        opLogService.log("EMPLOYEE", null, "DISH", "UPDATE", "修改菜品: ID=" + id);

        if (StringUtils.hasText(oldImage) && StringUtils.hasText(req.imageUrl())
                && !oldImage.equals(req.imageUrl())) {
            tryDeleteOldImage(oldImage);
        }
    }

    @Override
    public void deleteDish(String authorization, Long id) {
        ensureDishOperator(authorization);
        dishMapper.deleteDish(id);
        opLogService.log("EMPLOYEE", null, "DISH", "DELETE", "删除菜品: ID=" + id);
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

    private void tryDeleteOldImage(String imageUrl) {
        try {
            String filename = imageUrl.contains("/") ? imageUrl.substring(imageUrl.lastIndexOf('/') + 1) : imageUrl;
            if (!StringUtils.hasText(filename)) return;
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.warn("旧图片清理失败: {}, 原因: {}", imageUrl, e.getMessage());
        }
    }
}

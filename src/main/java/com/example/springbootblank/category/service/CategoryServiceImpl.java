package com.example.springbootblank.category.service;

import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.category.dto.CategoryCreateRequest;
import com.example.springbootblank.category.dto.CategoryUpdateRequest;
import com.example.springbootblank.category.entity.DishCategory;
import com.example.springbootblank.category.mapper.CategoryMapper;
import com.example.springbootblank.common.error.BusinessException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final MerchantAuthGuard merchantAuthGuard;

    public CategoryServiceImpl(CategoryMapper categoryMapper, MerchantAuthGuard merchantAuthGuard) {
        this.categoryMapper = categoryMapper;
        this.merchantAuthGuard = merchantAuthGuard;
    }

    @Override
    public List<Map<String, Object>> list(String authorization, Long shopId) {
        ensureManager(authorization);
        return categoryMapper.listByShopId(shopId).stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("shopId", c.getShopId());
            item.put("categoryName", c.getCategoryName());
            item.put("sort", c.getSort());
            item.put("status", c.getStatus());
            return item;
        }).toList();
    }

    @Override
    public List<Map<String, Object>> userList(Long shopId) {
        return categoryMapper.listByShopId(shopId).stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .map(c -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("shopId", c.getShopId());
                    item.put("categoryName", c.getCategoryName());
                    item.put("sort", c.getSort());
                    item.put("status", c.getStatus());
                    return item;
                })
                .toList();
    }

    @Override
    public Map<String, Object> create(String authorization, CategoryCreateRequest req) {
        ensureManager(authorization);
        DishCategory c = new DishCategory();
        c.setShopId(req.shopId());
        c.setCategoryName(req.categoryName());
        c.setSort(req.sort() == null ? 0 : req.sort());
        c.setStatus(req.status() == null ? 1 : req.status());
        categoryMapper.insertCategory(c);
        return Map.of("id", c.getId());
    }

    @Override
    public void update(String authorization, Long id, CategoryUpdateRequest req) {
        ensureManager(authorization);
        DishCategory c = new DishCategory();
        c.setShopId(req.shopId());
        c.setCategoryName(req.categoryName());
        c.setSort(req.sort());
        c.setStatus(req.status());
        int rows = categoryMapper.updateCategory(id, c);
        if (rows == 0) {
            throw new BusinessException(404, "分类不存在");
        }
    }

    @Override
    public void delete(String authorization, Long id) {
        ensureManager(authorization);
        if (categoryMapper.countDishByCategory(id) > 0) {
            throw new BusinessException(400, "分类下有菜品，无法删除");
        }
        int rows = categoryMapper.deleteCategory(id);
        if (rows == 0) {
            throw new BusinessException(404, "分类不存在");
        }
    }

    private void ensureManager(String authorization) {
        merchantAuthGuard.requireEmployeeRole(authorization, "SUPER_ADMIN", "SHOP_MANAGER");
    }
}

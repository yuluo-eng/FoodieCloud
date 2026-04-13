package com.example.springbootblank.shop.service;

import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.shop.dto.ShopBusinessStatusUpdateRequest;
import com.example.springbootblank.shop.dto.ShopUpdateRequest;
import com.example.springbootblank.shop.entity.Shop;
import com.example.springbootblank.shop.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ShopServiceImpl implements ShopService {

    private final ShopMapper shopMapper;
    private final MerchantAuthGuard merchantAuthGuard;

    public ShopServiceImpl(ShopMapper shopMapper, MerchantAuthGuard merchantAuthGuard) {
        this.shopMapper = shopMapper;
        this.merchantAuthGuard = merchantAuthGuard;
    }

    @Override
    public Map<String, Object> getShop(String authorization, Long shopId) {
        ensureManager(authorization);
        Shop shop = shopMapper.findById(shopId);
        if (shop == null) {
            throw new BusinessException(404, "店铺不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", shop.getId());
        data.put("shopName", shop.getShopName());
        data.put("address", shop.getAddress());
        data.put("phone", shop.getPhone());
        data.put("businessStatus", shop.getBusinessStatus());
        data.put("notice", shop.getNotice());
        return data;
    }

    @Override
    public void updateShop(String authorization, Long shopId, ShopUpdateRequest req) {
        ensureManager(authorization);
        Shop shop = new Shop();
        shop.setShopName(req.shopName());
        shop.setAddress(req.address());
        shop.setPhone(req.phone());
        shop.setNotice(req.notice());
        int rows = shopMapper.updateShop(shopId, shop);
        if (rows == 0) {
            throw new BusinessException(404, "店铺不存在");
        }
    }

    @Override
    public void updateBusinessStatus(String authorization, Long shopId, ShopBusinessStatusUpdateRequest req) {
        ensureManager(authorization);
        if (req.businessStatus() == null || (req.businessStatus() != 0 && req.businessStatus() != 1)) {
            throw new BusinessException(400, "businessStatus 仅支持 0/1");
        }
        int rows = shopMapper.updateBusinessStatus(shopId, req.businessStatus());
        if (rows == 0) {
            throw new BusinessException(404, "店铺不存在");
        }
    }

    private void ensureManager(String authorization) {
        merchantAuthGuard.requireEmployeeRole(authorization, "SUPER_ADMIN", "SHOP_MANAGER");
    }
}

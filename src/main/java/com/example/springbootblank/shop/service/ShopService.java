package com.example.springbootblank.shop.service;

import com.example.springbootblank.shop.dto.ShopBusinessStatusUpdateRequest;
import com.example.springbootblank.shop.dto.ShopUpdateRequest;

import java.util.Map;

public interface ShopService {

    Map<String, Object> getShop(String authorization, Long shopId);

    void updateShop(String authorization, Long shopId, ShopUpdateRequest req);

    void updateBusinessStatus(String authorization, Long shopId, ShopBusinessStatusUpdateRequest req);
}

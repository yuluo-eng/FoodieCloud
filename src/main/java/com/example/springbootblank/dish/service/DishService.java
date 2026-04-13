package com.example.springbootblank.dish.service;

import com.example.springbootblank.dish.dto.DishCreateRequest;
import com.example.springbootblank.dish.dto.DishStatusUpdateRequest;
import com.example.springbootblank.dish.dto.DishUpdateRequest;

import java.util.List;
import java.util.Map;

public interface DishService {

    Map<String, Object> merchantDishes(String authorization,
                                       int page,
                                       int pageSize,
                                       Long shopId,
                                       Long categoryId,
                                       String dishName,
                                       Integer status);

    Map<String, Object> createDish(String authorization, DishCreateRequest req);

    void updateDish(String authorization, Long id, DishUpdateRequest req);

    void deleteDish(String authorization, Long id);

    void updateDishStatus(String authorization, Long id, DishStatusUpdateRequest req);

    List<Map<String, Object>> userDishes(Long shopId, Long categoryId);
}

package com.example.springbootblank.category.service;

import com.example.springbootblank.category.dto.CategoryCreateRequest;
import com.example.springbootblank.category.dto.CategoryUpdateRequest;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    List<Map<String, Object>> list(String authorization, Long shopId);

    List<Map<String, Object>> userList(Long shopId);

    Map<String, Object> create(String authorization, CategoryCreateRequest req);

    void update(String authorization, Long id, CategoryUpdateRequest req);

    void delete(String authorization, Long id);
}

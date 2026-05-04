package com.example.springbootblank.shop.mapper;

import com.example.springbootblank.shop.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShopMapper {

    Shop findById(@Param("id") Long id);

    List<Shop> listAll();

    int updateShop(@Param("id") Long id, @Param("shop") Shop shop);

    int updateBusinessStatus(@Param("id") Long id, @Param("businessStatus") Integer businessStatus);
}

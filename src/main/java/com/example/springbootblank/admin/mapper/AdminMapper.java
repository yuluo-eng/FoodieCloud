package com.example.springbootblank.admin.mapper;

import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.shop.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

    long countAllUsers();

    List<Map<String, Object>> listUsers(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int updateUserEnabled(@Param("id") long id, @Param("enabled") int enabled);

    long countAllRiders();

    List<Map<String, Object>> listRiders(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int updateRiderEnabled(@Param("id") long id, @Param("enabled") int enabled);

    long countRiderDelivered(@Param("riderId") long riderId);

    long countAllOrders(@Param("status") Integer status, @Param("shopId") Long shopId);

    List<Order> listAllOrders(@Param("status") Integer status, @Param("shopId") Long shopId,
                              @Param("pageSize") int pageSize, @Param("offset") int offset);

    long countTotalOrders();

    BigDecimal sumTotalRevenue();

    long countActiveRiders();

    long countShops();

    List<Shop> listShops();

    int updateShopBusinessStatus(@Param("id") long id, @Param("businessStatus") int businessStatus);
}

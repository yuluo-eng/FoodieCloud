package com.example.springbootblank.cart.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车条目，对应数据库表 {@code cart_item}。
 * <p>
 * 一行表示：某用户选择了某道菜及数量。不存 shop_id，店铺归属通过 dish_id 关联 dish 表推断。
 */
public class CartItem {
    private Long id;
    /** 所属顾客 user.id */
    private Long userId;
    /** 所选菜品 dish.id */
    private Long dishId;
    /** 购买数量 */
    private Integer quantity;
    /**
     * 加购时的单价快照（元）。
     * 商家后续改价不影响已加购项；下单按此价格计算。
     */
    private BigDecimal unitPrice;
    /** 是否参与结算：1 勾选，0 未勾选 */
    private Integer selected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

package com.example.springbootblank.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long shopId;
    private BigDecimal totalAmount;
    private String remark;
    private Integer status;
    private Integer payStatus;
    private Long riderId;
    private LocalDateTime riderAcceptTime;
    private LocalDateTime riderArriveShopTime;
    private LocalDateTime riderPickupTime;
    private LocalDateTime riderDeliveredTime;
    private LocalDateTime payTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public Long getRiderId() {
        return riderId;
    }

    public void setRiderId(Long riderId) {
        this.riderId = riderId;
    }

    public LocalDateTime getRiderAcceptTime() {
        return riderAcceptTime;
    }

    public void setRiderAcceptTime(LocalDateTime riderAcceptTime) {
        this.riderAcceptTime = riderAcceptTime;
    }

    public LocalDateTime getRiderArriveShopTime() {
        return riderArriveShopTime;
    }

    public void setRiderArriveShopTime(LocalDateTime riderArriveShopTime) {
        this.riderArriveShopTime = riderArriveShopTime;
    }

    public LocalDateTime getRiderPickupTime() {
        return riderPickupTime;
    }

    public void setRiderPickupTime(LocalDateTime riderPickupTime) {
        this.riderPickupTime = riderPickupTime;
    }

    public LocalDateTime getRiderDeliveredTime() {
        return riderDeliveredTime;
    }

    public void setRiderDeliveredTime(LocalDateTime riderDeliveredTime) {
        this.riderDeliveredTime = riderDeliveredTime;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
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

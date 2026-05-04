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

    // JOIN fields for cross-role display
    private String shippingAddress;
    private String dishSummary;
    private String riderName;
    private String riderPhone;
    private String shopName;
    private String shopAddress;
    private String shopPhone;
    private String receiverName;
    private String shippingPhoneNo;

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

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getDishSummary() { return dishSummary; }
    public void setDishSummary(String dishSummary) { this.dishSummary = dishSummary; }
    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }
    public String getRiderPhone() { return riderPhone; }
    public void setRiderPhone(String riderPhone) { this.riderPhone = riderPhone; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getShopAddress() { return shopAddress; }
    public void setShopAddress(String shopAddress) { this.shopAddress = shopAddress; }
    public String getShopPhone() { return shopPhone; }
    public void setShopPhone(String shopPhone) { this.shopPhone = shopPhone; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getShippingPhoneNo() { return shippingPhoneNo; }
    public void setShippingPhoneNo(String shippingPhoneNo) { this.shippingPhoneNo = shippingPhoneNo; }
}

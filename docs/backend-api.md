# 悦食汇后端接口文档（MVP）

## 接口约定

- Base URL: `/api`
- 鉴权: `Authorization: Bearer <token>`
- 请求体: `application/json`
- 时间格式: `yyyy-MM-dd HH:mm:ss`
- 分页参数: `page`（从 1 开始）, `pageSize`

JWT 身份类型（`type`）：

- `USER`：C 端用户
- `EMPLOYEE`：商家端员工
- `RIDER`：骑手端

跨端复用 Token 的规则：

- `401`：未携带 `Bearer`、Token 无法解析、已过期、签名无效
- `403`：Token 可解析，但**身份类型不匹配**（例如用骑手 Token 访问商家端接口），或商家端员工**角色不足**

统一返回体示例:

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

## 1. 认证模块 Auth

### 1.1 用户注册

- `POST /api/auth/user/register`

请求:

```json
{
  "username": "testuser2",
  "password": "123456",
  "phone": "13600000001",
  "nickname": "小明"
}
```

返回:

```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": 2
  }
}
```

### 1.2 用户登录（C端）

- `POST /api/auth/user/login`

请求:

```json
{
  "username": "testuser",
  "password": "root"
}
```

返回:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "jwt-token",
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户"
    }
  }
}
```

### 1.3 员工登录（商家端）

- `POST /api/auth/employee/login`

请求:

```json
{
  "username": "admin",
  "password": "root"
}
```

返回:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "jwt-token",
    "employeeInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "roleCode": "SUPER_ADMIN"
    }
  }
}
```

### 1.4 获取当前登录信息

- `GET /api/auth/me`

返回:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "type": "EMPLOYEE",
    "username": "admin",
    "roleCode": "SUPER_ADMIN"
  }
}
```

## 2. 店铺模块 Shop（商家端）

### 2.1 获取店铺信息

- `GET /api/merchant/shop/{shopId}`

### 2.2 修改店铺信息

- `PUT /api/merchant/shop/{shopId}`

请求:

```json
{
  "shopName": "悦食汇总店",
  "address": "XX大学商业街1号",
  "phone": "13800000000",
  "notice": "欢迎下单"
}
```

### 2.3 切换营业状态

- `PATCH /api/merchant/shop/{shopId}/business-status`

请求:

```json
{
  "businessStatus": 1
}
```

## 3. 菜品分类模块 DishCategory（商家端）

### 3.1 分类列表

- `GET /api/merchant/categories?shopId=1`

### 3.2 新增分类

- `POST /api/merchant/categories`

请求:

```json
{
  "shopId": 1,
  "categoryName": "套餐",
  "sort": 4,
  "status": 1
}
```

### 3.3 修改分类

- `PUT /api/merchant/categories/{id}`

### 3.4 删除分类

- `DELETE /api/merchant/categories/{id}`

备注：分类下有菜品时建议禁止删除。

## 4. 菜品模块 Dish（商家端 + 用户端）

### 4.1 商家端分页查询菜品

- `GET /api/merchant/dishes?page=1&pageSize=10&shopId=1&categoryId=&dishName=&status=`

### 4.2 新增菜品

- `POST /api/merchant/dishes`

请求:

```json
{
  "shopId": 1,
  "categoryId": 1,
  "dishName": "黑椒鸡排饭",
  "price": 22.0,
  "imageUrl": "",
  "description": "新品",
  "stock": 100,
  "status": 1
}
```

### 4.3 修改菜品

- `PUT /api/merchant/dishes/{id}`

### 4.4 删除菜品

- `DELETE /api/merchant/dishes/{id}`

### 4.5 菜品上下架

- `PATCH /api/merchant/dishes/{id}/status`

请求:

```json
{
  "status": 0
}
```

### 4.6 用户端菜品列表（按店铺/分类）

- `GET /api/user/dishes?shopId=1&categoryId=1`

备注：仅返回上架菜品，且店铺营业时可下单。

## 5. 员工模块 Employee（商家端）

### 5.1 员工分页列表

- `GET /api/merchant/employees?page=1&pageSize=10&shopId=1&realName=&enabled=`

### 5.2 新增员工

- `POST /api/merchant/employees`

请求:

```json
{
  "username": "staff01",
  "password": "123456",
  "realName": "张三",
  "phone": "13900000001",
  "shopId": 1,
  "roleId": 3,
  "enabled": 1
}
```

### 5.3 修改员工

- `PUT /api/merchant/employees/{id}`

### 5.4 删除员工

- `DELETE /api/merchant/employees/{id}`

### 5.5 启用/禁用员工

- `PATCH /api/merchant/employees/{id}/enabled`

请求:

```json
{
  "enabled": 0
}
```

### 5.6 查询角色列表

- `GET /api/merchant/roles`

## 6. 购物车模块 Cart（用户端）

### 6.1 查询购物车

- `GET /api/user/cart?userId=1`

备注：生产实现建议从 token 取 `userId`，而不是前端传参。

### 6.2 加入购物车

- `POST /api/user/cart/items`

请求:

```json
{
  "dishId": 1,
  "quantity": 2
}
```

### 6.3 修改购物车数量

- `PUT /api/user/cart/items/{id}`

请求:

```json
{
  "quantity": 3
}
```

### 6.4 删除购物车项

- `DELETE /api/user/cart/items/{id}`

### 6.5 勾选/取消勾选

- `PATCH /api/user/cart/items/{id}/selected`

请求:

```json
{
  "selected": 1
}
```

### 6.6 清空购物车

- `DELETE /api/user/cart/clear`

## 7. 订单模块 Order（用户端 + 商家端）

### 7.1 用户提交订单

- `POST /api/user/orders`

请求:

```json
{
  "shopId": 1,
  "remark": "少辣"
}
```

说明：后端根据购物车已勾选项生成 `orders + order_item`，并清理已下单购物车项。

返回:

```json
{
  "code": 200,
  "msg": "下单成功",
  "data": {
    "orderId": 1001,
    "orderNo": "YSH202604010001",
    "totalAmount": 36.0,
    "status": 0
  }
}
```

### 7.2 用户订单列表

- `GET /api/user/orders?page=1&pageSize=10&status=`

### 7.3 用户订单详情

- `GET /api/user/orders/{orderId}`

### 7.4 取消订单（未支付可取消）

- `PATCH /api/user/orders/{orderId}/cancel`

### 7.5 商家端订单列表

- `GET /api/merchant/orders?page=1&pageSize=10&shopId=1&status=`

### 7.6 商家接单

- `PATCH /api/merchant/orders/{orderId}/accept`

状态：`已支付 -> 已接单`

### 7.7 商家发起配送

- `PATCH /api/merchant/orders/{orderId}/delivery`

状态：`已接单 -> 配送中`

### 7.8 商家完成订单

- `PATCH /api/merchant/orders/{orderId}/finish`

状态：`配送中 -> 已完成`

## 8. 支付模块 Payment（用户端）

### 8.1 创建支付单（模拟）

- `POST /api/user/payments/create`

请求:

```json
{
  "orderId": 1001,
  "payChannel": "MOCK"
}
```

返回:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "paymentNo": "PAY202604010001",
    "payAmount": 36.0,
    "payStatus": 0
  }
}
```

### 8.2 支付成功回调（模拟）

- `POST /api/user/payments/mock-success`

请求:

```json
{
  "paymentNo": "PAY202604010001"
}
```

说明：更新 `payment_record.pay_status=1`，并更新 `orders.pay_status=1, status=1, pay_time=now()`。

### 8.3 查询支付状态

- `GET /api/user/payments/{paymentNo}/status`

## 9. 骑手模块 Rider（骑手端）

### 9.1 骑手登录

- `POST /api/auth/rider/login`

### 9.1.1 骑手注册

- `POST /api/auth/rider/register`

请求:

```json
{
  "username": "rider02",
  "password": "123456",
  "realName": "李骑手",
  "phone": "13900000010"
}
```

返回:

```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "riderId": 2
  }
}
```

请求:

```json
{
  "username": "rider01",
  "password": "123456"
}
```

返回:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "jwt-token",
    "riderInfo": {
      "id": 1,
      "username": "rider01",
      "realName": "张骑手",
      "enabled": 1
    }
  }
}
```

### 9.2 获取骑手信息

- `GET /api/rider/me`

返回:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "rider01",
    "realName": "张骑手",
    "phone": "13900000009",
    "workStatus": "ONLINE"
  }
}
```

### 9.3 切换接单状态

- `PATCH /api/rider/work-status`

请求:

```json
{
  "workStatus": "ONLINE"
}
```

说明：`ONLINE` 表示可接单，`OFFLINE` 表示暂停接单。

### 9.4 可接订单池

- `GET /api/rider/orders/dispatch?page=1&pageSize=10`

说明：仅返回已支付且未被骑手接单的订单。

### 9.4.1 履约状态机与错误码（接单 / 到店 / 取餐 / 送达）

骑手侧 `orders.status` 大致为：`1` 已支付（待抢单）→ `2` 骑手已接单 → `3` 已到店（取餐前后仍用 `3`，以时间字段区分）→ `4` 已送达。

后端约束（与数据库 `UPDATE … AND` 条件一致）：

- 仅 `status = 1` 且 `rider_id IS NULL` 时可抢单；**并发抢单仅一条成功**，其余请求根据情况返回 `409` 或 `422`。
- 到店：`2 → 3`，写入 `rider_arrive_shop_time`。
- 取餐：须已到店（`rider_arrive_shop_time` 非空），写入 `rider_pickup_time`；**禁止**跳过到店直接取餐。
- 送达：须已取餐（`rider_pickup_time` 非空），`3 → 4`，写入 `rider_delivered_time`、`finish_time`。

常见业务码（响应体中的 `code`，HTTP 状态与之一致映射）：

| `code` | 说明 |
|--------|------|
| `409` | 抢单冲突：订单已被**其他骑手**接单 |
| `422` | 当前状态不允许该操作（含顺序错误、非待抢单池接单失败等） |
| `403` | 非当前骑手的订单 |

同一骑手重复调用「接单」且订单已是本人接单成功：视为**幂等成功**（不报错）。

若订单已分配给骑手，商家端再走「已接单后的配送/完成」类操作会返回 `422`，避免覆盖骑手履约状态机（请在骑手端完成到店/取餐/送达）。

### 9.5 骑手接单

- `PATCH /api/rider/orders/{orderId}/accept`

返回:

```json
{
  "code": 200,
  "msg": "接单成功",
  "data": {
    "orderId": 1001,
    "status": 2
  }
}
```

### 9.6 骑手当前进行中订单

- `GET /api/rider/orders/current`

### 9.7 骑手到店

- `PATCH /api/rider/orders/{orderId}/arrive-shop`

### 9.8 骑手取餐

- `PATCH /api/rider/orders/{orderId}/pickup`

### 9.9 骑手送达

- `PATCH /api/rider/orders/{orderId}/delivered`

### 9.10 骑手历史订单

- `GET /api/rider/orders/history?page=1&pageSize=20`
- `Authorization: Bearer <rider_token>`

说明：返回该骑手已完成（status=4）的历史配送记录，按送达时间倒序。

返回:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1001,
        "orderNo": "YSH202604010001",
        "totalAmount": 36.00,
        "shippingAddress": "XX大学3号楼",
        "dishSummary": "黑椒鸡排饭、可乐",
        "riderDeliveredTime": "2026-04-01 14:30:00",
        "createTime": "2026-04-01 13:00:00"
      }
    ],
    "total": 15,
    "page": 1,
    "pageSize": 20
  }
}
```

### 9.11 骑手位置上报（可选增强）

- `POST /api/rider/location/report`

请求:

```json
{
  "orderId": 1001,
  "lat": 31.2304,
  "lng": 121.4737,
  "accuracy": 15.2,
  "reportedAt": "2026-04-26 13:30:00"
}
```

## 10. 状态码与业务规则建议

- `200`：成功
- `400`：参数错误
- `401`：未登录 / Token 缺失 / Token 无法解析或已失效
- `403`：无权限（含跨端 Token、以及商家端角色不足）
- `404`：资源不存在
- `409`：冲突（如并发抢单失败、购物车与下单并发冲突等）
- `422`：状态或业务前置条件不满足（如履约顺序错误、骑手订单误走商家端流程）
- `500`：服务器错误

关键业务规则：

- 店铺打烊时禁止下单
- 菜品下架或库存不足时禁止加入购物车
- 未支付订单才能取消
- 订单状态流转必须按顺序，不允许跳状态
- 骑手接单需满足骑手状态为 `ONLINE`
- 同一订单只允许一个骑手接单成功，其余请求返回业务冲突
- 骑手端状态流转建议：`已支付 -> 骑手已接单 -> 骑手到店 -> 已取餐 -> 已送达 -> 已完成`

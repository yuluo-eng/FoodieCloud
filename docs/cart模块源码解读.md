# cart 模块源码解读

本文档按照「先看实体类，再看 DTO、Controller、Service、Mapper、XML」的顺序，梳理 `cart` 购物车模块的代码逻辑。写法偏向源码学习笔记，目标是说明每一层代码在项目里承担什么职责，以及它们之间如何协作。

---

## 1. 模块整体定位

`cart` 包负责用户端购物车功能。用户在点餐页面把菜品加入购物车，修改数量、勾选或取消勾选，最后下单时订单模块读取购物车中已勾选的条目生成订单。

整体调用链可以理解为：

```text
前端 UserHomeView
  -> CartController 接收 HTTP 请求
  -> CartService / CartServiceImpl 处理业务逻辑
  -> CartMapper 调用 MyBatis SQL
  -> cart_item 数据表

下单时：
OrderServiceImpl
  -> CartMapper.listSelectedCartForOrder
  -> 读取 selected=1 的购物车项
  -> 生成订单后清理已结算购物车项
```

也就是说，`cart` 模块不是孤立存在的，它是「用户点餐」和「订单生成」之间的中间层。

---

## 2. `CartItem`：购物车实体类

文件：

```text
src/main/java/com/example/springbootblank/cart/entity/CartItem.java
```

`CartItem` 是实体类，对应数据库中的 `cart_item` 表。一条 `CartItem` 记录表示：

> 某个用户把某道菜加入购物车，数量是多少，加购时单价是多少，以及当前是否被勾选用于结算。

主要字段含义：

| 字段 | 含义 |
|------|------|
| `id` | 购物车记录主键 |
| `userId` | 用户 ID，对应当前登录顾客 |
| `dishId` | 菜品 ID |
| `quantity` | 加购数量 |
| `unitPrice` | 加购时的菜品单价快照 |
| `selected` | 是否勾选，`1` 表示参与结算，`0` 表示暂不结算 |
| `createTime` | 创建时间 |
| `updateTime` | 更新时间 |

这里的 `unitPrice` 要特别注意。它不是实时查询菜品表的价格，而是用户加入购物车时保存下来的单价快照。这样即使商家后来修改菜品价格，购物车中已有条目的价格也不会被突然改变。

`CartItem` 中大量的 `get/set` 方法，是为了让 MyBatis 或其他 Java 代码可以读取和设置这些字段。可以把它理解为 Java 代码中对 `cart_item` 表结构的表达。

---

## 3. DTO：前端请求体的格式规范

目录：

```text
src/main/java/com/example/springbootblank/cart/dto/
```

这个包下有三个 DTO：

```text
CartAddRequest
CartQuantityUpdateRequest
CartSelectedUpdateRequest
```

一开始容易把 DTO 理解成「后端发给数据库的信息格式」，但在当前项目中更准确的理解是：

> DTO 用来规范前端发送给后端 Controller 的 JSON 请求体格式。

它主要服务于 HTTP 接口，而不是直接服务于数据库。

### 3.1 `CartAddRequest`

```java
public record CartAddRequest(Long dishId, Integer quantity) {}
```

用于添加购物车：

```http
POST /api/user/cart
```

前端请求体大致是：

```json
{
  "dishId": 10,
  "quantity": 1
}
```

后端 Controller 用 `@RequestBody CartAddRequest req` 接收，Spring 会自动把 JSON 转成这个 record。

### 3.2 `CartQuantityUpdateRequest`

```java
public record CartQuantityUpdateRequest(Integer quantity) {}
```

用于修改某个菜品在购物车中的数量：

```http
PUT /api/user/cart/{dishId}
```

请求体：

```json
{
  "quantity": 3
}
```

### 3.3 `CartSelectedUpdateRequest`

```java
public record CartSelectedUpdateRequest(Integer selected) {}
```

用于修改购物车项是否被勾选：

```http
PATCH /api/user/cart/{dishId}/selected
```

请求体：

```json
{
  "selected": 1
}
```

其中 `selected=1` 表示参与结算，`selected=0` 表示暂不参与结算。

### 3.4 为什么需要 DTO

虽然项目使用了 MyBatis，但 MyBatis 主要解决的是 Java 对象和数据库之间的映射问题。DTO 解决的是前端 JSON 和后端接口参数之间的约定问题。

分层关系可以写成：

```text
前端 JSON
  -> DTO
  -> Service 业务逻辑
  -> Entity / Mapper
  -> 数据库
```

使用 DTO 的好处：

| 好处 | 说明 |
|------|------|
| 请求格式清晰 | 一看 DTO 就知道接口需要哪些字段 |
| 避免前端乱传字段 | 加购物车只需要 `dishId` 和 `quantity`，不需要前端传 `userId`、`unitPrice` |
| 与数据库结构解耦 | 数据库表字段变化时，不一定影响前端请求格式 |
| 便于校验 | Service 可以直接检查 `req.dishId()`、`req.quantity()` 是否为空或非法 |

所以 DTO 不是多余的，它是 Controller 层和前端之间的契约。

---

## 4. `CartController`：购物车接口入口

文件：

```text
src/main/java/com/example/springbootblank/cart/controller/CartController.java
```

Controller 主要负责对外暴露接口。它本身不写复杂业务，而是把请求转交给 Service 层。

接口前缀：

```java
@RequestMapping("/api/user/cart")
```

说明这些接口都是用户端购物车接口。

主要接口：

| 方法 | 路径 | 含义 |
|------|------|------|
| `GET` | `/api/user/cart` | 查询当前用户购物车 |
| `POST` | `/api/user/cart` | 添加菜品到购物车 |
| `PUT` | `/api/user/cart/{dishId}` | 修改某个菜品数量 |
| `DELETE` | `/api/user/cart/{dishId}` | 删除某个菜品 |
| `PATCH` | `/api/user/cart/{dishId}/selected` | 勾选或取消勾选 |
| `DELETE` | `/api/user/cart/clear` | 清空购物车 |

Controller 的职责可以总结为：

1. 定义购物车模块有哪些功能。
2. 接收前端传来的请求头和请求体。
3. 把 DTO 传给 Service。
4. 使用 `ApiResponse.ok(...)` 返回统一格式。

例如添加购物车：

```java
@PostMapping
public ApiResponse<Void> add(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestBody CartAddRequest req
) {
    cartService.addToCart(authorization, req);
    return ApiResponse.ok();
}
```

这段代码的意思是：前端请求 `/api/user/cart`，Controller 取出 Token 和请求体，然后调用 `cartService.addToCart(...)`，真正逻辑交给 Service。

---

## 5. `CartService` 与 `CartServiceImpl`：业务逻辑层

文件：

```text
src/main/java/com/example/springbootblank/cart/service/CartService.java
src/main/java/com/example/springbootblank/cart/service/CartServiceImpl.java
```

`CartService` 是接口，定义购物车应该具备哪些能力。

`CartServiceImpl` 是实现类，真正完成业务逻辑。

### 5.1 为什么要有接口和实现类

在这个项目里，`CartService` 先声明能力：

```java
void addToCart(String authorization, CartAddRequest req);
void updateQuantity(String authorization, Long dishId, CartQuantityUpdateRequest req);
void deleteItem(String authorization, Long dishId);
```

`CartServiceImpl` 再写具体怎么做。

这样分层的好处是：

| 好处 | 说明 |
|------|------|
| 职责清晰 | Controller 面向接口调用，不关心实现细节 |
| 便于替换 | 以后可以换一种实现方式 |
| 便于测试 | 单测时可以 Mock Service 或 Mapper |

当前项目规模不算特别大，但这种写法符合 Spring 后端项目常见分层习惯。

---

## 6. `addToCart`：购物车最核心的逻辑

`CartServiceImpl` 里最核心的方法是：

```java
public void addToCart(String authorization, CartAddRequest req)
```

它的逻辑可以拆成几步。

### 6.1 解析当前用户

```java
Long userId = resolveUserId(authorization);
```

购物车必须属于某个登录用户，所以不能让前端直接传 `userId`。后端从 Token 中解析当前用户 ID。

这样可以避免前端伪造：

```json
{
  "userId": 999
}
```

也就是说，用户只能操作自己的购物车。

### 6.2 参数校验

```java
if (req.dishId() == null || req.quantity() == null || req.quantity() <= 0) {
    throw new BusinessException(400, "参数错误");
}
```

这里是防止非法请求，比如没有传菜品 ID，或者数量为 0、负数。

这类逻辑可以理解为「防呆设计」：提前识别错误输入，并返回明确错误，而不是让程序继续运行到数据库层才异常。

### 6.3 查询菜品是否存在

```java
Dish dish = dishMapper.findById(req.dishId());
if (dish == null) {
    throw new BusinessException(400, "菜品不存在或已下架");
}
```

这里先确认菜品 ID 是真实存在的。

### 6.4 判断菜品是否可售：`onSale`

```java
boolean onSale = dishMapper.listUserDishes(dish.getShopId(), null).stream()
        .anyMatch(d -> d.getId().equals(req.dishId()));
```

这里的 `onSale` 不是加锁，也不是并发控制。

它表示：

> 当前菜品是否出现在「用户端可售菜品列表」中。

`listUserDishes` 的 SQL 会过滤：

1. 菜品必须上架：`d.status = 1`
2. 店铺必须营业：`s.business_status = 1`
3. 菜品必须属于对应店铺：`d.shop_id = #{shopId}`

所以如果 `onSale` 是 `false`，可能原因是：

- 菜品不存在
- 菜品已下架
- 店铺已打烊
- 菜品所属店铺不符合用户端可售条件

这里说「复用用户端可售列表规则」，意思是加购时使用和用户浏览菜单一样的判断标准，避免出现「页面能看到但不能加购」或「页面看不到却能加购」的规则不一致。

### 6.5 判断购物车中是否已有这道菜

```java
CartItem exist = cartMapper.findByUserAndDish(userId, req.dishId());
```

这里查询的是：

> 当前用户的购物车中，是否已经有这道菜。

如果没有，就新增一条购物车记录。

如果已经有，就把数量累加。

### 6.6 新增购物车项

```java
CartItem item = new CartItem();
item.setUserId(userId);
item.setDishId(req.dishId());
item.setQuantity(req.quantity());
item.setUnitPrice(dish.getPrice());
item.setSelected(1);
cartMapper.insertCartItem(item);
```

这里注意两点：

1. `unitPrice` 来自菜品当前价格，作为加购时的价格快照。
2. `selected` 默认是 `1`，表示新加进来的菜默认参与结算。

### 6.7 已存在则累加数量

```java
cartMapper.updateCartQuantity(exist.getId(), exist.getQuantity() + req.quantity());
```

同一个用户同一道菜不会插入多行，而是更新原有记录数量。

这能避免购物车里出现：

```text
宫保鸡丁 x1
宫保鸡丁 x1
宫保鸡丁 x1
```

而是显示成：

```text
宫保鸡丁 x3
```

---

## 7. 其他 Service 方法

### 7.1 `listCart`

```java
public List<Map<String, Object>> listCart(String authorization)
```

查询当前用户购物车。它先解析用户 ID，再调用 Mapper 查询数据库。

### 7.2 `updateQuantity`

修改某道菜的数量。

关键校验：

1. 数量必须大于 0。
2. 购物车项必须存在。

如果不存在，返回 `404`。

### 7.3 `deleteItem`

删除某个菜品的购物车项。

### 7.4 `updateSelected`

修改勾选状态。

只允许：

```text
0：未勾选
1：已勾选
```

下单时只会处理 `selected=1` 的购物车项。

### 7.5 `clear`

清空当前用户全部购物车。

---

## 8. 「防呆设计」在这个模块里的体现

你提到的「防呆设计」可以理解为：在可能出错的位置提前校验，给出明确业务错误，避免系统进入不可控状态。

在 `cart` 模块中主要体现为：

| 场景 | 处理 |
|------|------|
| 没有 Token | 抛 `UnauthorizedException` |
| Token 不是用户类型 | 抛 `BusinessException(403, "无权限")` |
| `dishId` 为空 | 抛 `BusinessException(400, "参数错误")` |
| 数量小于等于 0 | 抛 `BusinessException(400, "数量必须大于0")` |
| 菜品不存在或不可售 | 抛 `BusinessException(400, "菜品不存在或已下架")` |
| 修改不存在的购物车项 | 抛 `BusinessException(404, "购物车项不存在")` |
| `selected` 不是 0/1 | 抛 `BusinessException(400, "selected 仅支持 0/1")` |

这些异常最终会被全局异常处理器转换成统一 JSON 响应，前端可以展示错误提示，而不是让后端直接抛出一堆堆栈信息。

---

## 9. `CartMapper`：Java 方法声明

文件：

```text
src/main/java/com/example/springbootblank/cart/mapper/CartMapper.java
```

`CartMapper` 是 MyBatis 的 Mapper 接口。它只声明方法，不直接写 SQL。

例如：

```java
CartItem findByUserAndDish(@Param("userId") Long userId, @Param("dishId") Long dishId);
```

这表示 Java 层有一个方法，功能是根据 `userId` 和 `dishId` 查询购物车项。

真正 SQL 在：

```text
src/main/resources/mapper/CartMapper.xml
```

MyBatis 会根据方法名和 XML 中的 `id` 进行绑定。

---

## 10. `CartMapper.xml`：SQL 语句

文件：

```text
src/main/resources/mapper/CartMapper.xml
```

这个文件负责把 `CartMapper` 中声明的方法映射成 SQL。

### 10.1 字段映射：`AS`

数据库字段通常是下划线风格：

```sql
dish_id
unit_price
```

Java / JSON 中常用驼峰风格：

```text
dishId
unitPrice
```

所以 SQL 中会写：

```sql
ci.dish_id AS dishId,
ci.unit_price AS unitPrice
```

`AS` 的作用是把数据库列名转换成前端或 Java 更容易使用的字段名。

### 10.2 `findByUserAndDish`

SQL：

```xml
<select id="findByUserAndDish" resultType="com.example.springbootblank.cart.entity.CartItem">
    SELECT id, user_id, dish_id, quantity, unit_price, selected, create_time, update_time
    FROM cart_item
    WHERE user_id = #{userId}
      AND dish_id = #{dishId}
</select>
```

含义是：

> 在 `cart_item` 表里查找当前用户是否已经添加过这道菜。

在 Service 中的使用：

```java
CartItem exist = cartMapper.findByUserAndDish(userId, req.dishId());
```

如果查不到，说明购物车还没有这道菜，于是新增。

如果查到了，说明已经有这道菜，于是累加数量。

### 10.3 Mapper 接口 + XML 的好处

这种写法不只是为了代码复用，也有分层和可维护的目的。

| 好处 | 说明 |
|------|------|
| SQL 与业务逻辑分离 | Service 里不直接拼 SQL，代码更清晰 |
| 方法声明清晰 | Java 接口告诉你有哪些数据库操作 |
| XML 适合复杂 SQL | 联表、动态条件更好写 |
| 便于测试 | 单元测试可以 Mock `CartMapper` |
| 便于维护 | SQL 改动集中在 Mapper XML 中 |

固定关系是：

```text
CartMapper.java 声明方法
CartMapper.xml 写 SQL，id 与方法名一致
CartServiceImpl 调用 cartMapper.方法名(...)
```

---

## 11. 购物车和订单模块的衔接

购物车模块不直接生成订单。真正生成订单的是：

```text
src/main/java/com/example/springbootblank/order/service/OrderServiceImpl.java
```

下单时会调用：

```java
cartMapper.listSelectedCartForOrder(userId)
```

该 SQL 查询：

1. 当前用户的购物车
2. 只查 `selected=1` 的条目
3. 联表 `dish` 查出菜品名称和 `shopId`

为什么要带 `shopId`？

因为多店铺场景下，订单必须属于某一家店。下单时需要校验：

1. 购物车中勾选的菜品必须来自同一家店。
2. 购物车菜品所属店铺必须和前端传来的 `shopId` 一致。

如果不校验，就可能出现：

```text
用户在 1 店加菜，又在 2 店加菜，却提交成 1 店订单
```

所以购物车模块提供数据，订单模块负责最终业务约束。

下单成功后，会调用：

```java
cartMapper.clearSelectedCart(userId)
```

只删除已勾选并结算的购物车项，未勾选的项继续保留。

---

## 12. 看完 `cart` 后还应该看哪里

为了把购物车放到完整业务链路中理解，建议继续看：

| 顺序 | 文件 | 重点 |
|------|------|------|
| 1 | `order/service/OrderServiceImpl.java` | `createOrder` 如何读取购物车并生成订单 |
| 2 | `resources/mapper/CartMapper.xml` | `listSelectedCartForOrder` 为什么带 `shopId` |
| 3 | `dish/mapper/DishMapper.xml` | `listUserDishes` 如何判断用户端可售菜品 |
| 4 | `frontend/src/views/user/UserHomeView.vue` | 前端如何调用购物车接口 |
| 5 | `common/api/ApiResponse.java` | 后端统一返回格式 |
| 6 | `common/web/GlobalExceptionHandler.java` | 业务异常如何变成前端可读响应 |
| 7 | `auth/security/JwtService.java` | Token 如何解析出当前用户 |
| 8 | `payment/service/PaymentServiceImpl.java` | 支付成功后如何扣库存 |

推荐顺序：

```text
CartItem
  -> DTO
  -> CartController
  -> CartServiceImpl
  -> CartMapper / CartMapper.xml
  -> OrderServiceImpl.createOrder
```

这样可以把「加购物车 -> 勾选 -> 下单 -> 支付」的前半段完整串起来。

---

## 13. 总结

`cart` 模块可以概括为：

> Controller 接收前端购物车请求，DTO 规范请求格式，Service 做登录校验和业务规则判断，Mapper 负责数据库读写，OrderService 在下单时消费已勾选的购物车项。

几个关键理解点：

1. `CartItem` 是数据库表 `cart_item` 的 Java 实体表达。
2. DTO 不是给数据库用的，而是规范前端传给后端的请求体。
3. Controller 是接口入口，不负责复杂业务。
4. `CartServiceImpl` 是业务核心，尤其是 `addToCart`。
5. `onSale` 是可售判断，不是加锁。
6. `selected` 决定下单时哪些购物车项会被结算。
7. Mapper 接口声明方法，XML 负责具体 SQL。
8. 购物车不直接生成订单，订单模块会读取已勾选项并校验同店铺。


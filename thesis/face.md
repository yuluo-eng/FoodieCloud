## 本科中期答辩问题清单（按难度渐进）

> 建议准备时：先自己口头回答，再根据项目代码对照补充细节。

### 一、基础概念与整体理解

1. **请你用自己的话介绍一下这个“悦食汇”点餐系统的整体功能和技术栈？前端、后端分别用了什么技术，为什么这么选？**
   前端使用vue，后端使用springboot，数据库使用了mysql，都是当前各自领域主流的开发技术，不容易在配置问题上出错并且有很多技术文档。
2. **系统中用户端和商家端分别有哪些主要业务流程？（比如用户从点餐到支付的完整路径，以及商家从接单到完成发货的路径）**

用户端：登录，点餐，选择菜品分类，加入购物车，结算功能，订单生成功能，模拟支付功能。

商家端：工作台查看信息，员工管理，菜品管理，分类管理，店铺设置，订单管理。

1. **后端项目的包结构是如何划分的？例如 `auth / dish / order / payment / category / shop` 等包分别负责什么职责？**

一个包大概管理一个实体，分别是用户，菜品，点餐，支付，，这里需要你补充，我对这块不是很敏感。

1. **你在项目里是如何设计“统一接口返回格式”的？`ApiResponse` 这个类型的字段含义是什么，有哪些好处和局限？**

使用泛型，格式是<T>（code，msg,data）
好处是统一了接口返回格式便于协作开发以及在方便开发。

### 二、Spring Boot 与后端实现

5. **以“用户下单”为例，从前端点击“去结算”按钮开始，到数据库写入 `orders` 和 `order_item`，整个调用链路是怎样的？请说出关键的 Controller、Service、Mapper 和数据库表。**

```java
@RequestHeader(value = "Authorization", required = false) String authorization,
@RequestBody OrderCreateRequest req
```


收到前端的消息之后会返回下单成功的消息，之后转到orderserve里面去创建订单，之后因为serve作为impl的接口，所以复写了这个createOrder

```java
public Map<String, Object> createOrder(String authorization, OrderCreateRequest req) {
    Long userId = resolveUserId(authorization);
    Long shopId = req.shopId() == null ? 1L : req.shopId();

    List<Map<String, Object>> selected = cartMapper.listSelectedCartForOrder(userId);
    if (selected.isEmpty()) {
        throw new BusinessException(400, "购物车为空");
    }

    BigDecimal total = selected.stream()
            .map(item -> ((BigDecimal) item.get("unitPrice")).multiply(BigDecimal.valueOf((Integer) item.get("quantity"))))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Order order = new Order();
    order.setOrderNo(buildOrderNo());
    order.setUserId(userId);
    order.setShopId(shopId);
    order.setTotalAmount(total);
    order.setRemark(req.remark());
    order.setStatus(0);
    order.setPayStatus(0);

    orderMapper.insertOrder(order);

    for (Map<String, Object> item : selected) {
        BigDecimal unitPrice = (BigDecimal) item.get("unitPrice");
        Integer qty = (Integer) item.get("quantity");

        OrderItem oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setDishId(((Number) item.get("dishId")).longValue());
        oi.setDishName((String) item.get("dishName"));
        oi.setDishPrice(unitPrice);
        oi.setQuantity(qty);
        oi.setAmount(unitPrice.multiply(BigDecimal.valueOf(qty)));
        orderMapper.insertOrderItem(oi);
    }

    cartMapper.clearSelectedCart(userId);

    return Map.of(
            "orderId", order.getId(),
            "orderNo", order.getOrderNo(),
            "totalAmount", total,
            "status", order.getStatus()
    );
}
```


大意是新建一个order类，把数据放进去，当orderMapper.insertOrderItem(oi);的时候mybatis会去找对应同名文件里面的xml文件里面的sql语句，实现添加订单信息到mysql中。
龙：我看你这个订单号不错，怎么生成的？
Long shopId = req.shopId() == null ? 1L : req.shopId();
但是我目前还看不懂这段怎么就拼接日期时间了。

5. **你是如何在项目中实现 JWT 登录鉴权的？说明用户登录和员工登录的 token 生成、解析流程，以及如何在业务代码中使用 `JwtService` 和 `MerchantAuthGuard` 做权限控制。**

  这个登录鉴权功能实现在在auto软件包里面，现在已经将jwt登录鉴权的大概思路理清楚了，分为三个部分，一个是Header，存数据类型和使用的签名，一个是payload，存具体信息比如用户名什么的，一个是signature将上述两者拼接成Hander.payload，对其整体进行签名生成一个Jwttoken，所以最后的的格式是Hander.payload.signature
  在我们的项目里面呢，对于创建用户和商家端的登录token唯一的区别是在创建token的时候用户端少引入一个roleCode》》》

  登录时：用户或员工提交账号密码，服务端校验通过后，用 JJWT 和配置里的 secret 签发 JWT，里面放了 身份类型（用户还是员工）、用户 id、用户名，员工还会放 角色编码。Token 返回给前端存在本地，之后请求通过 Authorization: Bearer 带上。

  访问接口时：业务层用 `JwtService` 解析并验签，拿到主体信息。用户端接口会校验 token 里是 USER，再用里面的 userId 做购物车、订单；商家端除了验 EMPLOYEE，部分管理功能还用 `MerchantAuthGuard` 校验 roleCode，比如只有店长、超管才能改店铺、员工，普通员工只能操作菜品或看统计——具体按我们代码里配置的来。

  查当前用户信息用 `/auth/me`，验签后再查数据库确认账号没被禁用。

  整体上 JWT 不落库，扩展开销小；安全上要注意 HTTPS、secret 保密、token 过期时间，敏感权限还是以服务端校验为准。」

5. **在商家后台接口里，为什么有的地方使用 `requireEmployeeRole("SUPER_ADMIN", "SHOP_MANAGER")`，有的地方又允许 `"STAFF"`？你是怎么划分不同角色权限边界的？**
6. **请解释 MyBatis 在本项目中的使用方式：你是如何从 `Mapper` 接口到 `Mapper.xml` 再到 SQL 的？举一个你写过或修改过的查询/统计 SQL 作为例子。**

### 三、数据库设计与脏数据修复

9. **请说明 `orders`、`order_item`、`dish`、`dish_category` 这几张表之间的关系和各自的关键字段（主键、外键）？为什么订单明细需要冗余存储 `dish_name` 和 `dish_price`？**
10. **在修复“菜品名称 / 分类名称为 null 导致前端显示不正确”时，你使用了哪些 SQL？请解释这些 SQL 的思路，比如如何把无分类菜品归到一个有效分类、如何给空分类名和空菜品名赋默认值。**
11. **你是如何给历史订单补全 `order_no`、`order_item.dish_name` 和 `amount` 等字段的？这样做可能有哪些风险或注意事项？**

### 四、前端实现与联调问题

12. **在用户点餐页 `UserHomeView.vue` 中，菜品分类按钮和菜品列表是如何从接口数据渲染出来的？请详细说明 `categories`、`selectedCategory` 与 `filteredDishes` 之间的关系。**
13. **你曾经遇到过“分类按钮显示为 ‘分类1/分类2…’ 而不是真实名字”的问题，最终是如何定位并解决的？请从“前端调试（console.log）、接口返回字段、数据库数据”三个角度说明排查过程。**
14. **商家工作台最初为什么一直显示“今日订单=0 / 今日营收=0”？你是如何在前后端两侧补齐逻辑，让统计数据能够正确反映当天订单的？**

### 五、支付与状态流转

15. **请描述一下从“创建支付单”到“模拟支付成功，再到订单状态更新”的完整流程。涉及到哪些表（如 `payment_record`、`orders`），以及哪些关键字段会发生变化？**
16. **在支付成功后，订单状态和支付状态的组合有哪些可能值？例如待支付、已支付、已接单、配送中、已完成、已取消等，你是如何在代码中约束这些状态流转合法性的？**

### 六、异常处理与安全性

17. **项目中统一异常处理是如何设计的？`GlobalExceptionHandler` 里处理了哪些类型的异常，它们分别对应什么 HTTP 状态码和业务含义？**
18. **在处理鉴权失败（未登录或 token 无效）和业务错误（如购物车为空、无权限）时，你是如何区分并在前端给用户不同提示的？（可结合 `UnauthorizedException`、`BusinessException` 以及前端 axios 拦截器的逻辑来回答）**

### 七、扩展性与优化思考

19. **如果以后需要支持多家店铺（`shop_id` 不再只是 1），你认为目前的代码结构和数据库设计有哪些地方是已经支持的？还有哪些地方需要改造才能真正实现多店铺？**
20. **当前统计“今日订单”和“今日营收”的实现是按 `pay_time` 做 SQL 过滤的，如果将来订单量非常大或需要按不同维度统计（比如按小时、按店铺分组），你会怎么优化现有的统计实现？可以从索引、聚合表、缓存等角度谈你的想法。**
21. **如果让你再做一次重构，你觉得这个项目中哪一块的代码最需要重构？你打算如何拆分、抽象或者测试它？**


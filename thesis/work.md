## 项目进展与问题记录（work log）

> 项目：悦食汇点餐系统（spring-boot-blank）

### 一、已完成的核心功能

- **用户端点餐流程**
  - 用户登录/注册（`/api/auth/user/*`）打通。
  - 用户端菜品列表：`UserHomeView.vue` 调用 `/api/user/dishes?shopId=1`，展示按分类过滤的菜品卡片。
  - 购物车：`/api/user/cart` 增删改查、勾选、全选，本地实时更新数量与合计金额。
  - 下单：`POST /api/user/orders` 从购物车生成订单（`OrderServiceImpl#createOrder`），写入 `orders` 与 `order_item`。
  - 用户订单列表/详情：`UserOrdersView.vue` + `/api/user/orders` 和 `/api/user/orders/{orderId}`。

- **支付与订单状态流转**
  - 支付创建：`PaymentServiceImpl#create` 根据订单生成 `payment_record`，返回 `paymentNo`、金额与状态。
  - 模拟支付成功：`/api/user/payments/mock-success`，更新 `payment_record.pay_status=1`，并调用 `OrderMapper.updateOrderPaySuccess` 将订单置为已支付。
  - 用户端轮询支付状态：`/api/user/payments/{paymentNo}/status`。

- **商家端基础管理**
  - 员工登录：`/api/auth/employee/login`，生成带 `roleCode` 的员工 token。
  - 员工管理：列表、创建、编辑、启用/禁用等（角色校验通过 `MerchantAuthGuard`）。
  - 店铺信息管理：`/api/merchant/shop/{shopId}` 查看与编辑。
  - 菜品管理：`/api/merchant/dishes` 列表 + 新增/编辑/上下架/删除；支持图片上传。
  - 分类管理：`/api/merchant/categories` 列表 + 新增/编辑/删除。
  - 商家订单管理：`/api/merchant/orders` 列表与详情，以及接单/配送中/完成状态更新。

- **工作台（商家端）**
  - 新增 `MerchantHomeView.vue` 左侧菜单 + 工作台卡片 UI。
  - 新增 `MerchantDashboardController` 与接口 `/api/merchant/dashboard/stats`：
    - 今日订单数：统计 `orders` 中今日 `pay_time` 在本地 0:00–24:00 且 `pay_status=1` 的数量。
    - 今日营收：同口径 `SUM(total_amount)`。
    - 员工数：`employee` 表中当前店铺、`enabled=1` 的数量。
    - 菜品数：当前店铺、`status=1` 的菜品数量。
  - 工作台前端在 `onMounted` 时请求 `/auth/me` 获取当前员工信息，再调用 `/merchant/dashboard/stats` 渲染 4 张统计卡片。

### 二、主要踩坑与解决方案

#### 1. 菜品/分类脏数据导致前端看不到菜品

- **现象**
  - 用户点餐页分类按钮有多个，但列表总是显示“暂无菜品”。
  - 调试接口 `/api/user/dishes` 时发现返回的 `dishName`、`categoryId` 等字段中有 `null`。
  - 分类接口 `/api/user/categories` 返回的 `categoryName` 为 `null`，导致前端显示“分类1 / 分类2 …”。

- **后端分析**
  - 实体命名：
    - 菜品实体 `Dish` 使用 `dishName` 字段，对应数据库 `dish.dish_name`。
    - 分类实体 `DishCategory` 使用 `categoryName`，对应 `dish_category.category_name`。
  - `DishMapper.listUserDishes` 已正确映射 `dish_name AS dishName`，并按 `status=1`、店铺营业状态过滤。
  - `CategoryMapper.listByShopId` 的 SQL 为：
    ```sql
    SELECT id, shop_id, category_name, sort, status
    FROM dish_category
    WHERE shop_id = #{shopId}
    ORDER BY sort ASC, id ASC;
    ```
  - `CategoryServiceImpl.userList` 将 `category_name` 映射为 `categoryName` 并保留 `sort/status`。
  - 用户端 `UserHomeView.vue` 中：
    - 调用 `/user/dishes` 获取菜品。
    - 调用 `/user/categories` 获取分类，并映射：
      ```js
      const remoteCategories = (catRes.data.data || [])
        .filter(c => Number(c.status) === 1)
        .map(c => {
          const id = Number(c.id)
          const rawName = String(c.categoryName ?? '').trim()
          const sort = Number(c.sort ?? 0)
          return { id, name: rawName || `分类${id}`, sort }
        })
      categories.value = remoteCategories.sort((a, b) => a.sort - b.sort || a.id - b.id)
      ```
    - 如果 `categoryName` 为空，就会兜底显示 `分类${id}`。

- **问题根因**
  - 数据库中 `dish_category.category_name` 对于 shopId=1 的 1–5 号分类全部为 `NULL`。
  - 订单明细 `order_item` 里早期数据的 `dish_name`、`dish_price` 等字段也有 `NULL`，导致“我的订单”页商品名与金额缺失。

- **解决方案**
  - 一次性修复分类/菜品/订单的脏数据（建议在 `yueshihui` 数据库执行）：
    - 补齐分类名：
      ```sql
      UPDATE dish_category
      SET category_name = CONCAT('分类', id)
      WHERE category_name IS NULL OR category_name = '';
      ```
      或改成业务需要的中文名（主食/小吃/饮品等）。
    - 修正菜品的 `shop_id` / `category_id` 指向、补齐 `dish_name`。
    - 回填 `order_item.dish_name/dish_price/amount`，确保订单详情有完整的菜品快照。
  - 前端侧保留 `rawName || '分类${id}'` 的兜底逻辑，以防后续新增脏数据；同时在 `console.log` 中调试确认 `categoryName` 字段是否为空。

#### 2. 订单详情里 dishName 为 null、我的订单不显示菜品名与金额

- **现象**
  - 用户“我的订单”页面里列表有订单，但详情中菜品名称和单个金额为空。
  - 调后端 `/api/user/orders/{orderId}`，返回的 `items[i].dishName` 为 `null`。

- **后端分析**
  - 下单逻辑：
    - `OrderServiceImpl#createOrder` 从购物车获取数据：
      ```java
      List<Map<String, Object>> selected = cartMapper.listSelectedCartForOrder(userId);
      ...
      oi.setDishId(((Number) item.get("dishId")).longValue());
      oi.setDishName((String) item.get("dishName"));
      oi.setDishPrice(unitPrice);
      oi.setAmount(unitPrice.multiply(BigDecimal.valueOf(qty)));
      ```
    - `CartMapper.listSelectedCartForOrder` 里，`dishName` 来自 `dish.dish_name`。
  - 订单详情展示：
    - `OrderMapper.listOrderItems` 从 `order_item` 读取 `dish_name AS dishName`、`dish_price AS dishPrice`、`amount`。

- **问题根因**
  - 早期插入的菜品数据中 `dish.dish_name` 就是空的，后续下单时写入 `order_item.dish_name` 也为空。
  - 订单明细没有二次从菜品表 join 回填，因此历史订单里的名称和金额都缺失。

- **解决方案**
  - SQL 回填订单明细：
    ```sql
    UPDATE order_item oi
    JOIN dish d ON oi.dish_id = d.id
    SET
      oi.dish_name = COALESCE(NULLIF(oi.dish_name, ''), d.dish_name),
      oi.dish_price = COALESCE(oi.dish_price, d.price),
      oi.amount = CASE
        WHEN oi.amount IS NULL OR oi.amount = 0
          THEN COALESCE(oi.dish_price, d.price) * oi.quantity
        ELSE oi.amount
      END
    WHERE
      oi.dish_id IS NOT NULL
      AND (
        oi.dish_name IS NULL OR oi.dish_name = ''
        OR oi.dish_price IS NULL
        OR oi.amount IS NULL OR oi.amount = 0
      );
    ```
  - 补齐 `orders.order_no`，确保订单号展示正常：
    ```sql
    UPDATE orders
    SET order_no = CONCAT('YSH', DATE_FORMAT(COALESCE(create_time, NOW()), '%Y%m%d%H%i%s'), LPAD(id, 8, '0'))
    WHERE order_no IS NULL OR order_no = '';
    ```

#### 3. 工作台统计一直为 0、订单/营收不更新

- **现象**
  - 商家端“工作台”四个卡片（今日订单/今日营收/员工数/菜品数）始终显示 0。
  - 即便已模拟支付订单、创建了多条记录，统计仍不变。

- **问题根因**
  - `MerchantHomeView.vue` 初版只在 `onMounted` 中调用 `/auth/me`，**根本没有调用任何统计接口**，`stats` 永远是初始的 0 值。
  - 后端也没有专门的 Dashboard 统计接口。

- **解决方案**
  - 新增后端统计 Controller 与 Mapper 方法：
    - `MerchantDashboardController` 的 `/api/merchant/dashboard/stats`：
      - 使用 `MerchantAuthGuard.requireEmployeeRole` 校验员工身份，并通过 `AuthMapper.findEmployeeByIdWithRole` 找到当前员工所属 `shopId`。
      - 计算今日时间段：
        ```java
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDate today = LocalDate.now(zone);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        ```
      - 调用 `OrderMapper.countMerchantOrdersPaidToday` 与 `sumMerchantRevenuePaidToday` 统计订单与营收。
      - 调用 `EmployeeMapper.countEmployees(shopId, null, 1)` 统计启用员工数。
      - 调用 `DishMapper.countMerchantDishes(shopId, null, null, 1)` 统计上架菜品数。
    - 在 `OrderMapper.java` 中声明新方法，并在 `OrderMapper.xml` 中实现相应 SQL。
  - 前端 `MerchantHomeView.vue` 修改：
    - `onMounted` 里先请求 `/auth/me` 获取员工信息，失败则跳转登录。
    - 然后单独 `try/catch` 调用 `/merchant/dashboard/stats`，成功时写入 `stats`，失败时仅在控制台打印，不影响页面其它功能。

#### 4. 商家端频繁 403（无权限）

- **现象**
  - 调用如 `/api/merchant/employees`、`/api/merchant/categories`、`/api/merchant/shop/1` 等接口时，经常出现 403，前端提示“无权限”。

- **根因**
  - 权限设计中，不同接口只允许特定角色访问：
    - 员工/店铺/分类管理：`MerchantAuthGuard.requireEmployeeRole(..., "SUPER_ADMIN", "SHOP_MANAGER")`。
    - 菜品管理和 dashboard 统计：允许 `"SUPER_ADMIN", "SHOP_MANAGER", "STAFF"`。
  - 当使用 `STAFF` 账号登录时，访问只允许管理/店长角色的接口会被拒绝。

- **解决方案**
  - 明确区分不同角色的使用场景：
    - 日常看工作台、处理订单：可以用 `STAFF`。
    - 管理员工/配置店铺/增删分类菜品：使用 `SUPER_ADMIN` 或 `SHOP_MANAGER`。
  - 如需调试方便，可以在 `MerchantAuthGuard` 的测试兜底逻辑中让 `admin` 账号直接放行（项目中已经实现）。

#### 5. 编译错误与小问题修复

- **`User` 实体类语法错误**
  - 在 `auth/entity/User.java` 中，`getStatus` 方法后误插入了字符串：
    ```java
    public Integer getStatus() {
        return status;
    }xian z
    ```
  - 造成 Maven 编译失败：`';' expected`。
  - 修复：删除多余字符，保留正常方法结尾。

- **Maven 在沙箱中无法访问全局 ~/.m2 仓库与外网**
  - 在当前环境下构建时，遇到本地仓库权限与网络限制问题。
  - 处理方式：将 Maven 本地仓库切换到项目内（`-Dmaven.repo.local=./.m2/repository`），并按需调整依赖下载策略（本地开发时你可以直接在宿主机正常执行 `mvn`）。

### 三、经验总结

- **数据优先清理**：前端经常出现的“字段为 null / 名称不显示 / 价格为 0”，多数是后端数据脏（例如 `category_name/dish_name` 为空、订单明细未回填）。通过一套集中 SQL “修复脏数据”远比在前端堆兜底逻辑可靠。
- **接口职责清晰**：工作台统计一开始没有单独接口，导致前端只能显示常量 0。增加 `MerchantDashboardController` 之后，统计逻辑集中且可测试。
- **权限粒度明确**：使用 `MerchantAuthGuard` + `roleCode` 控制接口访问时，要清晰区分仅限管理员（员工/店铺/分类管理）与普通员工可用（接单/发货/查看统计）的接口。
- **前后端字段对齐**：这次分类名称的问题说明，前后端字段名的契约（如 `categoryName`）必须和数据库/SQL 保持一致，否则前端会拿到 `null`，再被兜底逻辑掩盖真实问题。


# 开发日志

---

# 第 1 批：后端安全 + 库存闭环

涉及 Issues: #3, #4, #5, #6, #7, #8

---

## #3 + #4 支付成功后扣减库存 & 并发控制

**改动文件**：
- `DishMapper.java` / `DishMapper.xml` — 新增 `deductStock`、`findById`
- `PaymentServiceImpl.java` — 支付成功后遍历 order_item 逐条扣减

**核心 SQL**：
```sql
UPDATE dish SET stock = stock - #{quantity}
WHERE id = #{dishId} AND stock >= #{quantity}
```

**行为说明**：
- 扣减时机：`mockSuccess()` 事务内，在 `updateOrderPaySuccess` 之后
- 并发安全：条件更新 `stock >= #{quantity}`，受影响行数为 0 时抛出库存不足异常
- 事务回滚：任一菜品库存不足时整个事务回滚（支付状态、订单状态、库存均不变）

---

## #5 商家端鉴权统一收口

**改动文件**：
- `MerchantAuthGuard.java` — 新增 `resolveShopId()`、`requireShopAccess()`
- `OrderServiceImpl.java` — 注入 `MerchantAuthGuard`，移除重复的 `resolveEmployee` / `ensureMerchant` 方法

**行为说明**：
- 所有商家端订单操作统一走 `MerchantAuthGuard` 鉴权
- Token 解析逻辑不再散落在多个 Service 中

---

## #6 商家订单接口店铺隔离校验

**改动文件**：
- `MerchantAuthGuard.java` — `resolveShopId()` 从 DB 查员工所属 shopId
- `OrderServiceImpl.java` — 商家订单列表/详情/接单/配送/完成全部验证 shopId

**行为说明**：
- `merchantOrders` / `merchantOrderDetail`：调用 `requireShopAccess(auth, requestedShopId)`
- `acceptOrder` / `deliveryOrder` / `finishOrder`：调用 `resolveShopId()` + `ensureOrderBelongsToShop()`
- 跨店访问返回 403

---

## #7 上传模块安全增强

**改动文件**：
- `UploadController.java` — 商家端：5MB 限制 + MIME 白名单
- `UserUploadController.java` — 用户端：2MB 限制 + MIME 白名单

**校验顺序**：文件大小 → MIME Content-Type → 文件扩展名（三重校验）

---

## #8 菜品图片替换后旧文件清理

**改动文件**：
- `DishServiceImpl.java` — `updateDish()` 中比较新旧 imageUrl，不同则异步删除旧文件

**行为说明**：
- 删除失败只记 warn 日志，不阻断主流程
- 通过 `Files.deleteIfExists()` 实现安全删除

---

# 第 2 批：审计日志 + 前端基础设施

涉及 Issues: #9, #12, #13, #14

---

## #9 操作审计日志

**新增文件**：
- `log/entity/OperationLog.java` — 日志实体
- `log/mapper/OperationLogMapper.java` + `OperationLogMapper.xml` — MyBatis 插入
- `log/service/OpLogService.java` — `@Async` 异步写入，失败仅 warn 不阻断业务
- `docs/patch-operation-log.sql` — 建表 DDL

**改动文件**：
- `SpringBootBlankApplication.java` — 添加 `@EnableAsync`、`log.mapper` 扫描路径
- `AuthServiceImpl.java` — 用户/员工/骑手登录后记录日志
- `PaymentServiceImpl.java` — 支付成功后记录日志
- `OrderServiceImpl.java` — 商家订单状态变更（接单/配送/完成）记录日志
- `DishServiceImpl.java` — 菜品新增/修改/删除记录日志

**日志字段**：`operator_type`、`operator_id`、`module`、`action`、`content`、`ip`、`create_time`

---

## #12 Toast 通知组件

**新增文件**：
- `frontend/src/composables/useToast.js` — 响应式 toast 队列，支持 `success` / `error` / `info` / `warn`，自动定时消失
- `frontend/src/components/ToastContainer.vue` — Teleport 到 body，四种颜色主题，带进出动画

**改动文件**：
- `App.vue` — 挂载 `<ToastContainer />`
- 6 个 Vue 文件中所有 `alert()` 全部替换为 `toast.xxx()`（约 20 处）：
  - `UserOrdersView.vue`、`UserHomeView.vue`
  - `MerchantMenuView.vue`、`MerchantOrdersView.vue`、`ShopSettingsView.vue`
  - `RiderHomeView.vue`

---

## #13 错误码映射

**改动文件**：
- `frontend/src/api/request.js`

**行为说明**：
- 新增 `HTTP_STATUS_MAP` 常量，覆盖 400/403/404/409/422/500/502/503 等常见状态码
- 拦截器增加超时（`ECONNABORTED` → "请求超时"）和断网（无 response → "网络连接失败"）处理
- 业务错误优先取后端 `msg`，兜底走 status 映射

---

## #14 404 错误页

**新增文件**：
- `frontend/src/views/NotFoundView.vue` — 渐变大号 404、描述文案、返回首页按钮

**改动文件**：
- `frontend/src/router/index.js` — 添加 `/:pathMatch(.*)*` catch-all 路由

---

# 第 3 批：前端体验 + 骑手完善

涉及 Issues: #21, #24, #15

---

## #21 骑手登录态持久化完善

**改动文件**：
- `stores/auth.js` — `refreshRiderProfile()` 优先调用 `/rider/me`（保留 `workStatus` / `enabled`），失败再 fallback `/auth/me`
- `main.js` — 应用启动时从 localStorage 恢复 token 并调用 `setAuthHeader`

**行为说明**：
- Token / Profile 存 localStorage，刷新不丢失（已有逻辑）
- 路由守卫已支持 `requiresRider` + `guestOnly: 'rider'`
- 新增：页面刷新后全局 Axios Authorization header 自动恢复

---

## #24 移动端交互优化

**改动文件**：
- `frontend/index.html` — viewport 增加 `maximum-scale=1.0, user-scalable=no, viewport-fit=cover`；添加 `theme-color`、PWA meta
- `frontend/src/style.css` — safe-area padding、`min-height: 44px` 触控规范、`overscroll-behavior`、spinner / skeleton 动画、`@media (max-width: 640px)` 断点
- `RiderHomeView.vue` — tabs flex-1 等宽、cards 滚动容器、actions 按钮自适应宽度、小屏 header 纵向排列
- `UserHomeView.vue` — 顶栏 safe-area-inset、购物车按钮/数量按钮增大至 36px、购物车底栏 safe-area、小屏隐藏身份标签
- `MerchantOrdersView.vue` — 小屏 actions 自动换行

---

## #15 联调验收清单

**新增文件**：
- `docs/checklist.md` — 按用户故事列出 6 大类约 50 项验收条目（用户端、商家端、骑手端、跨端、审计日志、移动端体验）

---

# 第 4 批：测试 + DevOps 文档

涉及 Issues: #10, #11, #25, #26

---

## #10 补齐单元测试

**改动文件**：
- `OrderServiceImplTest.java` — 补充 `MerchantAuthGuard` / `OpLogService` mock；新增商家接单成功 + 日志验证、跨店隔离 403 用例
- `PaymentServiceImplTest.java` — 补充 `DishMapper` / `OpLogService` mock；新增支付成功+库存扣减、库存不足回滚用例
- `RiderOrderFulfillmentServiceTest.java` — 补充 `MerchantAuthGuard` / `OpLogService` mock；修复 merchantDelivery 测试适配新的 shop 校验逻辑
- `MerchantAuthGuardTest.java` — 补充 `AuthMapper` mock，适配构造器变更

**测试结果**：27 tests, 0 failures, 0 errors

---

## #11 关键接口集成测试

**新增文件**：
- `OrderFlowIntegrationTest.java` — 全链路模拟测试
  - `fullOrderFlowWithoutRider`：下单 → 支付+扣库存 → 商家接单 → 配送 → 完成
  - `stockInsufficientShouldRollbackPayment`：库存不足时抛异常

---

## #25 E2E 测试报告

**新增文件**：
- `docs/testing-guide.md` 第 7 节 — 主链路 + 异常链路用例表 + 单元测试覆盖摘要（原 `e2e-test-report.md` 已并入）

---

## #26 部署规范

**新增文件**：
- `docs/deploy-guide.md` — 环境变量表、数据库初始化顺序、前后端构建命令、发布步骤、回滚方案、安全注意事项

---

## 环境修复

- `pom.xml` — 新增 `maven-surefire-plugin` 配置，添加 `-XX:+EnableDynamicAgentLoading` 解决 Mockito 5 + JDK 17 的 MockMaker 初始化失败问题

---

# 第五批及后续（当前交付基线）

以下增量已合入代码库；完整索引见 [`development-documentation.md`](./development-documentation.md)，测试对照见 [`testing-guide.md`](./testing-guide.md)。

- **三端联动与下单流程**：订单确认页、支付页、支付结果页；订单查询展示骑手/店铺/收货信息（Mapper 联表）。
- **独立管理后台**：`/admin` 独立登录与路由，`adminToken`；商家管理（店铺列表、营业状态切换）；移除「近 30 天活跃用户」统计。
- **商家配送方式**：接单时可选择自配送或骑手池（`deliveryMode`）。
- **多商家**：`GET /api/user/shops`；用户 `/user` 选店后进入 `/user/shop/:shopId`；创建订单强制 `shopId`；员工登录与 `/auth/me` 返回 `shopId`，商家端请求不再硬编码店铺。
- **骑手送达**：送达操作二次确认弹窗。
- **`AuthMeResponse`**：增加 `shopId` 字段，修复旧会话下商家端 `shopId` 为空导致 400 的问题；商家首页待 profile 就绪后再挂载子页面。

# 悦食汇 · 开发文档（整理版）

本文档作为项目开发与交付的**总览入口**：说明系统边界、技术栈、功能模块与文档索引。详细实施记录仍以 [`development-log.md`](./development-log.md) 为准。

---

## 1. 项目定位

基于 Spring Boot + Vue 3 的外卖点餐演示系统，包含四类前端入口：

| 入口 | 路由前缀 | 角色 | 说明 |
|------|-----------|------|------|
| 用户端 | `/user`、`/user/shop/:shopId` 等 | 顾客 | 选店、点餐、购物车、下单确认、模拟支付、订单 |
| 商家端 | `/merchant` | 员工（绑定店铺） | 店铺设置、分类/菜品、订单履约；接单时可选自配送或骑手池 |
| 骑手端 | `/rider` | 骑手 | 可接订单、履约（含送达二次确认）、在线状态 |
| 管理后台 | `/admin` | 超级管理员 | 与商家端同一员工登录入口；平台数据、用户/骑手/商家/订单管理；**商家页支持平台代入驻（新建店铺 + 创建店长账号）** |

---

## 2. 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3、MyBatis、MySQL 8、JWT |
| 前端 | Vue 3、Vue Router、Pinia、Axios、Vite |
| 测试 | JUnit 5、Mockito、`OrderFlowIntegrationTest` 等 |

数据库初始化脚本：[`init.sql`](./init.sql)。增量补丁：`patch-*.sql`。

---

## 3. 功能模块总览（对照验收）

端到端验收条目见 [`checklist.md`](./checklist.md)。需在清单基础上额外覆盖的当前行为：

- **多商家**：用户先进入店铺列表（`/user`），进入某店后为 `/user/shop/:shopId`；下单携带 `shopId`，后端创建订单要求 `shopId` 非空。
- **商家店铺绑定**：员工登录与 `/auth/me` 返回 `shopId`；商家端请求均以该 `shopId` 为准（勿手工写死）；接口层对带 `shopId` 的读写做**店铺隔离**，普通员工不可跨店；`SUPER_ADMIN` 可跨店运维。
- **平台代入驻**：由管理后台调用 `POST /api/admin/shops` 与 `POST /api/admin/shops/{shopId}/employees/bootstrap` 创建新店与首个店长；详见 [`后端接口文档.md`](./后端接口文档.md) §10。
- **演示数据（江南小厨 / 韩味食堂）**：`init.sql` 中为 `shop_id` 2、3 各维护 **主食 / 小吃 / 饮品** 共 **12 道菜品**，定价区分江南亲民档与韩式略高档位；菜品图优先使用 `frontend/public/dishes/` 下静态文件（`/dishes/*.jpg`）。老库增量见 [`patch-expand-shop2-shop3-dishes.sql`](./patch-expand-shop2-shop3-dishes.sql)、[`patch-shop2-shop3-local-images.sql`](./patch-shop2-shop3-local-images.sql)。
- **用户端店铺列表**：`GET /api/user/shops` 返回聚合字段（分类数、菜品数、最低价、推荐菜名字符串等）；卡片左侧「图标 / 四字标签」部分由前端按 **`shop.id`** 写死映射（非 AI 生成），新店默认 🏪 +「精选商家」。
- **登录错误提示**：登录接口返回 **401** 时，前端不得按「踢下线」整页跳转（见 [`后端接口文档.md`](./后端接口文档.md) 接口约定 · 登录失败与 HTTP 401）。
- **下单与支付**：结算 → 订单确认页 → 支付页 → 支付结果页；路由与返回逻辑已串联。
- **三端信息展示**：用户/商家/骑手订单或任务卡片中展示对方关键信息（店铺、收货人、骑手等，以接口与 Mapper 为准）。
- **管理后台**：独立 `adminToken`；含平台看板、用户、骑手、**商家（列表、营业状态切换、新建店铺、创建店长）**、全局订单；已移除「近 30 天活跃用户」统计与展示。
- **配送方式**：商家「接单」时可选择自配送或进入骑手池；非复杂调度算法，以状态与 `rider_id` 为准。
- **骑手送达**：送达前弹窗二次确认，降低误触。

---

## 4. 安全与设计要点（摘要）

- **鉴权**：用户 / 员工 / 骑手 JWT 分类型；商家端按资源所属店铺与请求 `shopId` 校验（`MerchantAuthGuard`）；店长/店员不可越权访问其它店；`SUPER_ADMIN` 可管理任意店；管理接口要求 `SUPER_ADMIN`。
- **库存**：支付成功后在事务内按订单明细扣减；`UPDATE … WHERE stock >= qty` 防止超卖；不足则整单回滚。
- **上传**：商家与用户上传分别限制大小与 MIME，见各 Controller。
- **审计**：关键操作异步写 `operation_log`（见 `OpLogService`）。
- **前端 Axios**：全局响应拦截器对 **401** 会清理本地 Token 并跳转登录页；**登录路径**上的 401 已排除，避免与「密码错误」混淆（见 `frontend/src/api/request.js`）。

---

## 5. 文档索引

| 文档 | 用途 |
|------|------|
| [`development-log.md`](./development-log.md) | 分批次开发日志（库存、鉴权、审计、前端基建、测试与部署等） |
| [`checklist.md`](./checklist.md) | 联调与上线前功能验收清单 |
| （已并入） | MVP 回归用例表见 [`testing-guide.md`](./testing-guide.md) 第 7 节 |
| [`deploy-guide.md`](./deploy-guide.md) | 环境变量、构建、发布与回滚 |
| [`后端接口文档.md`](./后端接口文档.md) | **唯一** API 说明（原 `backend-api.md` 已合并） |
| [`研发流程与规范.md`](./研发流程与规范.md) | 流程与规范 |
| [`文档导航.md`](./文档导航.md) | 导航页 |
| [`文档索引说明.md`](./文档索引说明.md) | 仓库内全部 `.md` 文件一览与说明 |
| [`testing-guide.md`](./testing-guide.md) | **项目测试方案（黑盒 / 白盒）** |

---

## 6. 开发历程与当前基线

- **第 1～4 批**：详见 [`development-log.md`](./development-log.md)（安全与库存、审计与前端基建、体验与骑手、单测与集成测试与 DevOps）。
- **当前代码基线（日志未逐条展开的增量）**：三端联动与下单支付流程、订单 DTO 丰富字段、独立管理后台与 `admin` 认证、商家配送方式选择、多商家与用户选店、骑手送达确认、`AuthMeResponse.shopId` 与商家端 `shopId` 修复等，均已合入主干。**近年增量**（详见 [`development-log.md`](./development-log.md) 文首「增量」小节）：商家端店铺隔离与超管跨店、管理端代入驻、用户端店铺列表聚合、`shop_id` 2/3 演示菜扩充与静态图、`request.js` 对登录 401 豁免等。新验收以本文档第 3 节 + `checklist.md` + [`testing-guide.md`](./testing-guide.md) 为准。

---

## 7. 维护说明

- **部署形态**：**开发**时前端为 **Vite 开发服务器**（代理 `/api`）；**生产**为 **JAR + MySQL**，前端 **`dist`** 可选用 **Nginx**（或其它 Web 服务器）托管并反代 API，或并入后端静态资源；仓库**不再维护** Docker，细节见 `deploy-guide.md` 文首说明。
- 需求冻结后：功能变更请同步更新 `checklist.md`、本文件第 3 节及 `testing-guide.md`。
- 数据库结构变更：更新 `init.sql` 或新增 `patch-*.sql`，并在 `deploy-guide.md` 中注明执行顺序。

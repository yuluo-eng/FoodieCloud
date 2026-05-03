# PR3 开发建议（骑手端收尾 + 用户侧配送感知）

本文基于 PR2（骑手订单履约核心）合并后的现状，梳理 PR3 的开发范围、已知 Bug、新功能建议及 Git 操作流程。

---

## 一、PR2 遗留 Bug（已在 develop 上修复）

以下两处 Bug 已直接在 `develop` 分支修复，PR3 分支从 `develop` 切出后自动包含：

| 文件 | 问题 | 修复内容 |
|------|------|---------|
| `frontend/src/views/rider/RiderHomeView.vue` | `statusText(3)` 返回 "已到店"，但 status=3 实际是骑手已到店并进入配送阶段 | 改为 **"配送中"** |
| `frontend/src/views/rider/RiderHomeView.vue` | "到店" 按钮只判断 `order.status === 2`，若 `riderArriveShop` 不改变 status 则按钮永远不消失 | 增加 `&& !order.riderArriveShopTime` 条件 |

---

## 二、PR3 建议开发范围

### 2.1 骑手端——历史订单

**目标**：骑手可查看已完成（status=4）的历史配送记录。

后端：
- `GET /api/rider/orders/history?page=1&pageSize=20`
- `OrderService` 新增 `riderHistoryOrders(String authorization, int page, int pageSize)`
- `OrderMapper` 新增查询：`WHERE rider_id = #{riderId} AND status = 4 ORDER BY rider_delivered_time DESC`

前端：
- `RiderHomeView.vue` 新增第三个 Tab "历史记录"
- 展示：订单号、金额、送达时间

---

### 2.2 用户端——订单配送状态感知

**目标**：用户在 `UserOrdersView.vue` 中能看到订单当前配送进度。

前端（无需新接口，利用现有 `GET /api/user/orders`）：
- 当 `order.status === 2`：显示 "骑手已接单，正在前往商家"
- 当 `order.status === 3 && !order.riderPickupTime`：显示 "骑手已到店，等待取餐"
- 当 `order.status === 3 && order.riderPickupTime`：显示 "配送中"
- 当 `order.status === 4`：显示 "已送达 ✓"

后端（可选增强）：
- 在用户订单详情中返回 `riderArriveShopTime`、`riderPickupTime`、`riderDeliveredTime` 字段（检查现有 DTO 是否已包含）

---

### 2.3 骑手端——订单卡片信息增强

**目标**：订单卡片显示更多有用信息，减少骑手操作失误。

当前卡片只显示：订单号、金额、店铺ID。

建议增加：
- 收货地址（`shippingAddress`）
- 下单时间（`createTime`）
- 菜品摘要（可选，需后端 DTO 支持）

后端：检查 `riderDispatchOrders` / `riderCurrentOrders` 返回的 DTO 是否包含 `shippingAddress`，若无则在 `OrderMapper.xml` 对应查询中补充字段。

---

### 2.4 测试补充

| 测试类 | 建议覆盖场景 |
|--------|------------|
| `RiderOrderFulfillmentTest`（新建） | accept → arriveShop → pickup → delivered 完整流程；重复操作幂等性；非本人骑手操作返回 403 |
| `RiderOrderControllerTest`（新建） | 各接口 HTTP 状态码；未登录返回 401；骑手 Token 调用用户接口返回 403 |

---

### 2.5 接口文档更新

在 `docs/backend-api.md` 补充：

```
### 骑手订单历史
GET /api/rider/orders/history
Authorization: Bearer <rider_token>
Query: page, pageSize
Response: { records: [...], total, page, pageSize }
```

---

## 三、骑手订单状态流转总结（PR2 实现，PR3 参考）

```
用户下单 → status=0（待支付）
用户支付 → status=1（已支付/待接单）
骑手接单 → status=2（已接单）  riderAcceptTime ✓
骑手到店 → status=3（配送中）  riderArriveShopTime ✓
骑手取餐 →                     riderPickupTime ✓（status 仍为 3）
骑手送达 → status=4（已完成）  riderDeliveredTime ✓
```

> **注意**：`riderArriveShop` 将 status 从 2 改为 3；`riderPickup` 只记录时间不改 status；`riderDelivered` 将 status 从 3 改为 4。

---

## 四、PR3 Git 操作流程

```bash
# 1. 从最新 develop 切出功能分支
git checkout develop
git pull origin develop
git checkout -b feature/rider-PR3-history-ux

# 2. 开发、提交
git add .
git commit -m "feat(rider): PR3 历史订单 + 用户配送感知 + 测试"

# 3. 推送并创建 PR
git push origin feature/rider-PR3-history-ux
gh pr create --base develop --title "Feature/rider PR3 history ux" --body "骑手历史订单、用户配送状态感知、测试补充"

# 4. 合并前跑测试
./mvnw test

# 5. 合并后同步本地
git checkout develop && git pull origin develop
```

---

## 五、PR3 自查清单

| 项目 | 检查点 |
|------|--------|
| 后端 | `riderHistoryOrders` 接口已实现并注册到 Controller |
| 后端 | 用户订单 DTO 包含 `riderArriveShopTime` / `riderPickupTime` |
| 前端 | 骑手端 "历史记录" Tab 正常展示 |
| 前端 | 用户订单列表配送进度文案正确 |
| 测试 | `./mvnw test` 全部通过 |
| 文档 | `docs/backend-api.md` 已补充历史订单接口 |
| Git | PR base 分支为 **`develop`** 而非 `master` |
| Git | 合并后本地已 `git pull` |
# 分支策略（简化 GitFlow）

> 适用于当前 `spring-boot-blank` 项目的团队协作与发布管理。

## 1. 分支类型

- `master`：生产稳定分支，仅合入可发布代码。
- `develop`：开发集成分支，功能日常汇总。
- `feature/*`：功能开发分支，从 `develop` 拉取。
- `release/*`：发布准备分支，从 `develop` 拉取。
- `hotfix/*`：线上紧急修复，从 `master` 拉取。

## 2. 命名规范

- `feature/user-register-api`
- `feature/order-payment-timeout`
- `release/1.2.0`
- `hotfix/issue-231-order-status-bug`

## 3. 常规开发流程

1. 从 `develop` 创建功能分支：
   - `git checkout develop`
   - `git pull`
   - `git checkout -b feature/<name>`
2. 完成开发并提交 PR 到 `develop`。
3. 满足门禁后合并：
   - 至少 1 个 Approve
   - CI 全部通过
   - Review 评论已处理

## 4. 发布流程

1. 从 `develop` 创建发布分支：
   - `git checkout -b release/<version>`
2. 仅允许修复发布阻塞问题，不再新增功能。
3. 回归通过后：
   - 合并 `release/<version>` 到 `master`
   - 在 `master` 打 Tag（如 `v1.2.0`）
   - 将 `release/<version>` 回合并到 `develop`

## 5. 热修复流程

1. 从 `master` 拉取 `hotfix/*` 分支修复线上问题。
2. 修复完成后：
   - 合并到 `master` 并发布
   - 同步合并回 `develop`

## 6. 保护策略建议

对 `master` 与 `develop` 开启分支保护：

- 禁止直接 push。
- 必须通过 Pull Request。
- 必须通过 CI 检查。
- 必须至少 1 个审查通过。
- 建议启用 squash merge，保持提交历史整洁。

## 7. 适用边界与升级建议

- 团队规模较小、发布频率较高时，可逐步迁移到 Trunk-Based（`main` + 短分支）。
- 在迁移前，先稳定自动化测试与 CI 质量门禁。


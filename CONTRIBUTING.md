# 贡献指南（Contributing）

本指南用于统一 `spring-boot-blank` 的协作方式，确保需求、开发、评审、测试、发布全链路可控。

## 1. 开始之前

请先阅读以下文档：

- `docs/process/requirement-template.md`
- `docs/process/code-review-checklist.md`
- `docs/process/branching-strategy.md`
- `docs/quality/testing-strategy.md`
- `docs/architecture/adr-template.md`

## 2. 标准开发流程

1. **创建需求**  
   使用 `docs/process/requirement-template.md` 填写需求，并确认验收标准。

2. **创建分支**  
   从 `develop` 创建 `feature/*` 分支。

3. **进行开发**  
   遵循分层设计：`controller -> service -> repository`。

4. **补充测试**  
   至少覆盖主流程和关键异常流程。

5. **提交 PR**  
   PR 描述使用 `.github/pull_request_template.md`。

6. **代码评审**  
   审查人按 `docs/process/code-review-checklist.md` 进行评审。

7. **合并与发布**  
   按 `docs/process/branching-strategy.md` 执行合并与发版。

## 3. Commit 规范

建议使用 Conventional Commits：

- `feat: ...` 新功能
- `fix: ...` 缺陷修复
- `refactor: ...` 重构
- `test: ...` 测试相关
- `docs: ...` 文档更新
- `chore: ...` 工程维护

示例：

- `feat: 新增用户注册接口`
- `fix: 修复订单状态并发更新问题`

## 4. Pull Request 要求

每个 PR 必须满足：

- 说明变更目的与影响范围
- 说明测试方式与结果
- 说明风险与回滚策略
- 通过 CI 检查
- 至少 1 个 Approve

建议：

- PR 尽量小（建议 200-400 行核心变更）
- 一个 PR 聚焦一个主题，避免混入无关修改

## 5. 需求与架构变更要求

- 涉及业务规则变更：先更新需求文档
- 涉及架构和关键技术决策：新增 ADR 文档
- 涉及数据库结构变更：提供 SQL 脚本与回滚说明

## 6. 安全与质量底线

- 禁止提交密钥、密码、token 等敏感信息
- 禁止绕过鉴权或在日志打印敏感字段
- 关键路径必须可观测（日志、指标、告警）

## 7. 遇到问题怎么办

- 先在 PR 中说明问题与阻塞点
- 必要时新增 ADR 记录权衡与决策
- 线上问题走 `hotfix/*` 流程，修复后回合并 `develop`


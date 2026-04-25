# GitHub 分支保护与 CI 配置清单

> 目标：把流程制度化，避免“口头规范”失效。

## 1. 需要保护的分支

- `master`
- `develop`

## 2. Branch Protection Rules（推荐项）

在 GitHub 仓库设置中，对 `master` 和 `develop` 配置以下规则：

- [ ] Require a pull request before merging  
      （必须通过 PR 合并，禁止直接 push）
- [ ] Require approvals  
      （至少 1 个 Approve，核心模块建议 2 个）
- [ ] Dismiss stale pull request approvals when new commits are pushed  
      （有新提交后需重新审查）
- [ ] Require review from Code Owners（可选）  
      （关键目录必须由指定负责人审查）
- [ ] Require status checks to pass before merging  
      （CI 全通过才能合并）
- [ ] Require conversation resolution before merging  
      （所有 review 对话必须解决）
- [ ] Require linear history（可选）  
      （保持线性历史，推荐搭配 squash/rebase）
- [ ] Do not allow bypassing the above settings  
      （管理员不绕过规则）

## 3. PR 合并策略建议

- [ ] 默认使用 **Squash and merge**（保持历史整洁）
- [ ] 禁用或少用 Merge commit（避免噪音提交）
- [ ] 对需要保留提交粒度的模块可使用 Rebase merge

## 4. CI 最低检查项（后端 Spring Boot）

建议在 CI 中至少包含：

- [ ] 代码编译：`mvn -q -DskipTests compile`
- [ ] 单元测试：`mvn -q test`
- [ ] 打包检查：`mvn -q -DskipTests package`
- [ ] 基础静态检查（如已接入 Checkstyle/SpotBugs/PMD）

> 注意：命令可根据你当前项目插件配置调整，以实际能稳定执行为准。

## 5. 必选状态检查（Status Checks）

在 Branch Protection 中，勾选并要求以下检查必须通过：

- [ ] `build`
- [ ] `test`
- [ ] `package`

如果采用 GitHub Actions，可在工作流中将 job 名称固定为上述名字，便于管理。

## 6. CODEOWNERS（可选但推荐）

可在 `.github/CODEOWNERS` 设置关键目录责任人，例如：

```text
# 全局默认审查人
* @team-lead

# 后端核心模块
/src/main/java/ @backend-owner

# 数据库脚本
/docs/*.sql @dba-owner
```

## 7. 上线前门禁建议

- [ ] release 分支回归通过
- [ ] 发布说明（变更点、风险、回滚）已准备
- [ ] Tag 已规划（如 `v1.2.0`）
- [ ] 监控与告警规则已确认

## 8. 首次落地建议顺序

1. 先保护 `master`（阻断直接提交风险）。
2. 再保护 `develop`（规范日常协作）。
3. 最后逐步提升 CI 检查强度（从编译+测试开始）。


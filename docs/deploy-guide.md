# 预发布部署与环境变量规范

## 1. 系统要求

| 组件 | 最低版本 |
|------|---------|
| JDK | 17+ |
| MySQL | 8.0+ |
| Node.js | 18+ (仅构建前端) |
| Maven | 3.8+ (或使用 mvnw) |

---

## 2. 环境变量

| 变量 | 说明 | 默认值 | 必填 |
|------|------|--------|------|
| `DB_HOST` | MySQL 地址 | `localhost` | 生产必填 |
| `DB_PORT` | MySQL 端口 | `3306` | |
| `DB_NAME` | 数据库名 | `ysh` | |
| `DB_USER` | 数据库用户 | `root` | 生产必填 |
| `DB_PASSWORD` | 数据库密码 | — | 生产必填 |
| `JWT_SECRET` | JWT 签名密钥 | — | **必填**，≥ 32 字符 |
| `JWT_EXPIRATION_MS` | Token 有效期 (ms) | `86400000` (24h) | |
| `APP_UPLOAD_DIR` | 文件上传目录 | `uploads` | |
| `SERVER_PORT` | 服务端口 | `8080` | |

### application.yml 中通过 `${VAR:default}` 引用：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ysh}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}

app:
  jwt:
    secret: ${JWT_SECRET:change-me-in-production}
    expiration-ms: ${JWT_EXPIRATION_MS:86400000}
  upload:
    dir: ${APP_UPLOAD_DIR:uploads}
```

---

## 3. 数据库初始化

### 3.1 全新库（推荐）

当前主干已将演示店铺、多店菜品、`patch-dish-images` 等与早期补丁中的多处逻辑**合并进** [`docs/init.sql`](./init.sql)。**从零搭建环境时只需执行：**

```bash
mysql -u $DB_USER -p $DB_NAME < docs/init.sql
```

若仍需仓库内历史脚本（与早期分支对齐），可按下列顺序追加执行：

```bash
mysql -u $DB_USER -p $DB_NAME < docs/patch-rider.sql
mysql -u $DB_USER -p $DB_NAME < docs/patch-dish-images.sql
mysql -u $DB_USER -p $DB_NAME < docs/patch-user-shipping.sql
mysql -u $DB_USER -p $DB_NAME < docs/patch-operation-log.sql
```

重复执行可能报错（索引已存在等），以报错提示为准，可忽略或手工调整后重试。

### 3.2 已有库升级（增量）

若数据库是在**旧版 `init.sql`** 下创建的，可按需执行增量补丁（已包含幂等或「不存在则插入」的写法者为佳）：

| 补丁文件 | 用途（摘要） |
|----------|----------------|
| [`patch-shop2-shop3-local-images.sql`](./patch-shop2-shop3-local-images.sql) | 江南小厨/韩味食堂六道菜图片改为本地 `/dishes/*.jpg` |
| [`patch-expand-shop2-shop3-dishes.sql`](./patch-expand-shop2-shop3-dishes.sql) | 两家店调价 + 扩充菜品数据 |

执行示例：

```bash
mysql -u $DB_USER -p $DB_NAME < docs/patch-shop2-shop3-local-images.sql
mysql -u $DB_USER -p $DB_NAME < docs/patch-expand-shop2-shop3-dishes.sql
```

**说明**：若已用**最新** `init.sql` 全量重建，上述两条通常不必再执行（内容已包含在 `init.sql` 中）。

---

## 4. 构建与启动

### 后端

```bash
# 编译 + 打包（跳过测试）
./mvnw clean package -DskipTests

# 启动
java -jar target/spring-boot-blank-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### 前端

```bash
cd frontend
npm install
npm run build       # 输出到 dist/

# 方式一：将 dist/ 放到后端 static 资源目录
cp -r dist/* ../src/main/resources/static/

# 方式二：使用 Nginx 反向代理
#   location /api { proxy_pass http://127.0.0.1:8080; }
#   location / { root /path/to/dist; try_files $uri /index.html; }
```

---

## 5. 发布步骤

1. **代码合并**：PR 合并到 `develop` → 再合并到 `main`
2. **打 Tag**：`git tag v1.0.0 && git push origin v1.0.0`
3. **构建**：后端 `mvnw package`，前端 `npm run build`
4. **数据库迁移**：执行新增的 patch SQL
5. **部署**：替换 jar 包，重启服务
6. **验证**：按 `docs/checklist.md` 执行冒烟测试
7. **监控**：检查 `operation_log` 表有新记录、无异常日志

---

## 6. 回滚方案

| 场景 | 操作 |
|------|------|
| 代码问题 | `git revert` 或切回上一个 Tag 重新打包部署 |
| 数据库问题 | 提前备份 `mysqldump`，必要时恢复 |
| 紧急回滚 | 保留旧版 jar 包，直接替换重启 |

---

## 7. 上传目录

- 确保 `APP_UPLOAD_DIR` 指定的目录存在且应用有读写权限
- 生产环境建议使用独立存储（如 OSS），上传目录仅作中转
- 定期清理未引用的旧文件

---

## 8. 安全注意事项

- **JWT_SECRET**：生产环境必须使用随机生成的强密钥，不要提交到代码仓库
- **DB_PASSWORD**：通过环境变量或密钥管理服务注入
- **HTTPS**：生产环境必须启用 TLS
- **CORS**：按需配置允许的 Origin

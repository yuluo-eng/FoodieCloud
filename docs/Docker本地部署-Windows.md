# Windows 本地 Docker 部署（前后端 + MySQL）

## 为什么用 Docker

### 好处
- 环境一致：所有人都跑同一套镜像，减少“我这能跑你那不行”。
- 上手快：Windows 新电脑只装 Docker Desktop，就能一键拉起 MySQL、后端、前端。
- 隔离好：Java、Node、MySQL 依赖不污染本机环境。
- 易迁移：以后上云或换机器，直接复制 `docker-compose.yml` 即可。

### 缺点
- 首次构建慢：第一次 build 会下载 Maven/Node 镜像和依赖。
- 占用资源更高：相比本机直跑，会多占内存和磁盘。
- 调试链路稍复杂：需要看容器日志，不如本地进程直观。
- 文件挂载在 Windows 上偶尔有性能损耗（尤其是大量小文件）。

## 前置条件
- 已安装并启动 Docker Desktop（Windows）。
- 在项目根目录执行命令（包含 `docker-compose.yml` 的目录）。

## 一键启动

```powershell
docker compose up -d --build
```

启动后访问：
- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

## 常用命令

### 查看状态
```powershell
docker compose ps
```

### 查看日志
```powershell
docker compose logs -f
```

### 只看后端日志
```powershell
docker compose logs -f backend
```

### 停止服务
```powershell
docker compose down
```

### 停止并删除数据卷（会清空 MySQL 数据）
```powershell
docker compose down -v
```

## 说明
- MySQL 首次启动会自动执行 `docs/init.sql` 初始化库和测试数据。
- 上传文件目录已做卷持久化，容器重建后图片不丢失。
- 当前密码与 JWT 配置为开发默认值，仅用于本地测试。

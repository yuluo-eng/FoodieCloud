# 悦食汇 · 前端（Vue 3 + Vite）

## 运行

1. 先启动后端（默认 `http://127.0.0.1:8080`）。
2. 在本目录执行：

```bash
npm install
npm run dev
```

浏览器打开终端提示的地址（一般为 `http://127.0.0.1:5173`）。

开发环境下，`/api` 会由 Vite 代理到后端；同时后端已开启对 `5173` 的 CORS，也可按需直连。

## 页面

- `/`：选择顾客端 / 商家端
- `/user/login`、`/user/register`、`/user`（需登录，展示 `/api/auth/me`）
- `/merchant/login`、`/merchant`（需登录）

## 构建

```bash
npm run build
npm run preview
```

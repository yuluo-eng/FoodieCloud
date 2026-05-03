<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="error-page">
    <div class="error-card card">
      <div class="error-code">404</div>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-desc">你访问的页面已被移除或者地址不正确</p>
      <button class="btn btn-primary" @click="goHome">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 1.5rem;
}
.error-card {
  text-align: center;
  padding: 3rem 2rem;
  max-width: 380px;
  width: 100%;
}
.error-code {
  font-size: 4.5rem;
  font-weight: 800;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, var(--brand) 0%, var(--brand-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1;
  margin-bottom: 0.5rem;
}
.error-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 0.5rem;
}
.error-desc {
  color: var(--muted);
  font-size: 0.92rem;
  margin: 0 0 1.5rem;
}
</style>

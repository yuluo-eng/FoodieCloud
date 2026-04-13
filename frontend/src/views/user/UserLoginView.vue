<script setup>
import { ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userLogin } from '@/api/auth'
import heroFood from '@/assets/real/noodle.jpg'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function onSubmit() {
  error.value = ''
  loading.value = true
  try {
    const data = await userLogin({
      username: username.value.trim(),
      password: password.value,
    })
    auth.setUserToken(data.token)
    const redirect = route.query.redirect || '/user'
    router.replace(typeof redirect === 'string' ? redirect : '/user')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="brand-shell" :style="{ '--hero': `url(${heroFood})` }">
    <div class="brand-left">
      <p class="brand-kicker">悦食汇 · 顾客端</p>
      <h1>今天吃点什么？</h1>
      <p>登录后即可浏览菜品、加入购物车、下单与支付。</p>
      <RouterLink class="back" to="/">← 返回首页</RouterLink>
    </div>

    <div class="card form-card">
      <h2>顾客登录</h2>
      <p class="sub">欢迎回来</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <form @submit.prevent="onSubmit">
        <div class="field">
          <label class="label" for="u-name">用户名</label>
          <input id="u-name" v-model="username" class="input" autocomplete="username" required />
        </div>
        <div class="field">
          <label class="label" for="u-pass">密码</label>
          <input id="u-pass" v-model="password" type="password" class="input" autocomplete="current-password" required />
        </div>
        <button type="submit" class="btn btn-primary full" :disabled="loading">{{ loading ? '登录中…' : '登录' }}</button>
      </form>
      <p class="footer">还没有账号？<RouterLink to="/user/register">去注册</RouterLink></p>
    </div>
  </div>
</template>

<style scoped>
.brand-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background:
    linear-gradient(120deg, rgba(255, 237, 213, 0.9), rgba(255, 251, 235, 0.92)),
    var(--hero) center/cover no-repeat;
}
.brand-left {
  padding: 3.2rem 2.4rem;
  color: #7c2d12;
}
.brand-kicker {
  display: inline-block;
  background: #ffedd5;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
}
h1 { margin: 1rem 0 0.7rem; font-size: 2.4rem; }
.back { display: inline-block; margin-top: 1.2rem; color: #9a3412; font-weight: 700; }
.form-card {
  margin: auto;
  width: min(420px, calc(100% - 28px));
  padding: 1.6rem;
  border-radius: 16px;
}
h2 { margin: 0; }
.sub { margin: 0.3rem 0 1rem; color: #78716c; }
.full { width: 100%; }
.footer { margin-top: 1rem; color: #78716c; font-size: 0.9rem; }
@media (max-width: 880px) {
  .brand-shell { grid-template-columns: 1fr; }
  .brand-left { padding: 1.2rem 1rem 0.3rem; }
}
</style>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { employeeLogin } from '@/api/auth'
import heroFood from '@/assets/real/tea.jpg'

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
    const data = await employeeLogin({
      username: username.value.trim(),
      password: password.value,
    })
    auth.setMerchantToken(data.token)
    auth.setMerchantFromLogin(data.employeeInfo)
    const redirect = route.query.redirect || '/merchant'
    router.replace(typeof redirect === 'string' ? redirect : '/merchant')
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
      <p class="brand-kicker">悦食汇 · 商家端</p>
      <h1>经营从这里开始</h1>
      <p>登录后管理菜品、员工与订单履约流程。</p>
      <RouterLink class="back" to="/">← 返回首页</RouterLink>
    </div>

    <div class="card form-card">
      <h2>商家登录</h2>
      <p class="sub">员工账号登录后台</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <form @submit.prevent="onSubmit">
        <div class="field"><label class="label">账号</label><input v-model="username" class="input" required /></div>
        <div class="field"><label class="label">密码</label><input v-model="password" type="password" class="input" required /></div>
        <button type="submit" class="btn btn-primary full" :disabled="loading">{{ loading ? '登录中…' : '登录' }}</button>
      </form>
      <p class="tip">初始化账号见 <code>docs/init.sql</code>（如 <code>admin/root</code>）。</p>
    </div>
  </div>
</template>

<style scoped>
.brand-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background:
    linear-gradient(120deg, rgba(255, 237, 213, 0.9), rgba(254, 242, 230, 0.9)),
    var(--hero) center/cover no-repeat;
}
.brand-left { padding: 3.2rem 2.4rem; color: #7c2d12; }
.brand-kicker { display: inline-block; background: #ffedd5; padding: .2rem .6rem; border-radius: 999px; font-size: .78rem; font-weight: 700; }
h1 { margin: 1rem 0 .7rem; font-size: 2.3rem; }
.back { display: inline-block; margin-top: 1.2rem; color: #9a3412; font-weight: 700; }
.form-card { margin: auto; width: min(430px, calc(100% - 28px)); padding: 1.6rem; border-radius: 16px; }
h2 { margin: 0; }
.sub { margin: .3rem 0 1rem; color: #78716c; }
.full { width: 100%; }
.tip { margin-top: 1rem; color: #78716c; font-size: .86rem; }
@media (max-width: 880px) { .brand-shell { grid-template-columns: 1fr; } .brand-left { padding: 1.2rem 1rem .3rem; } }
</style>

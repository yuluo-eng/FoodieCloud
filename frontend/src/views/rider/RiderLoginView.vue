<script setup>
import { ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { riderLogin } from '@/api/auth'

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
    const data = await riderLogin({
      username: username.value.trim(),
      password: password.value,
    })
    auth.setRiderToken(data.token)
    auth.setRiderFromLogin(data.riderInfo)
    const redirect = route.query.redirect || '/rider'
    router.replace(typeof redirect === 'string' ? redirect : '/rider')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="shell">
    <div class="card form-card">
      <h2>骑手登录</h2>
      <p class="sub">登录后即可接单与履约</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <form @submit.prevent="onSubmit">
        <div class="field"><label class="label">账号</label><input v-model="username" class="input" required /></div>
        <div class="field"><label class="label">密码</label><input v-model="password" type="password" class="input" required /></div>
        <button type="submit" class="btn btn-primary full" :disabled="loading">{{ loading ? '登录中…' : '登录' }}</button>
      </form>
      <RouterLink class="btn btn-secondary full register-btn" to="/rider/register">没有账号？立即注册</RouterLink>
      <p class="tip">注册后可直接使用骑手账号登录。</p>
      <RouterLink class="back" to="/">← 返回首页</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.shell { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(180deg, #fff7ed, #ffedd5); padding: 12px; }
.form-card { width: min(420px, 100%); padding: 1.4rem; border-radius: 14px; }
.sub { margin: .3rem 0 1rem; color: #78716c; }
.full { width: 100%; }
.register-btn {
  margin-top: .7rem;
  display: inline-flex;
  justify-content: center;
  text-decoration: none;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  color: #334155;
}
.tip { margin-top: .8rem; color: #78716c; font-size: .86rem; }
.back { display: inline-block; margin-top: .7rem; color: #9a3412; font-weight: 600; }
</style>

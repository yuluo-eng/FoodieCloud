<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { userRegister } from '@/api/auth'
import heroFood from '@/assets/real/chicken.jpg'

const router = useRouter()
const username = ref('')
const password = ref('')
const phone = ref('')
const nickname = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

async function onSubmit() {
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    await userRegister({
      username: username.value.trim(),
      password: password.value,
      phone: phone.value.trim() || undefined,
      nickname: nickname.value.trim() || undefined,
    })
    success.value = '注册成功，请登录'
    setTimeout(() => router.replace('/user/login'), 800)
  } catch (e) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="brand-shell" :style="{ '--hero': `url(${heroFood})` }">
    <div class="brand-left">
      <p class="brand-kicker">悦食汇 · 新用户</p>
      <h1>3 秒加入悦食汇</h1>
      <p>注册后即可在顾客端进行点单与支付流程演示。</p>
      <RouterLink class="back" to="/user/login">← 返回登录</RouterLink>
    </div>

    <div class="card form-card">
      <h2>注册账号</h2>
      <p class="sub">创建你的顾客账号</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert ok">{{ success }}</div>
      <form @submit.prevent="onSubmit">
        <div class="field"><label class="label">用户名</label><input v-model="username" class="input" required minlength="2" /></div>
        <div class="field"><label class="label">密码</label><input v-model="password" type="password" class="input" required minlength="6" /></div>
        <div class="field"><label class="label">手机号（可选）</label><input v-model="phone" class="input" /></div>
        <div class="field"><label class="label">昵称（可选）</label><input v-model="nickname" class="input" /></div>
        <button type="submit" class="btn btn-primary full" :disabled="loading">{{ loading ? '提交中…' : '注册' }}</button>
      </form>
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
.brand-left { padding: 3.2rem 2.4rem; color: #7c2d12; }
.brand-kicker { display: inline-block; background: #ffedd5; padding: .2rem .6rem; border-radius: 999px; font-size: .78rem; font-weight: 700; }
h1 { margin: 1rem 0 .7rem; font-size: 2.3rem; }
.back { display: inline-block; margin-top: 1.2rem; color: #9a3412; font-weight: 700; }
.form-card { margin: auto; width: min(430px, calc(100% - 28px)); padding: 1.6rem; border-radius: 16px; }
h2 { margin: 0; }
.sub { margin: .3rem 0 1rem; color: #78716c; }
.full { width: 100%; }
.ok { background: #f0fdf4; color: var(--success); border: 1px solid #bbf7d0; }
@media (max-width: 880px) { .brand-shell { grid-template-columns: 1fr; } .brand-left { padding: 1.2rem 1rem .3rem; } }
</style>

<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { riderRegister } from '@/api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const realName = ref('')
const phone = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

async function onSubmit() {
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    await riderRegister({
      username: username.value.trim(),
      password: password.value,
      realName: realName.value.trim(),
      phone: phone.value.trim(),
    })
    success.value = '注册成功，请登录骑手端'
    setTimeout(() => router.replace('/rider/login'), 800)
  } catch (e) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="shell">
    <div class="card form-card">
      <h2>骑手注册</h2>
      <p class="sub">创建骑手账号后即可登录接单</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert ok">{{ success }}</div>
      <form @submit.prevent="onSubmit">
        <div class="field"><label class="label">用户名</label><input v-model="username" class="input" required minlength="2" /></div>
        <div class="field"><label class="label">密码</label><input v-model="password" type="password" class="input" required minlength="6" /></div>
        <div class="field"><label class="label">真实姓名</label><input v-model="realName" class="input" required /></div>
        <div class="field"><label class="label">手机号</label><input v-model="phone" class="input" required /></div>
        <button type="submit" class="btn btn-primary full" :disabled="loading">{{ loading ? '提交中…' : '注册' }}</button>
      </form>
      <RouterLink class="back" to="/rider/login">← 返回登录</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.shell { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(180deg, #eff6ff, #dbeafe); padding: 12px; }
.form-card { width: min(420px, 100%); padding: 1.4rem; border-radius: 14px; }
.sub { margin: .3rem 0 1rem; color: #64748b; }
.full { width: 100%; }
.ok { background: #f0fdf4; color: var(--success); border: 1px solid #bbf7d0; }
.back { display: inline-block; margin-top: .7rem; color: #1d4ed8; font-weight: 600; }
</style>

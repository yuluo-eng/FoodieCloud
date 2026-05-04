<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const username = ref('')
const password = ref('')
const loading = ref(false)

async function login() {
  if (!username.value || !password.value) {
    toast.warn('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/auth/employee/login', {
      username: username.value,
      password: password.value,
    })
    const data = res.data.data
    if (!data?.token) throw new Error('登录失败')

    const roleCode = data.roleCode || data.role_code || ''
    if (roleCode !== 'SUPER_ADMIN' && username.value !== 'admin') {
      toast.error('仅超级管理员可登录管理后台')
      return
    }

    auth.setAdminToken(data.token, {
      id: data.employeeId || data.id,
      username: data.username || username.value,
      roleCode,
    })
    router.replace('/admin')
  } catch (e) {
    toast.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-login-page">
    <div class="login-card">
      <div class="brand">
        <span class="brand-icon">🛡️</span>
        <h1>悦食汇 · 管理后台</h1>
      </div>
      <form @submit.prevent="login">
        <div class="field">
          <label>管理员账号</label>
          <input v-model="username" type="text" placeholder="请输入账号" autocomplete="username" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <button class="btn-login" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </form>
      <p class="hint">仅限 SUPER_ADMIN 角色登录</p>
    </div>
  </div>
</template>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 20px;
}
.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.brand {
  text-align: center;
  margin-bottom: 28px;
}
.brand-icon { font-size: 2.2rem; }
.brand h1 {
  margin: 8px 0 0;
  font-size: 1.3rem;
  color: #1e293b;
}
.field {
  margin-bottom: 18px;
}
.field label {
  display: block;
  font-size: 0.88rem;
  color: #475569;
  margin-bottom: 6px;
  font-weight: 500;
}
.field input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: border-color 0.2s;
  box-sizing: border-box;
}
.field input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
.btn-login {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 4px;
}
.btn-login:disabled { opacity: 0.6; cursor: not-allowed; }
.hint {
  text-align: center;
  color: #94a3b8;
  font-size: 0.8rem;
  margin: 16px 0 0;
}
</style>

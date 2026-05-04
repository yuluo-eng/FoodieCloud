<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const auth = useAuthStore()
const toast = useToast()
const users = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await request.get('/admin/users', {
      params: { page: page.value, pageSize: 20 },
      headers: { Authorization: `Bearer ${auth.adminToken}` },
    })
    users.value = res.data.data?.records || []
    total.value = res.data.data?.total || 0
  } catch (e) {
    toast.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function toggle(user) {
  const next = user.enabled === 1 ? 0 : 1
  try {
    await request.patch(`/admin/users/${user.id}/toggle`, null, {
      params: { enabled: next },
      headers: { Authorization: `Bearer ${auth.adminToken}` },
    })
    user.enabled = next
    toast.success(next === 1 ? '已启用' : '已禁用')
  } catch (e) {
    toast.error(e.message || '操作失败')
  }
}
</script>

<template>
  <section class="admin-panel">
    <h2>用户管理 <span class="count">共 {{ total }} 人</span></h2>
    <div v-if="loading" class="tip">加载中...</div>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>ID</th><th>用户名</th><th>昵称</th><th>手机</th><th>收货地址</th><th>状态</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>{{ u.nickname || '-' }}</td>
          <td>{{ u.phone || '-' }}</td>
          <td class="addr">{{ u.shippingAddress || '-' }}</td>
          <td><span :class="u.enabled === 1 ? 'tag-on' : 'tag-off'">{{ u.enabled === 1 ? '正常' : '禁用' }}</span></td>
          <td><button class="btn-sm" @click="toggle(u)">{{ u.enabled === 1 ? '禁用' : '启用' }}</button></td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.admin-panel { background: #fff; border-radius: 10px; padding: 20px; }
h2 { margin: 0 0 16px; font-size: 1.1rem; }
.count { font-weight: 400; color: #78716c; font-size: 0.85rem; }
.data-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.data-table th { text-align: left; padding: 10px 8px; border-bottom: 2px solid #e5e7eb; color: #6b7280; font-weight: 600; }
.data-table td { padding: 10px 8px; border-bottom: 1px solid #f3f4f6; }
.addr { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tag-on { color: #16a34a; font-weight: 600; }
.tag-off { color: #dc2626; font-weight: 600; }
.btn-sm { border: none; background: #eef2ff; border-radius: 6px; padding: 5px 12px; cursor: pointer; font-size: 0.85rem; }
.tip { color: #888; padding: 2rem 0; }
</style>

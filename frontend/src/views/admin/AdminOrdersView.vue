<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const auth = useAuthStore()
const toast = useToast()
const orders = ref([])
const total = ref(0)
const loading = ref(false)
const statusFilter = ref(-1)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const params = { page: 1, pageSize: 50 }
    if (statusFilter.value !== -1) params.status = statusFilter.value
    const res = await request.get('/admin/orders', {
      params,
      headers: { Authorization: `Bearer ${auth.adminToken}` },
    })
    orders.value = res.data.data?.records || []
    total.value = res.data.data?.total || 0
  } catch (e) {
    toast.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function statusText(s) {
  return { 0: '待支付', 1: '已支付', 2: '已接单', 3: '配送中', 4: '已完成', 5: '已取消' }[s] || '未知'
}
function payText(s) {
  return { 0: '未支付', 1: '已支付', 2: '已退款' }[s] || '未知'
}
function formatMoney(v) { return Number(v || 0).toFixed(2) }
</script>

<template>
  <section class="admin-panel">
    <div class="head-row">
      <h2>全局订单 <span class="count">共 {{ total }} 笔</span></h2>
      <select v-model.number="statusFilter" @change="load">
        <option :value="-1">全部状态</option>
        <option :value="0">待支付</option>
        <option :value="1">已支付</option>
        <option :value="2">已接单</option>
        <option :value="3">配送中</option>
        <option :value="4">已完成</option>
        <option :value="5">已取消</option>
      </select>
    </div>
    <div v-if="loading" class="tip">加载中...</div>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>ID</th><th>订单号</th><th>店铺</th><th>金额</th><th>状态</th><th>支付</th><th>骑手</th><th>下单时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="o in orders" :key="o.id">
          <td>{{ o.id }}</td>
          <td class="mono">{{ o.orderNo }}</td>
          <td>{{ o.shopName || `#${o.shopId}` }}</td>
          <td>¥{{ formatMoney(o.totalAmount) }}</td>
          <td>{{ statusText(o.status) }}</td>
          <td>{{ payText(o.payStatus) }}</td>
          <td>{{ o.riderName || '-' }}</td>
          <td class="time">{{ o.createTime || '-' }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.admin-panel { background: #fff; border-radius: 10px; padding: 20px; }
.head-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
h2 { margin: 0; font-size: 1.1rem; }
.count { font-weight: 400; color: #78716c; font-size: 0.85rem; }
select { border: 1px solid #ddd; border-radius: 8px; padding: 6px 10px; }
.data-table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
.data-table th { text-align: left; padding: 10px 8px; border-bottom: 2px solid #e5e7eb; color: #6b7280; font-weight: 600; }
.data-table td { padding: 10px 8px; border-bottom: 1px solid #f3f4f6; }
.mono { font-family: monospace; font-size: 0.82rem; }
.time { font-size: 0.8rem; color: #78716c; white-space: nowrap; }
.tip { color: #888; padding: 2rem 0; }
</style>

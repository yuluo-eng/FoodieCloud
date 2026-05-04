<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const auth = useAuthStore()
const toast = useToast()
const loading = ref(true)
const data = ref({
  totalOrders: 0,
  totalRevenue: '0',
  activeRiders: 0,
  totalUsers: 0,
  totalRiders: 0,
  totalShops: 0,
})

onMounted(async () => {
  try {
    const res = await request.get('/admin/dashboard', {
      headers: { Authorization: `Bearer ${auth.adminToken}` },
    })
    data.value = res.data.data
  } catch (e) {
    toast.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="admin-panel">
    <h2>平台数据概览</h2>
    <div v-if="loading" class="tip">加载中...</div>
    <div v-else class="grid">
      <div class="card purple">
        <div class="val">{{ data.totalOrders }}</div>
        <div class="label">总订单</div>
      </div>
      <div class="card orange">
        <div class="val">¥{{ data.totalRevenue }}</div>
        <div class="label">总营收（已支付）</div>
      </div>
      <div class="card blue">
        <div class="val">{{ data.totalUsers }}</div>
        <div class="label">注册用户</div>
      </div>
      <div class="card teal">
        <div class="val">{{ data.totalRiders }}</div>
        <div class="label">注册骑手</div>
      </div>
      <div class="card cyan">
        <div class="val">{{ data.activeRiders }}</div>
        <div class="label">在线骑手</div>
      </div>
      <div class="card pink">
        <div class="val">{{ data.totalShops }}</div>
        <div class="label">店铺数</div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-panel { background: #fff; border-radius: 10px; padding: 20px; }
h2 { margin: 0 0 20px; font-size: 1.1rem; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 16px; }
.card {
  border-radius: 10px;
  padding: 22px 18px;
  color: #fff;
  text-align: center;
}
.val { font-size: 1.8rem; font-weight: 800; margin-bottom: 6px; }
.label { font-size: 0.85rem; opacity: 0.9; }
.purple { background: linear-gradient(135deg, #667eea, #764ba2); }
.orange { background: linear-gradient(135deg, #ff6b35, #f7931e); }
.blue { background: linear-gradient(135deg, #2563eb, #3b82f6); }
.green { background: linear-gradient(135deg, #059669, #34d399); }
.teal { background: linear-gradient(135deg, #0d9488, #5eead4); }
.cyan { background: linear-gradient(135deg, #0891b2, #67e8f9); }
.pink { background: linear-gradient(135deg, #db2777, #f472b6); }
.tip { color: #888; padding: 2rem 0; }
</style>

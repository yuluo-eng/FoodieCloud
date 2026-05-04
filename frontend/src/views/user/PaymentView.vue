<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

const orderId = Number(route.params.orderId)
const order = ref(null)
const loading = ref(true)
const paying = ref(false)
const paymentNo = ref('')

onMounted(async () => {
  try {
    const res = await request.get(`/user/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    order.value = res.data.data?.order
    if (!order.value) {
      toast.error('订单不存在')
      router.replace('/user')
    }
  } catch (e) {
    toast.error('加载订单失败')
    router.replace('/user')
  } finally {
    loading.value = false
  }
})

async function pay() {
  if (paying.value) return
  paying.value = true
  try {
    const createRes = await request.post(
      '/user/payments/create',
      { orderId, payChannel: 'MOCK' },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )
    paymentNo.value = createRes.data.data?.paymentNo
    if (!paymentNo.value) throw new Error('支付单创建失败')

    await request.post(
      '/user/payments/mock-success',
      { paymentNo: paymentNo.value },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )

    router.replace(`/user/payment-result/${orderId}?status=success`)
  } catch (err) {
    toast.error(err.message || '支付失败')
    router.replace(`/user/payment-result/${orderId}?status=fail`)
  } finally {
    paying.value = false
  }
}

function goBack() {
  router.push('/user')
}
</script>

<template>
  <div class="payment-page">
    <header class="page-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>订单支付</h1>
    </header>

    <div v-if="loading" class="loading-row"><span class="spinner"></span> 加载中…</div>

    <template v-else-if="order">
      <section class="pay-card">
        <p class="order-no">订单号：{{ order.orderNo }}</p>
        <div class="amount-row">
          <span>支付金额</span>
          <span class="amount">¥{{ Number(order.totalAmount || 0).toFixed(2) }}</span>
        </div>
      </section>

      <section class="method-section">
        <h3>支付方式</h3>
        <label class="method-item active">
          <input type="radio" checked disabled />
          <span>模拟支付（Mock）</span>
        </label>
      </section>

      <div class="bottom-bar">
        <button class="pay-btn" :disabled="paying" @click="pay">
          {{ paying ? '支付处理中…' : `确认支付 ¥${Number(order.totalAmount || 0).toFixed(2)}` }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}
.page-header {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  padding: 16px 20px;
  padding-top: calc(16px + env(safe-area-inset-top));
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-header h1 { margin: 0; font-size: 1.15rem; }
.back-btn {
  background: none;
  border: none;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  padding: 4px 0;
  min-height: auto;
  min-width: auto;
}
.loading-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 3rem 0;
  color: #78716c;
}
.pay-card {
  background: #fff;
  margin: 16px;
  border-radius: 12px;
  padding: 20px;
}
.order-no { margin: 0 0 16px; color: #78716c; font-size: 0.88rem; }
.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.amount-row span:first-child { color: #333; font-size: 1rem; }
.amount {
  font-size: 1.8rem;
  font-weight: 800;
  color: #c2410c;
}
.method-section {
  background: #fff;
  margin: 0 16px;
  border-radius: 12px;
  padding: 16px 20px;
}
.method-section h3 { margin: 0 0 12px; font-size: 0.95rem; color: #333; }
.method-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  font-size: 0.95rem;
  cursor: default;
}
.method-item input { accent-color: #c2410c; }
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 12px 20px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  z-index: 100;
}
.pay-btn {
  width: 100%;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 14px;
  font-size: 1.05rem;
  font-weight: 700;
  cursor: pointer;
}
.pay-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>

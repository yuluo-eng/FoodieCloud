<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const remark = ref('')
const shopId = Number(route.query.shopId) || null
const submitting = ref(false)
const cartItems = ref([])
const loading = ref(true)

const cartTotal = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
)

const address = computed(() => {
  const p = auth.userProfile
  if (!p) return null
  return {
    receiverName: p.receiverName,
    phone: p.shippingPhone || p.phone,
    address: p.shippingAddress,
  }
})

onMounted(async () => {
  try {
    await auth.refreshUserProfile()
    const res = await request.get('/user/cart', {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    const all = res.data.data || []
    cartItems.value = all.filter((item) => item.selected === 1)
    if (cartItems.value.length === 0) {
      toast.warn('没有选中的购物车商品')
      router.replace('/user')
    }
  } catch (e) {
    toast.error('加载购物车失败')
    router.replace('/user')
  } finally {
    loading.value = false
  }
})

async function submitOrder() {
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await request.post(
      '/user/orders',
      { shopId, remark: remark.value.trim() },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )
    const data = res.data.data
    router.replace(`/user/payment/${data.orderId}`)
  } catch (err) {
    toast.error(err.message || '下单失败，请重试')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="confirm-page">
    <header class="page-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>确认订单</h1>
    </header>

    <div v-if="loading" class="loading-row"><span class="spinner"></span> 加载中…</div>

    <template v-else>
      <section class="section address-section" @click="router.push('/user/profile')">
        <h3>收货信息</h3>
        <div v-if="address && address.address" class="address-card">
          <p class="receiver">{{ address.receiverName || '未设置收货人' }} <span v-if="address.phone">{{ address.phone }}</span></p>
          <p class="addr">{{ address.address }}</p>
        </div>
        <div v-else class="address-card empty">
          <p>未设置收货地址，点击前往设置</p>
        </div>
        <span class="arrow">›</span>
      </section>

      <section class="section items-section">
        <h3>商品明细</h3>
        <div v-for="item in cartItems" :key="item.dishId" class="order-item">
          <div class="item-left">
            <span class="item-name">{{ item.dishName }}</span>
            <span class="item-qty">×{{ item.quantity }}</span>
          </div>
          <span class="item-price">¥{{ (item.unitPrice * item.quantity).toFixed(2) }}</span>
        </div>
      </section>

      <section class="section remark-section">
        <h3>备注</h3>
        <textarea v-model="remark" placeholder="口味偏好、忌口等（选填）" rows="2"></textarea>
      </section>

      <div class="bottom-bar">
        <div class="total">
          合计：<span class="price">¥{{ cartTotal.toFixed(2) }}</span>
        </div>
        <button class="submit-btn" :disabled="submitting" @click="submitOrder">
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.confirm-page {
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
.section {
  background: #fff;
  margin: 12px;
  border-radius: 12px;
  padding: 16px;
}
.section h3 {
  margin: 0 0 10px;
  font-size: 0.95rem;
  color: #333;
}
.address-section {
  cursor: pointer;
  position: relative;
}
.address-card p { margin: 0; }
.receiver { font-weight: 600; font-size: 0.95rem; }
.receiver span { color: #78716c; font-weight: 400; margin-left: 8px; }
.addr { color: #78716c; font-size: 0.88rem; margin-top: 4px; }
.address-card.empty p { color: #c2410c; font-size: 0.9rem; }
.arrow {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #ccc;
  font-size: 1.3rem;
}
.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}
.order-item:last-child { border-bottom: none; }
.item-left { display: flex; gap: 8px; align-items: center; }
.item-name { font-size: 0.93rem; }
.item-qty { color: #78716c; font-size: 0.85rem; }
.item-price { font-weight: 600; color: #c2410c; }
.remark-section textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
  font-size: 0.9rem;
  resize: none;
  font-family: inherit;
}
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 12px 20px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
}
.total { font-size: 0.95rem; }
.price { font-size: 1.2rem; font-weight: 700; color: #c2410c; }
.submit-btn {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 32px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
}
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>

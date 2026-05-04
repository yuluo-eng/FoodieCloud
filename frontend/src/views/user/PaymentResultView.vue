<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isSuccess = computed(() => route.query.status === 'success')
const orderId = route.params.orderId
</script>

<template>
  <div class="result-page">
    <div class="result-card">
      <div class="icon" :class="isSuccess ? 'success' : 'fail'">
        {{ isSuccess ? '✓' : '✕' }}
      </div>
      <h2>{{ isSuccess ? '支付成功' : '支付失败' }}</h2>
      <p v-if="isSuccess" class="desc">订单已支付，商家将尽快处理</p>
      <p v-else class="desc">支付未完成，你可以稍后再试</p>

      <div class="actions">
        <button class="btn-primary" @click="router.push('/user')">
          {{ isSuccess ? '查看订单' : '返回首页' }}
        </button>
        <button v-if="isSuccess" class="btn-ghost" @click="router.push('/user')">
          继续购物
        </button>
        <button v-else class="btn-ghost" @click="router.replace(`/user/payment/${orderId}`)">
          重新支付
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.result-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.result-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  text-align: center;
  width: 100%;
  max-width: 380px;
  box-shadow: 0 8px 30px -8px rgba(0, 0, 0, 0.12);
}
.icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 800;
  margin: 0 auto 16px;
}
.icon.success {
  background: #ecfdf5;
  color: #059669;
}
.icon.fail {
  background: #fef2f2;
  color: #dc2626;
}
h2 {
  margin: 0 0 8px;
  font-size: 1.3rem;
}
.desc {
  color: #78716c;
  margin: 0 0 28px;
  font-size: 0.92rem;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.btn-primary {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
}
.btn-ghost {
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  font-size: 1rem;
  color: #78716c;
  cursor: pointer;
}
</style>

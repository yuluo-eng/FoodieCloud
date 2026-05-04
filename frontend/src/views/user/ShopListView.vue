<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'
import UserOrdersView from './UserOrdersView.vue'

const router = useRouter()
const auth = useAuthStore()
const shops = ref([])
const loading = ref(true)
const error = ref('')
const showOrders = ref(false)

onMounted(async () => {
  try {
    await auth.refreshUserProfile()
  } catch (_) { /* ignore */ }

  try {
    const res = await request.get('/user/shops')
    shops.value = res.data.data || []
  } catch (e) {
    error.value = '加载店铺失败'
  } finally {
    loading.value = false
  }
})

function enterShop(shopId) {
  router.push(`/user/shop/${shopId}`)
}

function logout() {
  auth.logoutUser()
  router.push('/user/login')
}
</script>

<template>
  <div class="shop-list-page">
    <header class="top-bar">
      <h1>悦食汇</h1>
      <div class="top-right">
        <RouterLink v-if="auth.userDisplayLabel" class="user-link" to="/user/profile">
          <img v-if="auth.userAvatarSrc" class="avatar" :src="auth.userAvatarSrc" alt="" />
          <span v-else class="avatar-ph">{{ (auth.userDisplayLabel || '?').slice(0, 1) }}</span>
          <span class="uname">{{ auth.userDisplayLabel }}</span>
        </RouterLink>
        <button class="orders-link" @click="showOrders = !showOrders">
          {{ showOrders ? '返回首页' : '我的订单' }}
        </button>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </header>

    <template v-if="showOrders">
      <UserOrdersView />
    </template>

    <template v-else>
    <div class="banner">
      <h2>选择商家开始点餐</h2>
      <p>为您精选平台入驻商家</p>
    </div>

    <div v-if="loading" class="status-row"><span class="spinner"></span> 加载中…</div>
    <div v-else-if="error" class="status-row error">{{ error }}</div>
    <div v-else-if="shops.length === 0" class="status-row">暂无营业中的商家</div>

    <div v-else class="shop-grid">
      <article v-for="shop in shops" :key="shop.id" class="shop-card" @click="enterShop(shop.id)">
        <div class="shop-icon">🏪</div>
        <div class="shop-body">
          <h3>{{ shop.shopName }}</h3>
          <p v-if="shop.address" class="addr">📍 {{ shop.address }}</p>
          <p v-if="shop.notice" class="notice">{{ shop.notice }}</p>
        </div>
        <span class="arrow">›</span>
      </article>
    </div>
    </template>
  </div>
</template>

<style scoped>
.shop-list-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 0 16px 40px;
}
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
}
.top-bar h1 {
  font-size: 1.4rem;
  margin: 0;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.top-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-link {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: #334155;
  font-size: 0.9rem;
}
.avatar { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.avatar-ph {
  width: 28px; height: 28px; border-radius: 50%; background: #f1f5f9;
  display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 0.8rem; color: #64748b;
}
.orders-link {
  font-size: 0.88rem;
  color: #ff6b35;
  text-decoration: none;
  font-weight: 600;
}
.btn-logout {
  padding: 6px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  font-size: 0.85rem;
}
.banner {
  text-align: center;
  padding: 32px 0 24px;
}
.banner h2 {
  margin: 0 0 6px;
  font-size: 1.5rem;
  color: #1e293b;
}
.banner p {
  margin: 0;
  color: #94a3b8;
  font-size: 0.92rem;
}
.status-row {
  text-align: center;
  padding: 40px 0;
  color: #94a3b8;
}
.status-row.error { color: #dc2626; }
.shop-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.shop-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}
.shop-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.07);
  transform: translateY(-1px);
}
.shop-icon {
  font-size: 2rem;
  flex-shrink: 0;
}
.shop-body {
  flex: 1;
  min-width: 0;
}
.shop-body h3 {
  margin: 0 0 4px;
  font-size: 1.05rem;
  color: #1e293b;
}
.addr {
  margin: 0 0 2px;
  font-size: 0.85rem;
  color: #64748b;
}
.notice {
  margin: 0;
  font-size: 0.82rem;
  color: #94a3b8;
}
.arrow {
  font-size: 1.5rem;
  color: #cbd5e1;
  flex-shrink: 0;
}
@media (max-width: 480px) {
  .shop-list-page { padding: 0 10px 30px; }
  .shop-card { padding: 14px 14px; }
}
</style>

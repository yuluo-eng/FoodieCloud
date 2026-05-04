<template>
  <div class="merchant-home">
    <nav class="sidebar">
      <div class="logo">悦食汇</div>
      <ul class="menu">
        <li :class="{ active: activeTab === 'dashboard' }">
          <a href="#" @click.prevent="activeTab = 'dashboard'">📊 工作台</a>
        </li>
        <li :class="{ active: activeTab === 'employees' }">
          <a href="#" @click.prevent="activeTab = 'employees'">👥 员工管理</a>
        </li>
        <li :class="{ active: activeTab === 'menu' }">
          <a href="#" @click.prevent="activeTab = 'menu'">🍽️ 菜单管理</a>
        </li>
        <li :class="{ active: activeTab === 'shop' }">
          <a href="#" @click.prevent="activeTab = 'shop'">🏪 店铺设置</a>
        </li>
        <li :class="{ active: activeTab === 'orders' }">
          <a href="#" @click.prevent="activeTab = 'orders'">📦 订单管理</a>
        </li>
      </ul>
      <div class="user-info">
        <p class="display-name">{{ auth.merchantDisplayLabel }}</p>
        <p v-if="auth.merchantRoleLabel || auth.merchantProfile?.username" class="sub-line">
          <span v-if="auth.merchantRoleLabel">{{ auth.merchantRoleLabel }}</span>
          <span v-if="auth.merchantRoleLabel && auth.merchantProfile?.username"> · </span>
          <span v-if="auth.merchantProfile?.username">@{{ auth.merchantProfile.username }}</span>
        </p>
        <button @click="logout" class="btn-logout">退出登录</button>
      </div>
    </nav>

    <div class="main-content">
      <div v-if="!ready" style="padding:40px;text-align:center;color:#94a3b8">加载中…</div>
      <div v-else-if="activeTab === 'dashboard'" class="dashboard">
        <h2>工作台</h2>
        <div class="stats">
          <div class="stat-card">
            <div class="stat-value">{{ stats.totalOrders }}</div>
            <div class="stat-label">今日订单</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">¥{{ stats.totalRevenue }}</div>
            <div class="stat-label">今日营收</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stats.totalEmployees }}</div>
            <div class="stat-label">员工数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stats.totalDishes }}</div>
            <div class="stat-label">菜品数</div>
          </div>
        </div>
      </div>

      <EmployeeListView v-else-if="activeTab === 'employees'" />
      <MerchantMenuView v-else-if="activeTab === 'menu'" />
      <ShopSettingsView v-else-if="activeTab === 'shop'" />

      <MerchantOrdersView v-else-if="activeTab === 'orders'" />

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'
import EmployeeListView from './EmployeeListView.vue'
import MerchantMenuView from './MerchantMenuView.vue'
import ShopSettingsView from './ShopSettingsView.vue'
import MerchantOrdersView from './MerchantOrdersView.vue'

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('dashboard')
const ready = ref(false)
const stats = ref({
  totalOrders: 0,
  totalRevenue: 0,
  totalEmployees: 0,
  totalDishes: 0,
})

onMounted(async () => {
  try {
    await auth.refreshMerchantProfile()
  } catch (err) {
    console.error('获取用户信息失败', err)
    router.push('/merchant/login')
    return
  }

  ready.value = true

  try {
    const statsRes = await request.get('/merchant/dashboard/stats', {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    stats.value = statsRes.data.data
  } catch (err) {
    console.error('获取工作台统计失败', err)
  }
})

function logout() {
  auth.logoutMerchant()
  router.push('/merchant/login')
}
</script>

<style scoped>
.merchant-home {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

.sidebar {
  width: 250px;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: white;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 30px;
  text-align: center;
  letter-spacing: 2px;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
}

.menu li {
  margin-bottom: 10px;
}

.menu a {
  display: block;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.3s;
  font-size: 15px;
}

.menu a:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.menu li.active a {
  background: rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
}

.user-info {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 15px;
}

.user-info p {
  margin: 0 0 10px 0;
  font-size: 14px;
  opacity: 0.9;
}

.btn-logout {
  width: 100%;
  padding: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.3);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
}

.dashboard {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.dashboard h2 {
  margin-top: 0;
  color: #333;
  margin-bottom: 25px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.orders {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.orders h2 {
  margin-top: 0;
  color: #333;
}
</style>

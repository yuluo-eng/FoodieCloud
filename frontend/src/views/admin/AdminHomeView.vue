<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminDashboardView from './AdminDashboardView.vue'
import AdminUsersView from './AdminUsersView.vue'
import AdminRidersView from './AdminRidersView.vue'
import AdminOrdersView from './AdminOrdersView.vue'
import AdminShopsView from './AdminShopsView.vue'

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('dashboard')

const adminUser = computed(() => auth.adminProfile?.username || '管理员')

onMounted(() => {
  if (!auth.adminToken) {
    router.replace('/admin/login')
  }
})

function logout() {
  auth.logoutAdmin()
  router.replace('/admin/login')
}

const tabs = [
  { key: 'dashboard', label: '平台数据', icon: '📈' },
  { key: 'users', label: '用户管理', icon: '👤' },
  { key: 'riders', label: '骑手管理', icon: '🚴' },
  { key: 'shops', label: '商家管理', icon: '🏪' },
  { key: 'orders', label: '全局订单', icon: '📋' },
]
</script>

<template>
  <div class="admin-layout">
    <header class="top-bar">
      <div class="bar-left">
        <span class="bar-brand">🛡️ 悦食汇 · 管理后台</span>
        <nav class="bar-nav">
          <button
            v-for="t in tabs"
            :key="t.key"
            :class="['nav-btn', { active: activeTab === t.key }]"
            @click="activeTab = t.key"
          >
            {{ t.icon }} {{ t.label }}
          </button>
        </nav>
      </div>
      <div class="bar-right">
        <span class="admin-name">{{ adminUser }}</span>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </header>

    <main class="admin-main">
      <AdminDashboardView v-if="activeTab === 'dashboard'" />
      <AdminUsersView v-else-if="activeTab === 'users'" />
      <AdminRidersView v-else-if="activeTab === 'riders'" />
      <AdminShopsView v-else-if="activeTab === 'shops'" />
      <AdminOrdersView v-else-if="activeTab === 'orders'" />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f1f5f9;
}
.top-bar {
  background: #1e293b;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 56px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.bar-left {
  display: flex;
  align-items: center;
  gap: 24px;
}
.bar-brand {
  font-weight: 700;
  font-size: 1.05rem;
  white-space: nowrap;
}
.bar-nav {
  display: flex;
  gap: 4px;
}
.nav-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.65);
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
}
.nav-btn:hover { background: rgba(255, 255, 255, 0.1); color: #fff; }
.nav-btn.active { background: rgba(255, 255, 255, 0.15); color: #fff; font-weight: 600; }
.bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-name { font-size: 0.88rem; opacity: 0.8; }
.btn-logout {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
}
.btn-logout:hover { background: rgba(255, 255, 255, 0.2); }
.admin-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
@media (max-width: 768px) {
  .top-bar { flex-wrap: wrap; height: auto; padding: 10px 16px; gap: 8px; }
  .bar-left { flex-wrap: wrap; gap: 8px; }
  .bar-nav { overflow-x: auto; }
  .admin-main { padding: 16px; }
}
</style>

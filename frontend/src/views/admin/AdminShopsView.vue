<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const auth = useAuthStore()
const toast = useToast()
const shops = ref([])
const loading = ref(true)

const authHeader = () => ({ Authorization: `Bearer ${auth.adminToken}` })

onMounted(loadShops)

async function loadShops() {
  loading.value = true
  try {
    const res = await request.get('/admin/shops', { headers: authHeader() })
    shops.value = res.data.data || []
  } catch (e) {
    toast.error('加载商家失败')
  } finally {
    loading.value = false
  }
}

async function toggleStatus(shop) {
  const next = shop.businessStatus === 1 ? 0 : 1
  const label = next === 1 ? '上线' : '下线'
  try {
    await request.patch(`/admin/shops/${shop.id}/toggle`, null, {
      params: { businessStatus: next },
      headers: authHeader(),
    })
    shop.businessStatus = next
    toast.success(`已${label}`)
  } catch (e) {
    toast.error(`操作失败`)
  }
}
</script>

<template>
  <div class="admin-section">
    <h2>商家管理</h2>
    <div v-if="loading" class="tip">加载中…</div>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>店铺名称</th>
          <th>地址</th>
          <th>电话</th>
          <th>公告</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="shop in shops" :key="shop.id">
          <td>{{ shop.id }}</td>
          <td>{{ shop.shopName }}</td>
          <td>{{ shop.address || '-' }}</td>
          <td>{{ shop.phone || '-' }}</td>
          <td class="notice-cell">{{ shop.notice || '-' }}</td>
          <td>
            <span :class="shop.businessStatus === 1 ? 'tag-on' : 'tag-off'">
              {{ shop.businessStatus === 1 ? '营业中' : '已关闭' }}
            </span>
          </td>
          <td>
            <button
              class="btn-action"
              :class="shop.businessStatus === 1 ? 'btn-warn' : 'btn-success'"
              @click="toggleStatus(shop)"
            >
              {{ shop.businessStatus === 1 ? '关闭' : '上线' }}
            </button>
          </td>
        </tr>
        <tr v-if="shops.length === 0">
          <td colspan="7" class="tip">暂无商家</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.admin-section { background: #fff; border-radius: 12px; padding: 24px; }
h2 { margin: 0 0 16px; font-size: 1.15rem; }
.tip { text-align: center; padding: 20px; color: #94a3b8; }
.data-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.data-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 600; }
.data-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; }
.notice-cell { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tag-on { background: #dcfce7; color: #16a34a; padding: 2px 8px; border-radius: 4px; font-size: 0.82rem; font-weight: 600; }
.tag-off { background: #fee2e2; color: #dc2626; padding: 2px 8px; border-radius: 4px; font-size: 0.82rem; font-weight: 600; }
.btn-action {
  padding: 5px 12px; border: none; border-radius: 6px; font-size: 0.82rem; cursor: pointer; font-weight: 600;
}
.btn-warn { background: #fef3c7; color: #d97706; }
.btn-success { background: #dcfce7; color: #16a34a; }
</style>

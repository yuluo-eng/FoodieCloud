<template>
  <section class="orders-wrap">
    <div class="orders-head">
      <h2>订单管理</h2>
      <button class="btn-refresh" @click="loadOrders">刷新</button>
    </div>

    <div class="filters">
      <select v-model.number="statusFilter" @change="loadOrders">
        <option :value="-1">全部状态</option>
        <option :value="1">已支付</option>
        <option :value="2">已接单</option>
        <option :value="3">配送中</option>
        <option :value="4">已完成</option>
        <option :value="5">已取消</option>
      </select>
    </div>

    <div v-if="loading" class="tip">加载中...</div>
    <div v-else-if="orders.length === 0" class="tip">暂无订单</div>

    <div v-else class="order-list">
      <article v-for="order in orders" :key="order.id" class="order-card">
        <div class="head">
          <p class="order-no">订单号：{{ order.orderNo }}</p>
          <span class="status">{{ orderStatusText(order.status) }}</span>
        </div>
        <p>用户ID：{{ order.userId }}</p>
        <p>金额：¥{{ formatMoney(order.totalAmount) }}</p>
        <p>支付：{{ payStatusText(order.payStatus) }}</p>

        <div class="actions">
          <button class="btn" @click="openDetail(order.id)">查看明细</button>
          <button v-if="order.status === 1" class="btn" @click="accept(order.id)">接单</button>
          <button v-if="order.status === 2" class="btn" @click="delivery(order.id)">配送</button>
          <button v-if="order.status === 3" class="btn" @click="finish(order.id)">完成</button>
        </div>
      </article>
    </div>

    <div v-if="detailVisible" class="modal-mask" @click.self="detailVisible = false">
      <div class="modal">
        <h3>订单明细</h3>
        <p v-if="detailLoading" class="tip">加载中...</p>
        <template v-else>
          <p>订单号：{{ detailOrder?.orderNo }}</p>
          <p>金额：¥{{ formatMoney(detailOrder?.totalAmount) }}</p>
          <ul v-if="detailItems.length">
            <li v-for="it in detailItems" :key="it.id">
              <span>{{ it.dishName }}</span>
              <span>¥{{ formatMoney(it.dishPrice) }} × {{ it.quantity }}</span>
              <b>¥{{ formatMoney(it.amount) }}</b>
            </li>
          </ul>
          <p v-else class="tip">暂无明细</p>
        </template>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const orders = ref([])
const statusFilter = ref(-1)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailOrder = ref(null)
const detailItems = ref([])

onMounted(() => {
  if (!auth.merchantToken) {
    router.replace('/merchant/login')
    return
  }
  loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const params = { page: 1, pageSize: 50, shopId: 1 }
    if (statusFilter.value !== -1) params.status = statusFilter.value
    const res = await request.get('/merchant/orders', {
      params,
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    orders.value = res.data.data?.records || []
  } catch (e) {
    console.error('加载商家订单失败', e)
    const msg = e?.message || ''
    if (msg.includes('401') || msg.includes('未登录') || msg.includes('Token')) {
      auth.logoutMerchant()
      router.replace('/merchant/login')
    }
  } finally {
    loading.value = false
  }
}

async function accept(orderId) {
  await action(`/merchant/orders/${orderId}/accept`, '接单成功')
}

async function delivery(orderId) {
  await action(`/merchant/orders/${orderId}/delivery`, '已发起配送')
}

async function finish(orderId) {
  await action(`/merchant/orders/${orderId}/finish`, '订单已完成')
}

async function openDetail(orderId) {
  detailVisible.value = true
  detailLoading.value = true
  detailOrder.value = null
  detailItems.value = []
  try {
    const res = await request.get(`/merchant/orders/${orderId}`, {
      params: { shopId: 1 },
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    detailOrder.value = res.data.data?.order || null
    detailItems.value = res.data.data?.items || []
  } catch (e) {
    console.error('加载订单明细失败', e)
  } finally {
    detailLoading.value = false
  }
}

async function action(url, msg) {
  try {
    await request.patch(url, {}, {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    alert(msg)
    await loadOrders()
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

function orderStatusText(status) {
  return {
    0: '待支付',
    1: '已支付',
    2: '已接单',
    3: '配送中',
    4: '已完成',
    5: '已取消',
  }[status] || '未知状态'
}

function payStatusText(status) {
  return {
    0: '未支付',
    1: '已支付',
    2: '已退款',
  }[status] || '未知'
}

function formatMoney(v) {
  return Number(v || 0).toFixed(2)
}
</script>

<style scoped>
.orders-wrap {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.orders-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filters {
  margin: 12px 0 16px;
}
.filters select {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 8px 10px;
}
.order-list {
  display: grid;
  gap: 12px;
}
.order-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-no {
  margin: 0;
  font-weight: 600;
}
.status {
  font-weight: 600;
}
.actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}
.btn {
  border: none;
  background: #2563eb;
  color: #fff;
  border-radius: 8px;
  padding: 7px 12px;
  cursor: pointer;
}
.btn-refresh {
  border: none;
  background: #f3f4f6;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}
.tip { color: #888; }

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.modal {
  width: 92%;
  max-width: 520px;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}

.modal ul {
  list-style: none;
  padding: 0;
  margin: 8px 0 0;
}

.modal li {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 10px;
  padding: 6px 0;
  border-bottom: 1px dashed #e5e7eb;
}
</style>

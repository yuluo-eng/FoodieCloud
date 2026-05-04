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
        <p>金额：¥{{ formatMoney(order.totalAmount) }}</p>
        <p>支付：{{ payStatusText(order.payStatus) }}</p>
        <div v-if="order.riderId" class="rider-tag">
          🚴 {{ order.riderName || '骑手' }} · {{ riderStatusText(order) }}
        </div>
        <div v-else-if="order.status >= 1 && order.status <= 3" class="rider-tag waiting">
          {{ order.status === 1 ? '等待骑手接单' : '商家自配送' }}
        </div>

        <div class="actions">
          <button class="btn" @click="openDetail(order.id)">查看明细</button>
          <button v-if="order.status === 1" class="btn" @click="showAcceptModal(order.id)">接单</button>
          <button v-if="order.status === 2" class="btn" @click="delivery(order.id)">配送</button>
          <button v-if="order.status === 3" class="btn" @click="finish(order.id)">完成</button>
        </div>
      </article>
    </div>

    <div v-if="acceptModalVisible" class="modal-mask" @click.self="acceptModalVisible = false">
      <div class="modal accept-modal">
        <h3>选择配送方式</h3>
        <p class="accept-desc">请选择该订单的配送方式：</p>
        <div class="accept-btns">
          <button class="mode-btn self" @click="confirmAccept('SELF')">
            <span class="mode-icon">🏪</span>
            <span class="mode-title">商家自配送</span>
            <span class="mode-sub">由本店负责配送</span>
          </button>
          <button class="mode-btn rider" @click="confirmAccept('RIDER')">
            <span class="mode-icon">🚴</span>
            <span class="mode-title">骑手配送</span>
            <span class="mode-sub">推送至骑手抢单池</span>
          </button>
        </div>
        <button class="cancel-btn" @click="acceptModalVisible = false">取消</button>
      </div>
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
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const loading = ref(false)
const orders = ref([])
const statusFilter = ref(-1)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailOrder = ref(null)
const detailItems = ref([])
const acceptModalVisible = ref(false)
const acceptOrderId = ref(null)

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
    const params = { page: 1, pageSize: 50, shopId: auth.merchantProfile?.shopId }
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

function showAcceptModal(orderId) {
  acceptOrderId.value = orderId
  acceptModalVisible.value = true
}

async function confirmAccept(mode) {
  acceptModalVisible.value = false
  const id = acceptOrderId.value
  const msg = mode === 'RIDER' ? '已推送至骑手抢单池' : '接单成功（自配送）'
  try {
    await request.patch(`/merchant/orders/${id}/accept`, {}, {
      params: { deliveryMode: mode },
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    toast.success(msg)
    await loadOrders()
  } catch (e) {
    toast.error(e.message || '操作失败')
  }
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
      params: { shopId: auth.merchantProfile?.shopId },
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
    toast.success(msg)
    await loadOrders()
  } catch (e) {
    toast.error(e.message || '操作失败')
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

function riderStatusText(order) {
  if (order.riderDeliveredTime) return '已送达'
  if (order.riderPickupTime) return '配送中'
  if (order.riderArriveShopTime) return '已到店'
  if (order.riderAcceptTime) return '已接单'
  return '等待接单'
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

.accept-modal { text-align: center; }
.accept-modal h3 { margin: 0 0 4px; }
.accept-desc { color: #6b7280; font-size: 0.9rem; margin: 0 0 16px; }
.accept-btns { display: flex; gap: 12px; margin-bottom: 12px; }
.mode-btn {
  flex: 1;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  padding: 18px 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  transition: border-color 0.2s, background 0.2s;
}
.mode-btn:hover { border-color: #2563eb; background: #eff6ff; }
.mode-btn.rider:hover { border-color: #059669; background: #ecfdf5; }
.mode-icon { font-size: 1.6rem; }
.mode-title { font-weight: 700; font-size: 0.95rem; }
.mode-sub { font-size: 0.78rem; color: #6b7280; }
.cancel-btn {
  border: none;
  background: none;
  color: #6b7280;
  font-size: 0.9rem;
  cursor: pointer;
  padding: 8px 16px;
}

.rider-tag {
  display: inline-block;
  margin-top: 6px;
  padding: 4px 10px;
  background: #eff6ff;
  color: #2563eb;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
}
.rider-tag.waiting {
  background: #fef9c3;
  color: #a16207;
}
@media (max-width: 640px) {
  .orders-wrap { padding: 12px; }
  .actions { flex-wrap: wrap; }
  .actions .btn { flex: 1; min-width: 0; text-align: center; }
}
</style>

<template>
  <div class="rider-home">
    <header class="top">
      <div>
        <h1>骑手端</h1>
        <p>{{ auth.riderDisplayLabel || '骑手' }}</p>
      </div>
      <div class="right">
        <select :value="workStatus" @change="onSwitchStatus($event)">
          <option value="ONLINE">在线接单</option>
          <option value="OFFLINE">离线休息</option>
        </select>
        <button class="btn" @click="logout">退出</button>
      </div>
    </header>

    <section class="panel">
      <div class="tabs">
        <button :class="{ active: tab === 'dispatch' }" @click="tab = 'dispatch'">可接订单</button>
        <button :class="{ active: tab === 'current' }" @click="tab = 'current'">我的进行中</button>
      </div>

      <div v-if="loading" class="loading-row"><span class="spinner"></span> 加载中…</div>
      <div v-else-if="activeList.length === 0" class="tip empty-tip">暂无订单，下拉刷新试试</div>
      <div v-else class="cards">
        <article v-for="order in activeList" :key="order.id" class="card">
          <div class="head">
            <p>#{{ order.orderNo }}</p>
            <span>{{ statusText(order.status) }}</span>
          </div>
          <p class="amount">¥{{ formatMoney(order.totalAmount) }}</p>
          <div class="info-block">
            <p class="info-title">🏪 商家</p>
            <p>{{ order.shopName || `店铺#${order.shopId}` }}</p>
            <p v-if="order.shopAddress" class="sub">{{ order.shopAddress }}</p>
            <a v-if="order.shopPhone" :href="'tel:' + order.shopPhone" class="phone-link">📞 {{ order.shopPhone }}</a>
          </div>
          <div v-if="order.shippingAddress || order.receiverName" class="info-block">
            <p class="info-title">📍 收货人</p>
            <p>{{ order.receiverName || '未设置' }} <span v-if="order.shippingPhoneNo" class="sub">{{ order.shippingPhoneNo }}</span></p>
            <p v-if="order.shippingAddress" class="sub">{{ order.shippingAddress }}</p>
          </div>
          <div class="actions">
            <button v-if="tab === 'dispatch'" class="btn primary" @click="accept(order.id)">接单</button>
            <template v-else>
              <button v-if="order.status === 2" class="btn" @click="arrive(order.id)">到店</button>
              <button v-if="order.status === 3 && !order.riderPickupTime" class="btn" @click="pickup(order.id)">取餐</button>
              <button v-if="order.status === 3 && order.riderPickupTime" class="btn success" @click="showDeliverConfirm(order)">送达</button>
            </template>
          </div>
        </article>
      </div>
    </section>

    <div v-if="deliverConfirmVisible" class="modal-mask" @click.self="deliverConfirmVisible = false">
      <div class="confirm-modal">
        <div class="confirm-icon">📦</div>
        <h3>确认送达？</h3>
        <p class="confirm-order">订单 #{{ deliverConfirmOrder?.orderNo }}</p>
        <p class="confirm-addr" v-if="deliverConfirmOrder?.shippingAddress">{{ deliverConfirmOrder.shippingAddress }}</p>
        <p class="confirm-warn">请确认已将餐品交给顾客后再点击确认</p>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="deliverConfirmVisible = false">取消</button>
          <button class="btn-confirm" @click="confirmDeliver">确认送达</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  fetchCurrentOrders,
  fetchDispatchOrders,
  riderAcceptOrder,
  riderArriveShop,
  riderDelivered,
  riderMe,
  riderPickup,
  updateRiderWorkStatus,
} from '@/api/rider'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const tab = ref('dispatch')
const loading = ref(false)
const dispatchOrders = ref([])
const currentOrders = ref([])
const workStatus = ref(auth.riderProfile?.workStatus || 'ONLINE')
const deliverConfirmVisible = ref(false)
const deliverConfirmOrder = ref(null)

const activeList = computed(() => (tab.value === 'dispatch' ? dispatchOrders.value : currentOrders.value))

onMounted(async () => {
  await syncRiderProfile()
  await loadData()
})

async function syncRiderProfile() {
  try {
    const res = await riderMe(auth.riderToken)
    auth.setRiderFromLogin(res.data.data)
    workStatus.value = res.data.data?.workStatus || 'ONLINE'
  } catch (e) {
    console.error('获取骑手资料失败', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const [dispatchRes, currentRes] = await Promise.all([
      fetchDispatchOrders(auth.riderToken, { page: 1, pageSize: 50 }),
      fetchCurrentOrders(auth.riderToken),
    ])
    dispatchOrders.value = dispatchRes.data.data?.records || []
    currentOrders.value = currentRes.data.data?.records || []
  } catch (e) {
    console.error('加载骑手订单失败', e)
  } finally {
    loading.value = false
  }
}

async function accept(orderId) {
  try {
    await riderAcceptOrder(auth.riderToken, orderId)
    await loadData()
  } catch (e) {
    toast.error(e.message || '接单失败')
  }
}

async function arrive(orderId) {
  try {
    await riderArriveShop(auth.riderToken, orderId)
    await loadData()
  } catch (e) {
    toast.error(e.message || '操作失败')
  }
}

async function pickup(orderId) {
  try {
    await riderPickup(auth.riderToken, orderId)
    await loadData()
  } catch (e) {
    toast.error(e.message || '操作失败')
  }
}

function showDeliverConfirm(order) {
  deliverConfirmOrder.value = order
  deliverConfirmVisible.value = true
}

async function confirmDeliver() {
  deliverConfirmVisible.value = false
  const orderId = deliverConfirmOrder.value?.id
  if (!orderId) return
  try {
    await riderDelivered(auth.riderToken, orderId)
    toast.success('送达成功')
    await loadData()
  } catch (e) {
    toast.error(e.message || '操作失败')
  }
}

async function onSwitchStatus(event) {
  const next = event.target.value
  try {
    await updateRiderWorkStatus(auth.riderToken, next)
    workStatus.value = next
    auth.riderProfile = {
      ...(auth.riderProfile || {}),
      workStatus: next,
    }
    await loadData()
  } catch (e) {
    toast.error(e.message || '状态切换失败')
  }
}

function logout() {
  auth.logoutRider()
  router.replace('/rider/login')
}

function statusText(status) {
  return {
    1: '待接单',
    2: '已接单',
    3: '已到店',
    4: '已送达',
  }[status] || `状态${status}`
}

function formatMoney(v) {
  return Number(v || 0).toFixed(2)
}
</script>

<style scoped>
.rider-home {
  min-height: 100vh;
  background: #f8fafc;
  padding: 12px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}
.top {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.top h1 { margin: 0; font-size: 20px; }
.top p { margin: 4px 0 0; color: #64748b; font-size: 0.9rem; }
.right { display: flex; gap: 8px; align-items: center; }
.right select, .btn {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 14px;
  background: #fff;
  font-size: 0.92rem;
  min-height: 44px;
  cursor: pointer;
  touch-action: manipulation;
}
.panel {
  margin-top: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}
.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.tabs button {
  flex: 1;
  border: none;
  border-radius: 10px;
  padding: 10px 14px;
  background: #f1f5f9;
  font-size: 0.95rem;
  font-weight: 600;
  min-height: 44px;
  cursor: pointer;
  touch-action: manipulation;
  transition: background 0.15s, color 0.15s;
}
.tabs button.active {
  background: #2563eb;
  color: #fff;
}
.cards {
  display: grid;
  gap: 10px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  max-height: calc(100vh - 220px);
}
.card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.head p { margin: 0; font-weight: 600; }
.actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.actions .btn {
  flex: 1;
  min-width: 0;
  text-align: center;
  justify-content: center;
}
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
.btn.success { background: #16a34a; color: #fff; border-color: #16a34a; }
.tip { color: #64748b; text-align: center; padding: 2rem 0; }
.loading-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem 0;
  color: #64748b;
}
.empty-tip { font-size: 0.95rem; }
.amount { font-weight: 700; color: #c2410c; margin: 6px 0; }
.info-block {
  margin-top: 8px;
  padding: 8px 10px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 0.88rem;
}
.info-block p { margin: 0 0 2px; }
.info-title { font-weight: 600; color: #334155; margin-bottom: 4px !important; }
.sub { color: #64748b; font-size: 0.82rem; }
.phone-link {
  display: inline-block;
  margin-top: 4px;
  color: #2563eb;
  text-decoration: none;
  font-size: 0.85rem;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
  padding: 20px;
}
.confirm-modal {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px;
  text-align: center;
  width: 100%;
  max-width: 340px;
}
.confirm-icon { font-size: 2rem; margin-bottom: 8px; }
.confirm-modal h3 { margin: 0 0 8px; font-size: 1.15rem; }
.confirm-order { color: #334155; font-weight: 600; margin: 0 0 4px; font-size: 0.92rem; }
.confirm-addr { color: #64748b; font-size: 0.85rem; margin: 0 0 12px; }
.confirm-warn {
  color: #dc2626;
  font-size: 0.85rem;
  margin: 0 0 20px;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: 8px;
}
.confirm-actions { display: flex; gap: 10px; }
.btn-cancel {
  flex: 1;
  padding: 11px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  font-size: 0.95rem;
  cursor: pointer;
}
.btn-confirm {
  flex: 1;
  padding: 11px;
  border: none;
  border-radius: 10px;
  background: #16a34a;
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
}

@media (max-width: 480px) {
  .top { flex-direction: column; align-items: stretch; }
  .right { justify-content: space-between; }
}
</style>

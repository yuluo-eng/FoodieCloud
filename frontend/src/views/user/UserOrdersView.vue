<template>
  <section class="orders-wrap">
    <div class="orders-head">
      <h2>我的订单</h2>
      <button class="btn-refresh" @click="loadOrders">刷新</button>
    </div>

    <div v-if="loading" class="tip">加载中...</div>
    <div v-else-if="orders.length === 0" class="tip">暂无订单</div>

    <div v-else class="order-list">
      <article v-for="order in orders" :key="order.id" class="order-card">
        <header class="card-head">
          <div>
            <p class="order-no">订单号：{{ order.orderNo }}</p>
            <p class="order-time">创建时间：{{ order.createTime || '-' }}</p>
          </div>
          <div class="status-box">
            <span class="status">{{ orderStatusText(order.status) }}</span>
            <span class="pay">支付：{{ payStatusText(order.payStatus) }}</span>
          </div>
        </header>

        <div class="card-body">
          <p>金额：<b>¥{{ formatMoney(order.totalAmount) }}</b></p>
          <p v-if="order.remark">备注：{{ order.remark }}</p>
        </div>

        <div v-if="order.riderId && order.status >= 2 && order.status <= 4" class="delivery-section">
          <div class="rider-info">
            <span class="rider-label">🚴 骑手：</span>
            <span>{{ order.riderName || '未知' }}</span>
            <a v-if="order.riderPhone" :href="'tel:' + order.riderPhone" class="phone-link">{{ order.riderPhone }}</a>
          </div>
          <div class="progress-bar">
            <div class="step" :class="{ done: order.riderAcceptTime }"><span class="dot"></span><span>已接单</span></div>
            <div class="step" :class="{ done: order.riderArriveShopTime }"><span class="dot"></span><span>已到店</span></div>
            <div class="step" :class="{ done: order.riderPickupTime }"><span class="dot"></span><span>已取餐</span></div>
            <div class="step" :class="{ done: order.riderDeliveredTime }"><span class="dot"></span><span>已送达</span></div>
          </div>
        </div>

        <footer class="card-actions">
          <button class="btn" @click="toggleDetail(order.id)">
            {{ detailMap[order.id] ? '收起明细' : '查看明细' }}
          </button>

          <button
            v-if="order.status === 0 && order.payStatus === 0"
            class="btn warn"
            @click="cancelOrder(order.id)"
          >
            取消订单
          </button>

          <button
            v-if="order.status === 0 && order.payStatus === 0 && !paymentNoMap[order.id]"
            class="btn primary"
            @click="createPayment(order.id)"
          >
            创建支付单
          </button>

          <button
            v-if="paymentNoMap[order.id] && order.payStatus === 0"
            class="btn success"
            @click="mockPaySuccess(paymentNoMap[order.id], order.id)"
          >
            模拟支付成功
          </button>

          <button
            v-if="paymentNoMap[order.id]"
            class="btn"
            @click="queryPayStatus(paymentNoMap[order.id])"
          >
            查询支付状态
          </button>
        </footer>

        <p v-if="paymentNoMap[order.id]" class="payment-no">
          支付单：{{ paymentNoMap[order.id] }}
        </p>

        <div v-if="detailMap[order.id]" class="detail-box">
          <div v-if="detailLoadingMap[order.id]" class="tip">明细加载中...</div>
          <template v-else>
            <div v-if="!detailItemsMap[order.id] || detailItemsMap[order.id].length === 0" class="tip">
              暂无明细
            </div>
            <ul v-else>
              <li v-for="item in detailItemsMap[order.id]" :key="item.id">
                <span>{{ item.dishName }}</span>
                <span>¥{{ formatMoney(item.dishPrice) }} × {{ item.quantity }}</span>
                <b>¥{{ formatMoney(item.amount) }}</b>
              </li>
            </ul>
          </template>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const toast = useToast()

const auth = useAuthStore()
const loading = ref(false)
const orders = ref([])
const detailMap = ref({})
const detailLoadingMap = ref({})
const detailItemsMap = ref({})
const paymentNoMap = ref({})

onMounted(loadOrders)

async function loadOrders() {
  loading.value = true
  try {
    const res = await request.get('/user/orders', {
      params: { page: 1, pageSize: 50 },
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    orders.value = res.data.data?.records || []
  } catch (e) {
    console.error('加载订单失败', e)
  } finally {
    loading.value = false
  }
}

async function toggleDetail(orderId) {
  const open = !!detailMap.value[orderId]
  detailMap.value = { ...detailMap.value, [orderId]: !open }
  if (!open && !detailItemsMap.value[orderId]) {
    detailLoadingMap.value = { ...detailLoadingMap.value, [orderId]: true }
    try {
      const res = await request.get(`/user/orders/${orderId}`, {
        headers: { Authorization: `Bearer ${auth.userToken}` },
      })
      const items = res.data.data?.items || []
      detailItemsMap.value = { ...detailItemsMap.value, [orderId]: items }
    } catch (e) {
      console.error('加载订单明细失败', e)
    } finally {
      detailLoadingMap.value = { ...detailLoadingMap.value, [orderId]: false }
    }
  }
}

async function cancelOrder(orderId) {
  if (!window.confirm('确认取消该订单？')) return
  try {
    await request.patch(`/user/orders/${orderId}/cancel`, {}, {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    await loadOrders()
  } catch (e) {
    toast.error(e.message || '取消失败')
  }
}

async function createPayment(orderId) {
  try {
    const res = await request.post('/user/payments/create', {
      orderId,
      payChannel: 'MOCK',
    }, {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    const paymentNo = res.data.data?.paymentNo
    if (paymentNo) {
      paymentNoMap.value = { ...paymentNoMap.value, [orderId]: paymentNo }
      toast.success(`支付单创建成功：${paymentNo}`)
    }
  } catch (e) {
    toast.error(e.message || '创建支付单失败')
  }
}

async function mockPaySuccess(paymentNo, orderId) {
  try {
    await request.post('/user/payments/mock-success', { paymentNo }, {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    await loadOrders()
    await queryPayStatus(paymentNo)
  } catch (e) {
    toast.error(e.message || '模拟支付失败')
  }
}

async function queryPayStatus(paymentNo) {
  try {
    const res = await request.get(`/user/payments/${paymentNo}/status`, {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    const payStatus = res.data.data?.payStatus
    toast.info(`支付状态：${payStatusText(payStatus)}`)
  } catch (e) {
    toast.error(e.message || '查询支付状态失败')
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
  const n = Number(v || 0)
  return n.toFixed(2)
}
</script>

<style scoped>
.orders-wrap {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
}
.orders-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.btn-refresh {
  border: none;
  background: #f3f4f6;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
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
.card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.order-no {
  font-weight: 600;
  margin: 0;
}
.order-time {
  margin: 4px 0 0;
  color: #888;
  font-size: 12px;
}
.status-box {
  text-align: right;
}
.status {
  display: block;
  font-weight: 600;
}
.pay {
  color: #666;
  font-size: 12px;
}
.card-body {
  margin-top: 10px;
  color: #444;
}
.card-actions {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.btn {
  border: none;
  border-radius: 8px;
  padding: 7px 10px;
  background: #eef2ff;
  cursor: pointer;
}
.btn.primary { background: #2563eb; color: #fff; }
.btn.success { background: #16a34a; color: #fff; }
.btn.warn { background: #ea580c; color: #fff; }
.payment-no {
  margin: 8px 0 0;
  color: #4b5563;
  font-size: 12px;
}
.detail-box {
  margin-top: 10px;
  border-top: 1px dashed #e5e7eb;
  padding-top: 10px;
}
.detail-box ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.detail-box li {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  padding: 6px 0;
}
.tip { color: #888; }

.delivery-section {
  margin-top: 12px;
  padding: 10px 12px;
  background: #f0f9ff;
  border-radius: 8px;
}
.rider-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  margin-bottom: 10px;
}
.rider-label { font-weight: 600; }
.phone-link {
  color: #2563eb;
  text-decoration: none;
  margin-left: auto;
}
.progress-bar {
  display: flex;
  justify-content: space-between;
}
.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
  font-size: 0.78rem;
  color: #9ca3af;
  position: relative;
}
.step .dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d1d5db;
  transition: background 0.2s;
}
.step.done .dot { background: #2563eb; }
.step.done { color: #2563eb; font-weight: 600; }
.step + .step::before {
  content: '';
  position: absolute;
  top: 6px;
  right: 50%;
  width: 100%;
  height: 2px;
  background: #d1d5db;
  z-index: 0;
}
.step.done + .step.done::before,
.step.done + .step::before { background: #2563eb; }
</style>

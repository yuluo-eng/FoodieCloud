<template>
  <div class="user-home">
    <nav class="top-nav">
      <div class="nav-left">
        <h1>🍽️ 悦食汇</h1>
      </div>
      <div class="nav-right">
        <RouterLink v-if="auth.userDisplayLabel" class="identity identity-link" to="/user/profile" title="个人资料">
          <img v-if="auth.userAvatarSrc" class="identity-avatar" :src="auth.userAvatarSrc" alt="" />
          <span v-else class="identity-avatar-ph">{{ (auth.userDisplayLabel || '?').slice(0, 1) }}</span>
          <span class="identity-name">{{ auth.userDisplayLabel }}</span>
          <span class="identity-tag">顾客</span>
        </RouterLink>
        <button class="btn-cart" @click="showCart = true">
          🛒 购物车 <span v-if="cartCount > 0" class="badge">{{ cartCount }}</span>
        </button>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </nav>

    <div class="user-content">
      <div class="dishes-section">
        <h2>菜品列表</h2>
        <div class="section-switch">
          <button :class="{ active: activeView === 'dishes' }" @click="activeView = 'dishes'">点餐</button>
          <button :class="{ active: activeView === 'orders' }" @click="activeView = 'orders'">我的订单</button>
        </div>

        <UserOrdersView v-if="activeView === 'orders'" />

        <template v-else>
        <MenuBoard
          v-model:selected-category="selectedCategory"
          :categories="categoryTabs"
          :dishes="dishes"
          :load-error="loadError"
          mode="user"
          @add-cart="addToCart"
        />
        </template>
      </div>
    </div>

    <div v-if="showCart" class="cart-modal" @click.self="showCart = false">
      <div class="cart-panel">
        <div class="cart-header">
          <h2>购物车</h2>
          <button class="close-btn" @click="showCart = false">✕</button>
        </div>

        <div v-if="cartItems.length === 0" class="cart-empty">购物车为空</div>
        <div v-else class="cart-items">
          <div class="cart-tools">
            <label class="check-all">
              <input type="checkbox" :checked="allSelected" @change="toggleAllSelected($event)" /> 全选
            </label>
            <button class="btn-clear" @click="clearCart">清空购物车</button>
          </div>
          <div v-for="item in cartItems" :key="item.dishId" class="cart-item">
            <div class="item-info">
              <h4>
                <input type="checkbox" :checked="item.selected === 1" @change="toggleSelected(item.dishId, $event)" />
                {{ item.dishName }}
              </h4>
              <p>¥{{ item.unitPrice.toFixed(2) }} × {{ item.quantity }}</p>
            </div>
            <div class="item-controls">
              <button @click="decreaseQuantity(item.dishId)">−</button>
              <span>{{ item.quantity }}</span>
              <button @click="increaseQuantity(item.dishId)">+</button>
              <button class="btn-remove" @click="removeFromCart(item.dishId)">删除</button>
            </div>
          </div>
        </div>

        <div v-if="cartItems.length > 0" class="cart-footer">
          <div class="total">
            <span>合计：</span>
            <span class="total-price">¥{{ cartTotal.toFixed(2) }}</span>
          </div>
          <button class="btn-checkout" @click="checkout">去结算</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'
import UserOrdersView from './UserOrdersView.vue'
import MenuBoard from '@/components/menu/MenuBoard.vue'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const showCart = ref(false)
const activeView = ref('dishes')
const selectedCategory = ref(null)
const dishes = ref([])
const cartItems = ref([])
const categories = ref([])
const loadError = ref('')

const categoryTabs = computed(() =>
  categories.value.map((c) => ({ ...c, active: true }))
)

const cartCount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const cartTotal = computed(() => {
  return cartItems.value
    .filter(item => item.selected === 1)
    .reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
})

const allSelected = computed(() => {
  return cartItems.value.length > 0 && cartItems.value.every(item => item.selected === 1)
})

onMounted(async () => {
  try {
    await auth.refreshUserProfile()
  } catch (e) {
    console.error('同步用户信息失败', e)
  }

  try {
    loadError.value = ''
    const res = await request.get('/user/dishes', {
      params: { shopId: 1 },
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    dishes.value = res.data.data || []

    const catRes = await request.get('/user/categories', {
      params: { shopId: 1 },
    })
    const remoteCategories = (catRes.data.data || [])
      .filter(c => Number(c.status) === 1)
      .map(c => {
        const id = Number(c.id)
        const rawName = String(c.categoryName ?? '').trim()
        const sort = Number(c.sort ?? 0)
        return {
          id,
          name: rawName || `分类${id}`,
          sort,
        }
      })

    // 强制按后端 sort/id 再排序，避免返回顺序不一致导致按钮“顺序错乱”
    categories.value = remoteCategories.sort((a, b) => {
      const ds = a.sort - b.sort
      return ds !== 0 ? ds : a.id - b.id
    })

    const validCatIdSet = new Set(categories.value.map(c => c.id))
    const hasMatchedDish = dishes.value.some(d => validCatIdSet.has(Number(d.categoryId)))
    if (validCatIdSet.size > 0 && hasMatchedDish) {
      dishes.value = dishes.value.filter(d => validCatIdSet.has(Number(d.categoryId)))
      selectedCategory.value = categories.value[0]?.id ?? null
    } else {
      selectedCategory.value = null
    }
  } catch (err) {
    console.error('获取菜品列表失败', err)
    loadError.value = '菜品加载失败，请检查后端接口或登录状态'
    dishes.value = []
    categories.value = []
    selectedCategory.value = null
  }

  loadCart()
})

async function loadCart() {
  try {
    const res = await request.get('/user/cart', {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    cartItems.value = res.data.data || []
  } catch (err) {
    console.error('获取购物车失败', err)
  }
}

async function addToCart(dish) {
  try {
    await request.post(
      '/user/cart',
      { dishId: dish.id, quantity: 1 },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )
    const existing = cartItems.value.find(item => item.dishId === dish.id)
    if (existing) {
      existing.quantity += 1
    } else {
      cartItems.value.push({
        dishId: dish.id,
        dishName: dish.dishName,
        unitPrice: dish.price,
        quantity: 1,
        selected: 1,
      })
    }
  } catch (err) {
    console.error('加入购物车失败', err)
  }
}

async function increaseQuantity(dishId) {
  const item = cartItems.value.find(i => i.dishId === dishId)
  if (item) {
    item.quantity += 1
    await updateCart(dishId, item.quantity)
  }
}

async function decreaseQuantity(dishId) {
  const item = cartItems.value.find(i => i.dishId === dishId)
  if (item && item.quantity > 1) {
    item.quantity -= 1
    await updateCart(dishId, item.quantity)
  }
}

async function removeFromCart(dishId) {
  try {
    await request.delete(`/user/cart/${dishId}`, {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    cartItems.value = cartItems.value.filter(item => item.dishId !== dishId)
  } catch (err) {
    console.error('删除购物车项失败', err)
  }
}

async function toggleSelected(dishId, event) {
  const selected = event.target.checked ? 1 : 0
  try {
    await request.patch(
      `/user/cart/${dishId}/selected`,
      { selected },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )
    cartItems.value = cartItems.value.map(item => item.dishId === dishId ? { ...item, selected } : item)
  } catch (err) {
    console.error('更新勾选状态失败', err)
  }
}

async function toggleAllSelected(event) {
  const selected = event.target.checked ? 1 : 0
  const tasks = cartItems.value.map(item =>
    request.patch(
      `/user/cart/${item.dishId}/selected`,
      { selected },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )
  )
  try {
    await Promise.all(tasks)
    cartItems.value = cartItems.value.map(item => ({ ...item, selected }))
  } catch (err) {
    console.error('批量更新勾选状态失败', err)
  }
}

async function clearCart() {
  if (!window.confirm('确认清空购物车？')) return
  try {
    await request.delete('/user/cart/clear', {
      headers: { Authorization: `Bearer ${auth.userToken}` },
    })
    cartItems.value = []
  } catch (err) {
    console.error('清空购物车失败', err)
  }
}

async function updateCart(dishId, quantity) {
  try {
    await request.put(
      `/user/cart/${dishId}`,
      { quantity },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )
  } catch (err) {
    console.error('更新购物车失败', err)
  }
}

async function checkout() {
  try {
    const res = await request.post(
      '/user/orders',
      { shopId: 1, remark: '' },
      { headers: { Authorization: `Bearer ${auth.userToken}` } }
    )
    toast.success('下单成功！订单号：' + res.data.data.orderNo)
    cartItems.value = []
    showCart.value = false
  } catch (err) {
    console.error('下单失败', err)
    toast.error('下单失败，请重试')
  }
}

function logout() {
  auth.logoutUser()
  router.push('/user/login')
}
</script>

<style scoped>
.user-home {
  min-height: 100vh;
  background: #f9f9f9;
}

.top-nav {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: white;
  padding: 15px 30px;
  padding-top: calc(15px + env(safe-area-inset-top));
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-wrap: wrap;
  gap: 10px;
}

.nav-left h1 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 1px;
}

.nav-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.identity {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  font-size: 14px;
}

.identity-link {
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

.identity-link:hover {
  background: rgba(255, 255, 255, 0.22);
}

.identity-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.identity-avatar-ph {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.identity-name {
  font-weight: 600;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.identity-tag {
  font-size: 11px;
  opacity: 0.9;
  padding: 2px 6px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 4px;
}

.btn-cart,
.btn-logout {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-cart:hover,
.btn-logout:hover {
  background: rgba(255, 255, 255, 0.3);
}

.badge {
  background: #e74c3c;
  color: white;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 12px;
  margin-left: 4px;
}

.user-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.dishes-section h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 12px;
}

.section-switch {
  display: inline-flex;
  gap: 8px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 16px;
}

.section-switch button {
  border: none;
  background: transparent;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
  color: #475569;
  font-weight: 600;
}

.section-switch button.active {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
}

.cart-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: flex-end;
  z-index: 1000;
}

.cart-panel {
  background: white;
  width: 100%;
  max-width: 400px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.cart-header h2 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.cart-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 16px;
}

.cart-items {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.cart-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

.check-all {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #475569;
}

.btn-clear {
  border: 1px solid #fecaca;
  background: #fff;
  color: #ef4444;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #e0e0e0;
}

.item-info h4 {
  margin: 0 0 4px 0;
  color: #333;
  font-size: 14px;
}

.item-info p {
  margin: 0;
  color: #999;
  font-size: 12px;
}

.item-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-controls button {
  background: #f0f0f0;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  touch-action: manipulation;
}

.btn-remove {
  background: #e74c3c;
  color: white;
  font-size: 11px;
  padding: 4px 8px;
  width: auto;
}

.cart-footer {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.total {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.total-price {
  font-weight: 600;
  color: #ff6b35;
  font-size: 18px;
}

.btn-checkout {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: transform 0.2s;
}

.btn-checkout:hover {
  transform: translateY(-2px);
}

.cart-items {
  -webkit-overflow-scrolling: touch;
}

.cart-footer {
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
}

@media (max-width: 640px) {
  .top-nav {
    padding: 12px 14px;
    padding-top: calc(12px + env(safe-area-inset-top));
  }
  .nav-left h1 { font-size: 18px; }
  .nav-right { gap: 8px; flex-wrap: wrap; }
  .identity { padding: 4px 8px; font-size: 12px; }
  .identity-name { max-width: 80px; }
  .identity-tag { display: none; }
  .user-content { padding: 16px 10px; }
  .dishes-section h2 { font-size: 18px; }
  .cart-panel { max-width: 100%; }
  .btn-cart, .btn-logout { padding: 8px 10px; font-size: 13px; }
}
</style>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'

const auth = useAuthStore()
const toast = useToast()
const shops = ref([])
const loading = ref(true)

const newShop = ref({
  shopName: '',
  address: '',
  phone: '',
  notice: '',
})
const creatingShop = ref(false)

const bootstrap = ref({
  shopId: '',
  username: '',
  password: '',
  realName: '',
  phone: '',
})
const bootstrapping = ref(false)

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

async function createShop() {
  if (!newShop.value.shopName?.trim()) {
    toast.warn('请填写店铺名称')
    return
  }
  creatingShop.value = true
  try {
    await request.post('/admin/shops', {
      shopName: newShop.value.shopName.trim(),
      address: newShop.value.address?.trim() || undefined,
      phone: newShop.value.phone?.trim() || undefined,
      notice: newShop.value.notice?.trim() || undefined,
    }, { headers: authHeader() })
    toast.success('店铺已创建')
    newShop.value = { shopName: '', address: '', phone: '', notice: '' }
    await loadShops()
  } catch (e) {
    toast.error(e.response?.data?.message || '创建失败')
  } finally {
    creatingShop.value = false
  }
}

async function bootstrapEmployee() {
  const sid = Number(bootstrap.value.shopId)
  if (!sid) {
    toast.warn('请选择或填写店铺 ID')
    return
  }
  if (!bootstrap.value.username?.trim() || !bootstrap.value.password) {
    toast.warn('请填写店长账号与密码')
    return
  }
  bootstrapping.value = true
  try {
    await request.post(
      `/admin/shops/${sid}/employees/bootstrap`,
      {
        username: bootstrap.value.username.trim(),
        password: bootstrap.value.password,
        realName: bootstrap.value.realName?.trim() || undefined,
        phone: bootstrap.value.phone?.trim() || undefined,
      },
      { headers: authHeader() },
    )
    toast.success('店长账号已创建，可使用商家端登录')
    bootstrap.value = { shopId: '', username: '', password: '', realName: '', phone: '' }
  } catch (e) {
    toast.error(e.response?.data?.message || '创建失败')
  } finally {
    bootstrapping.value = false
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
    <p class="intro">
      平台代入驻：先创建店铺，再为该店创建首个员工账号（默认角色为店长 SHOP_MANAGER）。新商家使用商家端登录，仅能管理本店数据。
    </p>

    <div class="panel-grid">
      <div class="panel">
        <h3>新建店铺</h3>
        <div class="field">
          <label>店铺名称 *</label>
          <input v-model="newShop.shopName" type="text" placeholder="例如：江南小厨" />
        </div>
        <div class="field">
          <label>地址</label>
          <input v-model="newShop.address" type="text" placeholder="可选" />
        </div>
        <div class="field row">
          <div>
            <label>电话</label>
            <input v-model="newShop.phone" type="text" placeholder="可选" />
          </div>
        </div>
        <div class="field">
          <label>公告</label>
          <input v-model="newShop.notice" type="text" placeholder="可选" />
        </div>
        <button class="btn-primary" type="button" :disabled="creatingShop" @click="createShop">
          {{ creatingShop ? '提交中…' : '创建店铺' }}
        </button>
      </div>

      <div class="panel">
        <h3>创建店长账号</h3>
        <div class="field">
          <label>店铺 ID *</label>
          <select v-model="bootstrap.shopId">
            <option value="">请选择店铺</option>
            <option v-for="s in shops" :key="s.id" :value="String(s.id)">
              {{ s.id }} · {{ s.shopName }}
            </option>
          </select>
        </div>
        <div class="field">
          <label>登录账号 *</label>
          <input v-model="bootstrap.username" type="text" autocomplete="off" />
        </div>
        <div class="field">
          <label>初始密码 *</label>
          <input v-model="bootstrap.password" type="password" autocomplete="new-password" />
        </div>
        <div class="field">
          <label>姓名</label>
          <input v-model="bootstrap.realName" type="text" placeholder="可选，默认与账号相同" />
        </div>
        <div class="field">
          <label>手机</label>
          <input v-model="bootstrap.phone" type="text" placeholder="可选" />
        </div>
        <button class="btn-primary" type="button" :disabled="bootstrapping" @click="bootstrapEmployee">
          {{ bootstrapping ? '提交中…' : '创建店长' }}
        </button>
      </div>
    </div>

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
.intro {
  margin: 0 0 20px;
  font-size: 0.88rem;
  color: #64748b;
  line-height: 1.5;
}
.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}
.panel {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  background: #fafafa;
}
.panel h3 { margin: 0 0 12px; font-size: 1rem; color: #334155; }
.field { margin-bottom: 10px; }
.field label { display: block; font-size: 0.78rem; color: #64748b; margin-bottom: 4px; }
.field input, .field select {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
}
.btn-primary {
  margin-top: 8px;
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: #1e293b;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
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

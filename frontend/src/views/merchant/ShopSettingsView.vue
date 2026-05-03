<template>
  <section class="shop-wrap">
    <div class="head">
      <h2>店铺设置</h2>
    </div>

    <div class="card">
      <label>店铺名称</label>
      <input v-model="form.shopName" placeholder="店铺名称" />

      <label>店铺地址</label>
      <input v-model="form.address" placeholder="店铺地址" />

      <label>联系电话</label>
      <input v-model="form.phone" placeholder="联系电话" />

      <label>店铺公告</label>
      <textarea v-model="form.notice" rows="3" placeholder="店铺公告" />

      <div class="status-row">
        <span>营业状态：</span>
        <button class="btn" :class="form.businessStatus === 1 ? 'on' : 'off'" @click="toggleBusiness">
          {{ form.businessStatus === 1 ? '营业中' : '已打烊' }}
        </button>
      </div>

      <button class="btn-save" @click="save">保存店铺信息</button>
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
const form = ref({
  shopName: '',
  address: '',
  phone: '',
  notice: '',
  businessStatus: 1,
})

onMounted(load)

async function load() {
  try {
    const res = await request.get('/merchant/shop/1', {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    form.value = {
      ...form.value,
      ...(res.data.data || {}),
      businessStatus: Number(res.data.data?.businessStatus ?? 1),
    }
  } catch (e) {
    console.error('加载店铺信息失败', e)
  }
}

async function save() {
  try {
    await request.put('/merchant/shop/1', {
      shopName: form.value.shopName,
      address: form.value.address,
      phone: form.value.phone,
      notice: form.value.notice,
    }, {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    toast.success('保存成功')
  } catch (e) {
    toast.error(e.message || '保存失败')
  }
}

async function toggleBusiness() {
  const next = form.value.businessStatus === 1 ? 0 : 1
  try {
    await request.patch('/merchant/shop/1/business-status', {
      businessStatus: next,
    }, {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    form.value.businessStatus = next
  } catch (e) {
    toast.error(e.message || '切换营业状态失败')
  }
}
</script>

<style scoped>
.shop-wrap { background: #fff; border-radius: 10px; padding: 20px; }
.card { display: grid; gap: 8px; max-width: 560px; }
input, textarea { border: 1px solid #ddd; border-radius: 8px; padding: 10px; }
.status-row { margin: 10px 0; display: flex; align-items: center; gap: 10px; }
.btn { border: none; border-radius: 8px; padding: 6px 12px; color: #fff; cursor: pointer; }
.btn.on { background: #16a34a; }
.btn.off { background: #64748b; }
.btn-save { margin-top: 8px; border: none; background: #ff6b35; color: #fff; border-radius: 8px; padding: 10px 14px; cursor: pointer; }
</style>

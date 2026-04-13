<template>
  <section class="category-wrap">
    <div class="head">
      <h2>分类管理</h2>
      <button class="btn-add" @click="openCreate">+ 新增分类</button>
    </div>

    <div v-if="categories.length === 0" class="tip">暂无分类</div>

    <div v-else class="list">
      <div v-for="c in categories" :key="c.id" class="item">
        <div>
          <b>{{ c.categoryName }}</b>
          <p>排序：{{ c.sort }} | 状态：{{ Number(c.status) === 1 ? '启用' : '停用' }}</p>
        </div>
        <div class="actions">
          <button class="btn" @click="openEdit(c)">编辑</button>
          <button class="btn danger" @click="remove(c.id)">删除</button>
        </div>
      </div>
    </div>

    <div v-if="visible" class="mask" @click.self="visible = false">
      <div class="modal">
        <h3>{{ editingId ? '编辑分类' : '新增分类' }}</h3>
        <input v-model="form.categoryName" placeholder="分类名称" />
        <input v-model.number="form.sort" type="number" placeholder="排序" />
        <select v-model.number="form.status">
          <option :value="1">启用</option>
          <option :value="0">停用</option>
        </select>
        <div class="actions">
          <button class="btn" @click="save">保存</button>
          <button class="btn" @click="visible = false">取消</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'

const auth = useAuthStore()
const categories = ref([])
const visible = ref(false)
const editingId = ref(null)
const form = ref({ categoryName: '', sort: 0, status: 1 })

onMounted(load)

async function load() {
  try {
    const res = await request.get('/merchant/categories', {
      params: { shopId: 1 },
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    categories.value = res.data.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

function openCreate() {
  editingId.value = null
  form.value = { categoryName: '', sort: 0, status: 1 }
  visible.value = true
}

function openEdit(c) {
  editingId.value = c.id
  form.value = {
    categoryName: c.categoryName,
    sort: Number(c.sort ?? 0),
    status: Number(c.status ?? 1),
  }
  visible.value = true
}

async function save() {
  try {
    const payload = { shopId: 1, ...form.value }
    if (editingId.value) {
      await request.put(`/merchant/categories/${editingId.value}`, payload, {
        headers: { Authorization: `Bearer ${auth.merchantToken}` },
      })
    } else {
      await request.post('/merchant/categories', payload, {
        headers: { Authorization: `Bearer ${auth.merchantToken}` },
      })
    }
    visible.value = false
    await load()
  } catch (e) {
    alert(e.message || '保存失败')
  }
}

async function remove(id) {
  if (!window.confirm('确认删除该分类？')) return
  try {
    await request.delete(`/merchant/categories/${id}`, {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    await load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}
</script>

<style scoped>
.category-wrap { background: #fff; border-radius: 10px; padding: 20px; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.btn-add { border: none; border-radius: 8px; background: #ff6b35; color: #fff; padding: 8px 12px; cursor: pointer; }
.list { display: grid; gap: 10px; }
.item { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; display: flex; justify-content: space-between; }
.item p { margin: 6px 0 0; color: #64748b; font-size: 13px; }
.actions { display: flex; gap: 8px; }
.btn { border: none; border-radius: 8px; background: #e2e8f0; padding: 6px 10px; cursor: pointer; }
.btn.danger { background: #fee2e2; color: #b91c1c; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.45); display: flex; align-items: center; justify-content: center; z-index: 1200; }
.modal { width: 92%; max-width: 420px; background: #fff; border-radius: 10px; padding: 14px; display: grid; gap: 10px; }
input, select { border: 1px solid #ddd; border-radius: 8px; padding: 9px; }
.tip { color: #888; }
</style>

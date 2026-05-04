<template>
  <div class="merchant-menu">
    <div class="toolbar">
      <h2>菜单管理</h2>
      <div class="toolbar-actions">
        <button type="button" class="btn-primary" @click="openCreateCategory">+ 新增分类</button>
        <button
          type="button"
          class="btn-secondary"
          :disabled="selectedCategory == null"
          @click="openEditCurrentCategory"
        >
          编辑当前分类
        </button>
        <button
          type="button"
          class="btn-secondary danger"
          :disabled="selectedCategory == null"
          @click="deleteCurrentCategory"
        >
          删除当前分类
        </button>
        <button type="button" class="btn-primary" @click="openCreateDish">+ 新增菜品</button>
      </div>
    </div>

    <p class="hint">点击卡片上的“编辑”可修改菜品信息，点击“上架/下架”可切换售卖状态。</p>

    <MenuBoard
      :key="categoryListVersion"
      :categories="categoryTabs"
      :dishes="dishes"
      :selected-category="selectedCategory"
      :load-error="loadError"
      mode="merchant"
      @update:selected-category="selectedCategory = $event"
      @edit-dish="openEditDish"
      @toggle-status="onToggleStatus"
      @delete-dish="onDeleteDish"
    />

    <!-- 分类弹窗 -->
    <div v-if="catVisible" class="mask" @click.self="catVisible = false">
      <div class="modal">
        <h3>{{ catEditingId ? '编辑分类' : '新增分类' }}</h3>
        <input v-model="catForm.categoryName" placeholder="分类名称" />
        <input v-model.number="catForm.sort" type="number" placeholder="排序（越小越靠前）" />
        <select v-model.number="catForm.status">
          <option :value="1">启用</option>
          <option :value="0">停用</option>
        </select>
        <div class="modal-actions">
          <button type="button" class="btn-primary" @click="saveCategory">保存</button>
          <button type="button" class="btn" @click="catVisible = false">取消</button>
        </div>
      </div>
    </div>


    <!-- 菜品弹窗 -->
    <div v-if="dishVisible" class="mask" @click.self="dishVisible = false">
      <div class="modal modal-wide">
        <h3>{{ dishEditingId ? '编辑菜品' : '新增菜品' }}</h3>
        <input v-model="dishForm.dishName" placeholder="菜品名称" />
        <input v-model.number="dishForm.price" type="number" step="0.01" placeholder="价格" />
        <select v-model.number="dishForm.categoryId">
          <option v-for="c in rawCategories" :key="c.id" :value="Number(c.id)">
            {{ c.categoryName }}（id={{ c.id }}）
          </option>
        </select>
        <input v-model="dishForm.description" placeholder="描述" />
        <input v-model.number="dishForm.stock" type="number" placeholder="库存" />
        <div class="upload-row">
          <input type="file" accept="image/*" @change="onFileChange" />
          <span v-if="uploading">上传中…</span>
        </div>
        <img v-if="dishForm.imageUrl" :src="dishForm.imageUrl" class="preview" alt="预览" />
        <div class="modal-actions">
          <button type="button" class="btn-primary" @click="saveDish">保存</button>
          <button type="button" class="btn" @click="dishVisible = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'
import MenuBoard from '@/components/menu/MenuBoard.vue'

const toast = useToast()

const auth = useAuthStore()
const SHOP_ID = auth.merchantProfile?.shopId
const authHeader = () => ({ Authorization: `Bearer ${auth.merchantToken}` })

const rawCategories = ref([])
const dishes = ref([])
const loadError = ref('')
const selectedCategory = ref(null)
/** 列表重拉后递增，强制 MenuBoard 重挂载，避免偶发界面不随 GET 结果更新 */
const categoryListVersion = ref(0)

const categoryTabs = computed(() =>
  rawCategories.value
    .slice()
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0) || a.id - b.id)
    .map((c) => ({
      id: Number(c.id),
      name: String(c.categoryName ?? '').trim() || `分类${c.id}`,
      active: Number(c.status) === 1,
    }))
)

function normalizeCategoryRow(c) {
  return {
    id: c.id,
    shopId: c.shopId,
    categoryName: c.categoryName,
    sort: c.sort,
    status: c.status,
  }
}

async function loadCategories() {
  const res = await request.get('/merchant/categories', {
    params: { shopId: SHOP_ID },
    headers: authHeader(),
  })
  const list = res.data.data || []
  rawCategories.value = list.map(normalizeCategoryRow)
  const ids = new Set(categoryTabs.value.map((c) => c.id))
  if (selectedCategory.value != null && !ids.has(Number(selectedCategory.value))) {
    selectedCategory.value = categoryTabs.value[0]?.id ?? null
  }
  if (categoryTabs.value.length && selectedCategory.value == null) {
    selectedCategory.value = categoryTabs.value[0].id
  }
}

async function loadDishes() {
  loadError.value = ''
  try {
    const res = await request.get('/merchant/dishes', {
      params: { page: 1, pageSize: 500, shopId: SHOP_ID },
      headers: authHeader(),
    })
    dishes.value = res.data.data?.records || []
  } catch (e) {
    console.error(e)
    loadError.value = '菜品加载失败'
    dishes.value = []
  }
}

onMounted(async () => {
  try {
    await loadCategories()
    await loadDishes()
    const valid = new Set(categoryTabs.value.map((c) => c.id))
    dishes.value = dishes.value.filter((d) => valid.has(Number(d.categoryId)))
    if (categoryTabs.value.length) {
      selectedCategory.value = categoryTabs.value[0].id
    }
  } catch (e) {
    loadError.value = '加载失败'
  }
})

/** 分类 CRUD */
const catVisible = ref(false)
const catEditingId = ref(null)
const catForm = ref({ categoryName: '', sort: 0, status: 1 })

function openCreateCategory() {
  catEditingId.value = null
  catForm.value = { categoryName: '', sort: 0, status: 1 }
  catVisible.value = true
}

function openEditCurrentCategory() {
  const id = selectedCategory.value
  if (id == null) return
  const c = rawCategories.value.find((x) => Number(x.id) === Number(id))
  if (!c) return
  catEditingId.value = c.id
  catForm.value = {
    categoryName: String(c.categoryName ?? ''),
    sort: Number(c.sort ?? 0),
    status: Number(c.status ?? 1),
  }
  catVisible.value = true
}

async function deleteCurrentCategory() {
  const id = selectedCategory.value
  if (id == null) return
  const c = rawCategories.value.find((x) => Number(x.id) === Number(id))
  if (!c) return
  if (!confirm(`确定删除分类「${c.categoryName}」？若分类下仍有菜品将无法删除。`)) return
  try {
    await request.delete(`/merchant/categories/${id}`, { headers: authHeader() })
    await loadCategories()
    categoryListVersion.value += 1
    await loadDishes()
    const valid = new Set(categoryTabs.value.map((x) => x.id))
    dishes.value = dishes.value.filter((d) => valid.has(Number(d.categoryId)))
  } catch (e) {
    toast.error(e.message || '删除失败')
  }
}

async function saveCategory() {
  try {
    const payload = { shopId: SHOP_ID, ...catForm.value }
    if (catEditingId.value) {
      await request.put(`/merchant/categories/${Number(catEditingId.value)}`, payload, {
        headers: authHeader(),
      })
    } else {
      await request.post('/merchant/categories', payload, { headers: authHeader() })
    }
    catVisible.value = false
    catEditingId.value = null
    await loadCategories()
    categoryListVersion.value += 1
    if (selectedCategory.value == null && categoryTabs.value.length) {
      selectedCategory.value = categoryTabs.value[0].id
    }
  } catch (e) {
    toast.error(e.message || '保存失败')
  }
}

/** 菜品 CRUD */
const dishVisible = ref(false)
const dishEditingId = ref(null)
const dishEditingStatus = ref(1)
const uploading = ref(false)
const dishForm = ref({
  dishName: '',
  price: '',
  categoryId: 1,
  description: '',
  stock: 100,
  imageUrl: '',
})

function openCreateDish() {
  dishEditingId.value = null
  dishEditingStatus.value = 1
  const firstCat = categoryTabs.value[0]?.id ?? 1
  dishForm.value = {
    dishName: '',
    price: '',
    categoryId: firstCat,
    description: '',
    stock: 100,
    imageUrl: '',
  }
  dishVisible.value = true
}

function openEditDish(dish) {
  dishEditingId.value = dish.id
  dishEditingStatus.value = dish.status
  dishForm.value = {
    dishName: dish.dishName,
    price: Number(dish.price),
    categoryId: Number(dish.categoryId),
    description: dish.description || '',
    stock: Number(dish.stock ?? 0),
    imageUrl: dish.imageUrl || '',
  }
  dishVisible.value = true
}

async function onFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  uploading.value = true
  try {
    const res = await request.post('/merchant/upload/image', fd, {
      headers: { ...authHeader(), 'Content-Type': 'multipart/form-data' },
    })
    dishForm.value.imageUrl = res.data.data?.url || ''
  } catch (e) {
    toast.error('上传失败')
  } finally {
    uploading.value = false
  }
}

async function saveDish() {
  try {
    const payload = {
      ...dishForm.value,
      shopId: SHOP_ID,
      categoryId: Number(dishForm.value.categoryId),
      status: dishEditingStatus.value,
    }
    if (dishEditingId.value) {
      await request.put(`/merchant/dishes/${dishEditingId.value}`, payload, { headers: authHeader() })
    } else {
      await request.post('/merchant/dishes', payload, { headers: authHeader() })
    }
    dishVisible.value = false
    await loadDishes()
  } catch (e) {
    console.error(e)
    toast.error(e.message || '保存失败')
  }
}

async function onToggleStatus(dish) {
  try {
    await request.patch(
      `/merchant/dishes/${dish.id}/status`,
      { status: dish.status === 1 ? 0 : 1 },
      { headers: authHeader() }
    )
    await loadDishes()
  } catch (e) {
    console.error(e)
  }
}

async function onDeleteDish(dish) {
  if (!confirm(`确定删除「${dish.dishName}」？`)) return
  try {
    await request.delete(`/merchant/dishes/${dish.id}`, { headers: authHeader() })
    await loadDishes()
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.merchant-menu {
  max-width: 1200px;
  margin: 0 auto;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
}

.toolbar h2 {
  margin: 0;
  font-size: 22px;
  color: #333;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.btn-primary {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-secondary {
  background: #fff;
  color: #334155;
  border: 1px solid #cbd5e1;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary.danger {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fff5f5;
}

.hint {
  color: #64748b;
  font-size: 13px;
  margin: 0 0 16px 0;
}

.mask {
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
  max-width: 420px;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.modal-wide {
  max-width: 480px;
}

.modal h3 {
  margin: 0 0 8px 0;
}

.modal input,
.modal select {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 9px;
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.btn {
  border: none;
  border-radius: 8px;
  background: #e2e8f0;
  padding: 8px 14px;
  cursor: pointer;
}

.upload-row {
  font-size: 13px;
  color: #64748b;
}

.preview {
  width: 100%;
  max-height: 160px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}
</style>

<template>
  <div class="dish-container">
    <div class="header">
      <h1>菜品管理</h1>
      <button class="btn-primary" @click="openCreate">+ 新增菜品</button>
    </div>

    <div v-if="showAddForm" class="modal-overlay" @click.self="showAddForm = false">
      <div class="modal">
        <h2>{{ editingId ? '编辑菜品' : '新增菜品' }}</h2>
        <form @submit.prevent="saveDish">
          <input v-model="form.dishName" placeholder="菜品名称" required />
          <input v-model.number="form.price" type="number" step="0.01" placeholder="价格" required />
          <select v-model="form.categoryId">
            <option value="">选择分类</option>
            <option value="1">主食</option>
            <option value="2">小吃</option>
            <option value="3">饮品</option>
          </select>
          <input v-model="form.description" placeholder="描述" />
          <input v-model.number="form.stock" type="number" placeholder="库存" />

          <div class="upload-row">
            <input type="file" accept="image/*" @change="onFileChange" />
            <span v-if="uploading">上传中...</span>
          </div>
          <img v-if="form.imageUrl" :src="form.imageUrl" class="preview" alt="预览" />

          <div class="form-actions">
            <button type="submit" class="btn-primary">保存</button>
            <button type="button" class="btn-secondary" @click="showAddForm = false">取消</button>
          </div>
        </form>
      </div>
    </div>

    <div class="dish-list">
      <div v-if="dishes.length === 0" class="empty">暂无菜品</div>
      <div v-for="dish in dishes" :key="dish.id" class="dish-card">
        <div class="dish-image">
          <img :src="displayImage(dish)" :alt="dish.dishName" @error="onImgError" />
        </div>
        <div class="dish-info">
          <h3>{{ dish.dishName }}</h3>
          <p class="category">{{ getCategoryName(dish.categoryId) }}</p>
          <p class="description">{{ dish.description }}</p>
          <p class="price">¥{{ dish.price.toFixed(2) }}</p>
          <p class="stock">库存：{{ dish.stock }}</p>
          <p class="status" :class="{ active: dish.status === 1, inactive: dish.status === 0 }">
            {{ dish.status === 1 ? '上架' : '下架' }}
          </p>
        </div>
        <div class="dish-actions">
          <button class="btn-small" @click="openEdit(dish)">编辑</button>
          <button class="btn-small" @click="toggleDish(dish.id, dish.status)">
            {{ dish.status === 1 ? '下架' : '上架' }}
          </button>
          <button class="btn-small btn-danger" @click="deleteDish(dish.id)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'
import imgMain from '@/assets/dishes/main.svg'
import imgSnack from '@/assets/dishes/snack.svg'
import imgDrink from '@/assets/dishes/drink.svg'
import imgDefault from '@/assets/dishes/default.svg'
import imgBeefRice from '@/assets/real/beef-rice.jpg'
import imgChicken from '@/assets/real/chicken.jpg'
import imgTea from '@/assets/real/tea.jpg'
import imgNoodle from '@/assets/real/noodle.jpg'
import imgBurger from '@/assets/real/burger.jpg'
import imgRealDrink from '@/assets/real/drink.jpg'

const auth = useAuthStore()
const dishes = ref([])
const showAddForm = ref(false)
const editingId = ref(null)
const editingStatus = ref(1)
const uploading = ref(false)
const form = ref({
  dishName: '',
  price: '',
  categoryId: 1,
  description: '',
  stock: 100,
  imageUrl: '',
})

const categories = {
  1: '主食',
  2: '小吃',
  3: '饮品',
}

const categoryImageMap = {
  1: imgMain,
  2: imgSnack,
  3: imgDrink,
}

const keywordImageMap = [
  { keys: ['牛肉饭', '牛肉', '盖饭', '饭'], image: imgBeefRice },
  { keys: ['鸡翅', '鸡排', '鸡', '炸鸡'], image: imgChicken },
  { keys: ['面', '拉面', '拌面', '汤面'], image: imgNoodle },
  { keys: ['汉堡', 'burger'], image: imgBurger },
  { keys: ['红茶', '奶茶', '柠檬', '茶', '可乐', '饮料'], image: imgTea },
  { keys: ['饮品', '果汁', '咖啡'], image: imgRealDrink },
]

onMounted(async () => {
  await loadDishes()
})

async function loadDishes() {
  try {
    const res = await request.get('/merchant/dishes', {
      params: { page: 1, pageSize: 100, shopId: 1 },
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    dishes.value = res.data.data?.records || []
  } catch (err) {
    console.error('获取菜品列表失败', err)
  }
}

function getCategoryName(categoryId) {
  return categories[categoryId] || '未分类'
}

function displayImage(dish) {
  if (dish.imageUrl && String(dish.imageUrl).trim()) {
    return dish.imageUrl
  }
  const name = String(dish.dishName || '').toLowerCase()
  const hit = keywordImageMap.find(item => item.keys.some(k => name.includes(String(k).toLowerCase())))
  if (hit) return hit.image
  return categoryImageMap[dish.categoryId] || imgDefault
}

function onImgError(event) {
  event.target.src = imgDefault
}

async function onFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return

  const fd = new FormData()
  fd.append('file', file)

  try {
    uploading.value = true
    const res = await request.post('/merchant/upload/image', fd, {
      headers: {
        Authorization: `Bearer ${auth.merchantToken}`,
        'Content-Type': 'multipart/form-data',
      },
    })
    form.value.imageUrl = res.data.data?.url || ''
  } catch (err) {
    console.error('上传图片失败', err)
    alert('上传图片失败')
  } finally {
    uploading.value = false
  }
}

function resetForm() {
  form.value = { dishName: '', price: '', categoryId: 1, description: '', stock: 100, imageUrl: '' }
  editingId.value = null
  editingStatus.value = 1
}

function openCreate() {
  resetForm()
  showAddForm.value = true
}

function openEdit(dish) {
  editingId.value = dish.id
  editingStatus.value = dish.status
  form.value = {
    dishName: dish.dishName,
    price: Number(dish.price),
    categoryId: Number(dish.categoryId),
    description: dish.description || '',
    stock: Number(dish.stock ?? 0),
    imageUrl: dish.imageUrl || '',
  }
  showAddForm.value = true
}

async function saveDish() {
  try {
    const payload = {
      ...form.value,
      shopId: 1,
      categoryId: Number(form.value.categoryId),
      status: editingStatus.value,
    }

    if (editingId.value) {
      await request.put(`/merchant/dishes/${editingId.value}`, payload, {
        headers: { Authorization: `Bearer ${auth.merchantToken}` },
      })
    } else {
      await request.post('/merchant/dishes', payload, {
        headers: { Authorization: `Bearer ${auth.merchantToken}` },
      })
    }

    showAddForm.value = false
    resetForm()
    await loadDishes()
  } catch (err) {
    console.error('保存菜品失败', err)
  }
}

async function toggleDish(id, currentStatus) {
  try {
    await request.patch(
      `/merchant/dishes/${id}/status`,
      { status: currentStatus === 1 ? 0 : 1 },
      { headers: { Authorization: `Bearer ${auth.merchantToken}` } }
    )
    await loadDishes()
  } catch (err) {
    console.error('更新菜品状态失败', err)
  }
}

async function deleteDish(id) {
  if (!confirm('确定删除该菜品吗？')) return
  try {
    await request.delete(`/merchant/dishes/${id}`, {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    await loadDishes()
  } catch (err) {
    console.error('删除菜品失败', err)
  }
}
</script>

<style scoped>
.dish-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 28px;
  color: #333;
}

.btn-primary {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: transform 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
}

.btn-secondary {
  background: #e0e0e0;
  color: #333;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-small {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 8px;
}

.btn-danger {
  background: #e74c3c;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 30px;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal h2 {
  margin-bottom: 20px;
  color: #333;
}

.modal form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal input,
.modal select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #666;
}

.preview {
  width: 100%;
  max-height: 140px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}

.form-actions button {
  flex: 1;
}

.dish-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.dish-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s;
  display: flex;
  flex-direction: column;
}

.dish-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.dish-image {
  width: 100%;
  height: 180px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder {
  color: #999;
  font-size: 14px;
}

.dish-info {
  padding: 15px;
  flex: 1;
}

.dish-info h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.category {
  color: #999;
  font-size: 12px;
  margin: 4px 0;
}

.description {
  color: #666;
  font-size: 13px;
  margin: 8px 0;
  line-height: 1.4;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff6b35;
  margin: 8px 0;
}

.stock {
  color: #666;
  font-size: 12px;
  margin: 4px 0;
}

.status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  margin-top: 8px;
}

.status.active {
  background: #d4edda;
  color: #155724;
}

.status.inactive {
  background: #f8d7da;
  color: #721c24;
}

.dish-actions {
  padding: 12px 15px;
  border-top: 1px solid #e0e0e0;
  display: flex;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}
</style>

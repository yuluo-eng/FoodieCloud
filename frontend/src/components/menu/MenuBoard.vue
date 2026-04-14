<template>
  <div class="menu-board">
    <div class="category-tabs">
      <button
        v-for="cat in categories"
        :key="cat.id"
        :class="['tab-btn', { active: selectedCategory === cat.id, inactive: cat.active === false }]"
        type="button"
        @click="$emit('update:selectedCategory', cat.id)"
      >
        {{ cat.name }}
      </button>
    </div>

    <div class="dishes-grid">
      <div v-if="loadError" class="empty">{{ loadError }}</div>
      <div v-else-if="filteredDishes.length === 0" class="empty">暂无菜品</div>
      <div v-for="dish in filteredDishes" :key="dish.id" class="dish-item">
        <div class="dish-img">
          <img :src="displayImage(dish)" :alt="dish.dishName" @error="onImgError($event, dish)" />
        </div>
        <div class="dish-details">
          <h3>{{ dish.dishName }}</h3>
          <p class="desc">{{ dish.description }}</p>
          <p v-if="mode === 'merchant'" class="meta">
            <span :class="['badge-status', dish.status === 1 ? 'on' : 'off']">
              {{ dish.status === 1 ? '上架' : '下架' }}
            </span>
          </p>
          <div class="dish-footer">
            <span class="price">¥{{ Number(dish.price).toFixed(2) }}</span>
            <template v-if="mode === 'user'">
              <button type="button" class="btn-add" @click="$emit('add-cart', dish)">加入购物车</button>
            </template>
            <template v-else>
              <div class="merchant-actions">
                <button type="button" class="btn-sm" @click="$emit('edit-dish', dish)">编辑</button>
                <button type="button" class="btn-sm" @click="$emit('toggle-status', dish)">
                  {{ dish.status === 1 ? '下架' : '上架' }}
                </button>
                <button type="button" class="btn-sm danger" @click="$emit('delete-dish', dish)">删除</button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useDishDisplayImage } from '@/composables/useDishDisplayImage.js'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  dishes: { type: Array, default: () => [] },
  selectedCategory: { type: Number, default: null },
  loadError: { type: String, default: '' },
  /** user：仅展示上架菜品；merchant：展示该分类下全部菜品（含下架） */
  mode: { type: String, default: 'user' },
})

defineEmits(['update:selectedCategory', 'add-cart', 'edit-dish', 'delete-dish', 'toggle-status'])

const { displayImage, onImgError } = useDishDisplayImage()

const filteredDishes = computed(() => {
  const list = props.dishes || []
  const onlyOn = props.mode === 'user'
  const byCat = (d) =>
    props.selectedCategory == null || Number(d.categoryId) === Number(props.selectedCategory)
  if (onlyOn) {
    if (props.selectedCategory == null) {
      return list.filter((d) => Number(d.status) === 1)
    }
    return list.filter((d) => byCat(d) && Number(d.status) === 1)
  }
  return list.filter(byCat)
})
</script>

<style scoped>
.menu-board {
  width: 100%;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 25px;
}

.tab-btn {
  padding: 10px 20px;
  border: 2px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-btn.active {
  border-color: #ff6b35;
  background: #ff6b35;
  color: white;
}

.tab-btn.inactive:not(.active) {
  opacity: 0.65;
  border-style: dashed;
}

.dishes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.dish-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.dish-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.dish-img {
  width: 100%;
  height: 160px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.dish-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dish-details {
  padding: 15px;
}

.dish-details h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.desc {
  color: #999;
  font-size: 12px;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.meta {
  margin: 0 0 8px 0;
}

.badge-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
.badge-status.on {
  background: #dcfce7;
  color: #166534;
}
.badge-status.off {
  background: #fee2e2;
  color: #991b1b;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff6b35;
}

.btn-add {
  background: #ff6b35;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.merchant-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.btn-sm {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-sm.danger {
  background: #e74c3c;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}
</style>

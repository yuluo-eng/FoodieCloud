<template>
  <div class="employee-container">
    <div class="header">
      <h1>员工管理</h1>
      <button class="btn-primary" @click="showAddForm = true">+ 新增员工</button>
    </div>

    <div v-if="showAddForm" class="modal-overlay" @click.self="showAddForm = false">
      <div class="modal">
        <h2>新增员工</h2>
        <form @submit.prevent="addEmployee">
          <input v-model="form.username" placeholder="登录账号" required />
          <input v-model="form.realName" placeholder="姓名" required />
          <input v-model="form.phone" placeholder="手机号" />
          <input v-model="form.password" type="password" placeholder="密码" required />
          <select v-model="form.roleId">
            <option value="">选择角色</option>
            <option value="2">店长</option>
            <option value="3">员工</option>
          </select>
          <div class="form-actions">
            <button type="submit" class="btn-primary">保存</button>
            <button type="button" class="btn-secondary" @click="showAddForm = false">取消</button>
          </div>
        </form>
      </div>
    </div>

    <div class="employee-list">
      <div v-if="employees.length === 0" class="empty">暂无员工</div>
      <div v-for="emp in employees" :key="emp.id" class="employee-card">
        <div class="emp-info">
          <h3>{{ emp.realName }}</h3>
          <p>账号：{{ emp.username }}</p>
          <p>手机：{{ emp.phone }}</p>
          <p>角色：{{ emp.roleCode }}</p>
          <p class="status" :class="{ enabled: emp.enabled, disabled: !emp.enabled }">
            {{ emp.enabled ? '启用' : '禁用' }}
          </p>
        </div>
        <div class="emp-actions">
          <button class="btn-small" @click="toggleEmployee(emp.id, emp.enabled)">
            {{ emp.enabled ? '禁用' : '启用' }}
          </button>
          <button class="btn-small btn-danger" @click="deleteEmployee(emp.id)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'

const auth = useAuthStore()
const employees = ref([])
const showAddForm = ref(false)
const form = ref({
  username: '',
  realName: '',
  phone: '',
  password: '',
  roleId: 3,
})

onMounted(async () => {
  await loadEmployees()
})

async function loadEmployees() {
  try {
    const res = await request.get('/merchant/employees', {
      params: { page: 1, pageSize: 100, shopId: 1 },
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    employees.value = res.data.data?.records || []
  } catch (err) {
    console.error('获取员工列表失败', err)
  }
}

async function addEmployee() {
  try {
    await request.post(
      '/merchant/employees',
      {
        ...form.value,
        roleId: Number(form.value.roleId),
        shopId: 1,
        enabled: 1,
      },
      { headers: { Authorization: `Bearer ${auth.merchantToken}` } }
    )
    showAddForm.value = false
    form.value = { username: '', realName: '', phone: '', password: '', roleId: 3 }
    await loadEmployees()
  } catch (err) {
    console.error('新增员工失败', err)
  }
}

async function toggleEmployee(id, currentEnabled) {
  try {
    await request.patch(
      `/merchant/employees/${id}/enabled`,
      { enabled: currentEnabled ? 0 : 1 },
      { headers: { Authorization: `Bearer ${auth.merchantToken}` } }
    )
    await loadEmployees()
  } catch (err) {
    console.error('更新员工状态失败', err)
  }
}

async function deleteEmployee(id) {
  if (!confirm('确定删除该员工吗？')) return
  try {
    await request.delete(`/merchant/employees/${id}`, {
      headers: { Authorization: `Bearer ${auth.merchantToken}` },
    })
    await loadEmployees()
  } catch (err) {
    console.error('删除员工失败', err)
  }
}
</script>

<style scoped>
.employee-container {
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

.form-actions button {
  flex: 1;
}

.employee-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.employee-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s;
}

.employee-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.emp-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
}

.emp-info p {
  margin: 6px 0;
  color: #666;
  font-size: 14px;
}

.status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  margin-top: 8px;
}

.status.enabled {
  background: #d4edda;
  color: #155724;
}

.status.disabled {
  background: #f8d7da;
  color: #721c24;
}

.emp-actions {
  margin-top: 15px;
  display: flex;
  gap: 8px;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}
</style>

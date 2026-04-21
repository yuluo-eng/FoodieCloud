<template>
  <div class="profile-page">
    <header class="head">
      <RouterLink class="back" to="/user">← 返回点餐</RouterLink>
      <h1>个人资料</h1>
    </header>

    <div v-if="loadError" class="alert">{{ loadError }}</div>

    <form class="card" @submit.prevent="onSave">
      <section class="section">
        <h2>头像</h2>
        <div class="avatar-row">
          <div class="avatar-wrap">
            <img v-if="avatarPreview" :src="avatarPreview" alt="头像" />
            <span v-else class="avatar-ph">{{ avatarLetter }}</span>
          </div>
          <div>
            <input type="file" accept="image/jpeg,image/png,image/webp,image/gif" @change="onAvatarFile" />
            <p class="hint">支持 jpg/png/webp/gif。上传后需点击页面底部「保存」写入资料。</p>
          </div>
        </div>
      </section>

      <section class="section">
        <h2>昵称</h2>
        <label class="field">
          <span>昵称</span>
          <input v-model="form.nickname" maxlength="50" placeholder="展示名称" />
        </label>
        <p class="hint muted">登录名：{{ form.username }}（不可修改）</p>
      </section>

      <section class="section">
        <h2>收货信息</h2>
        <label class="field">
          <span>收货人</span>
          <input v-model="form.receiverName" maxlength="50" placeholder="姓名" />
        </label>
        <label class="field">
          <span>收货电话</span>
          <input v-model="form.shippingPhone" maxlength="20" placeholder="手机号" />
        </label>
        <label class="field">
          <span>详细地址</span>
          <textarea v-model="form.shippingAddress" rows="3" maxlength="500" placeholder="门牌、楼层等" />
        </label>
        <div class="loc-row">
          <button type="button" class="btn-loc" :disabled="locating" @click="fillAddressByLocation">
            {{ locating ? '定位中…' : '根据当前位置填写地址' }}
          </button>
          <span v-if="form.shippingLat != null && form.shippingLng != null" class="coords">
            坐标：{{ Number(form.shippingLat).toFixed(6) }}, {{ Number(form.shippingLng).toFixed(6) }}
          </span>
        </div>
        <p class="hint">定位需浏览器授权；地址由开放地图服务逆地理解析，请核对后再保存。</p>
      </section>

      <div class="actions">
        <button type="submit" class="btn-primary" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
      </div>
      <p v-if="saveOk" class="ok">已保存</p>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchUserProfile, updateUserProfile, reverseGeocode, uploadUserAvatar } from '@/api/userProfile'

const auth = useAuthStore()

const loadError = ref('')
const saving = ref(false)
const saveOk = ref(false)
const locating = ref(false)
/** 当前将提交的头像相对路径，如 /uploads/xxx.jpg */
const avatarRelative = ref('')

const form = ref({
  username: '',
  nickname: '',
  receiverName: '',
  shippingPhone: '',
  shippingAddress: '',
  shippingLat: null,
  shippingLng: null,
})

function resolveImgSrc(relativeOrFull) {
  if (!relativeOrFull) return ''
  if (relativeOrFull.startsWith('http')) return relativeOrFull
  return relativeOrFull.startsWith('/') ? relativeOrFull : `/${relativeOrFull}`
}

const avatarPreview = computed(() => resolveImgSrc(avatarRelative.value))

const avatarLetter = computed(() => {
  const n = form.value.nickname || form.value.username || '?'
  return String(n).slice(0, 1)
})

onMounted(async () => {
  loadError.value = ''
  try {
    const res = await fetchUserProfile(auth.userToken)
    const p = res.data.data
    form.value = {
      username: p.username || '',
      nickname: p.nickname || '',
      receiverName: p.receiverName || '',
      shippingPhone: p.shippingPhone || '',
      shippingAddress: p.shippingAddress || '',
      shippingLat: p.shippingLat != null ? Number(p.shippingLat) : null,
      shippingLng: p.shippingLng != null ? Number(p.shippingLng) : null,
    }
    avatarRelative.value = p.avatar || ''
    await auth.refreshUserProfile()
  } catch (e) {
    loadError.value =
      e.message || '加载失败（请确认已执行 docs/patch-user-shipping.sql 迁移并重启后端）'
  }
})

async function onAvatarFile(e) {
  const file = e.target.files?.[0]
  if (!file) return
  saving.value = true
  saveOk.value = false
  loadError.value = ''
  try {
    const res = await uploadUserAvatar(auth.userToken, file)
    const rel = res.data.data?.relativeUrl
    if (rel) {
      avatarRelative.value = rel
    }
  } catch (err) {
    loadError.value = err.message || '上传失败'
  } finally {
    saving.value = false
    e.target.value = ''
  }
}

async function onSave() {
  saving.value = true
  saveOk.value = false
  loadError.value = ''
  try {
    const payload = {
      nickname: form.value.nickname?.trim() || null,
      avatar: avatarRelative.value?.trim() || null,
      receiverName: form.value.receiverName?.trim() || null,
      shippingPhone: form.value.shippingPhone?.trim() || null,
      shippingAddress: form.value.shippingAddress?.trim() || null,
      shippingLat: form.value.shippingLat,
      shippingLng: form.value.shippingLng,
    }
    await updateUserProfile(auth.userToken, payload)
    await auth.refreshUserProfile()
    saveOk.value = true
    setTimeout(() => {
      saveOk.value = false
    }, 2000)
  } catch (e) {
    loadError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function fillAddressByLocation() {
  if (!navigator.geolocation) {
    loadError.value = '当前浏览器不支持定位'
    return
  }
  locating.value = true
  loadError.value = ''
  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      try {
        const lat = pos.coords.latitude
        const lng = pos.coords.longitude
        const res = await reverseGeocode(auth.userToken, lat, lng)
        const data = res.data.data
        if (data.displayName) {
          form.value.shippingAddress = data.displayName
        }
        form.value.shippingLat = data.latitude != null ? Number(data.latitude) : lat
        form.value.shippingLng = data.longitude != null ? Number(data.longitude) : lng
      } catch (e) {
        loadError.value = e.message || '逆地理编码失败'
      } finally {
        locating.value = false
      }
    },
    () => {
      locating.value = false
      loadError.value = '无法获取位置，请检查浏览器定位权限'
    },
    { enableHighAccuracy: true, timeout: 15000 }
  )
}
</script>

<style scoped>
.profile-page {
  max-width: 560px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.head {
  margin-bottom: 20px;
}

.head h1 {
  margin: 12px 0 0;
  font-size: 22px;
  color: #1e293b;
}

.back {
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
}

.back:hover {
  color: #ff6b35;
}

.alert {
  background: #fef2f2;
  color: #b91c1c;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section {
  margin-bottom: 24px;
}

.section h2 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #334155;
}

.avatar-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.avatar-wrap {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  background: #e2e8f0;
  flex-shrink: 0;
}

.avatar-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-ph {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #64748b;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #475569;
}

.field input,
.field textarea {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
}

.hint {
  font-size: 12px;
  color: #64748b;
  margin: 8px 0 0;
}

.hint.muted {
  margin-top: 4px;
}

.loc-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.btn-loc {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #334155;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}

.btn-loc:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.coords {
  font-size: 12px;
  color: #64748b;
}

.actions {
  margin-top: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ok {
  color: #166534;
  font-size: 14px;
  margin-top: 10px;
}
</style>

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { setAuthHeader } from '@/api/request'
import './style.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia).use(router)

const restoredToken =
  localStorage.getItem('ysh_rider_token') ||
  localStorage.getItem('ysh_merchant_token') ||
  localStorage.getItem('ysh_user_token')
if (restoredToken) {
  setAuthHeader(restoredToken)
}

app.mount('#app')

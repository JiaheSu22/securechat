// src/main.js

import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 应用启动时检查 token 过期
const authStore = useAuthStore()
if (authStore.isAuthenticated) {
  // 检查 token 是否过期
  if (authStore.isTokenExpired()) {
    console.log('Token expired on app startup, logging out...')
    authStore.logout(false) // 不显示确认弹窗
  }
}

app.mount('#app')

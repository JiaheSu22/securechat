// src/main.js - Application entry point

import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Check token expiration on app startup
const authStore = useAuthStore()
if (authStore.isAuthenticated) {
  // Check if token is expired
  if (authStore.isTokenExpired()) {
    if (process.env.NODE_ENV === 'development') {
      console.log('Token expired on app startup, logging out...')
    }
    authStore.logout(false) // Don't show confirmation dialog
  }
}

app.mount('#app')

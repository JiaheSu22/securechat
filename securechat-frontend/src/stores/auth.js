// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import { userService } from '@/services/userService'
import { generateKeyPair } from '@/services/crypto'

export const useAuthStore = defineStore('auth', () => {
  // state
  const token = ref(localStorage.getItem('token'))
  const user = ref(JSON.parse(localStorage.getItem('user')))
  const privateKey = ref(localStorage.getItem('privateKey'))

  // getters
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const userInitial = computed(() => user.value?.nickname?.charAt(0).toUpperCase() || '?')

  // actions
  function setToken(newToken) {
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
    token.value = newToken
  }

  function setUser(newUser) {
    if (newUser) {
      localStorage.setItem('user', JSON.stringify(newUser))
    } else {
      localStorage.removeItem('user')
    }
    user.value = newUser
  }

  function setPrivateKey(newPrivateKey) {
    if (newPrivateKey) {
      localStorage.setItem('privateKey', newPrivateKey)
    } else {
      localStorage.removeItem('privateKey')
    }
    privateKey.value = newPrivateKey
  }

  async function login(username, password) {
    try {
      const res = await authService.login({ username, password })
      const { token: jwt, ...userData } = res.data
      setToken(jwt)
      setUser(userData)
      // 登录后不处理密钥，密钥只在注册时生成
      return { success: true }
    } catch (e) {
      return { success: false, message: e.response?.data?.message || 'Login failed' }
    }
  }

  async function register({ username, password, nickname }) {
    try {
      // 1. 注册
      await authService.register({ username, password, nickname })
      // 2. 注册成功后自动登录
      const loginRes = await authService.login({ username, password })
      const { token: jwt, ...userData } = loginRes.data
      setToken(jwt)
      setUser(userData)
      // 3. 生成密钥对
      const { privateKey: priv, publicKey } = generateKeyPair()
      setPrivateKey(priv)
      // 4. 处理公钥内容（去掉头尾和换行）
      const cleanPublicKey = publicKey
        .replace('-----BEGIN PUBLIC KEY-----', '')
        .replace('-----END PUBLIC KEY-----', '')
        .replace(/\n/g, '')
        .trim();
      // 5. 上传公钥，确保token已写入localStorage
      await userService.uploadPublicKey({ publicKey: cleanPublicKey })
      return { success: true }
    } catch (e) {
      return { success: false, message: e.response?.data?.message || 'Register failed' }
    }
  }

  function logout() {
    setToken(null)
    setUser(null)
    setPrivateKey(null)
  }

  return {
    user,
    token,
    privateKey,
    isAuthenticated,
    userInitial,
    login,
    register,
    logout
  }
})

// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import { userService } from '@/services/userService'
import sodium from 'libsodium-wrappers'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const user = ref(JSON.parse(localStorage.getItem('user')))
  const x25519PrivateKey = ref(localStorage.getItem('x25519PrivateKey'))
  const ed25519PrivateKey = ref(localStorage.getItem('ed25519PrivateKey'))

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  function setToken(newToken) {
    if (newToken) localStorage.setItem('token', newToken)
    else localStorage.removeItem('token')
    token.value = newToken
  }

  function setUser(newUser) {
    if (newUser) localStorage.setItem('user', JSON.stringify(newUser))
    else localStorage.removeItem('user')
    user.value = newUser
  }

  function setX25519PrivateKey(sk) {
    if (sk) localStorage.setItem('x25519PrivateKey', sk)
    else localStorage.removeItem('x25519PrivateKey')
    x25519PrivateKey.value = sk
  }

  function setEd25519PrivateKey(sk) {
    if (sk) localStorage.setItem('ed25519PrivateKey', sk)
    else localStorage.removeItem('ed25519PrivateKey')
    ed25519PrivateKey.value = sk
  }

  async function register({ username, password, nickname }) {
    await sodium.ready;
    // 1. 注册账号，获取 token
      const registerRes = await authService.register({ username, password, nickname })
      const jwt = registerRes.data.token
      setToken(jwt)
    // 2. 获取用户信息
      const userInfoRes = await userService.getMe()
      setUser(userInfoRes.data)
    // 3. 生成 X25519 和 Ed25519 密钥对
    const x25519KeyPair = sodium.crypto_kx_keypair()
    const ed25519KeyPair = sodium.crypto_sign_keypair()
    // 4. 上传公钥
    await userService.uploadX25519Key({ x25519PublicKey: sodium.to_base64(x25519KeyPair.publicKey) })
    await userService.uploadEd25519Key({ ed25519PublicKey: sodium.to_base64(ed25519KeyPair.publicKey) })
    // 5. 本地保存私钥
    setX25519PrivateKey(sodium.to_base64(x25519KeyPair.privateKey))
    setEd25519PrivateKey(sodium.to_base64(ed25519KeyPair.privateKey))
      return { success: true }
  }

  async function login(username, password) {
    try {
      // 1. 登录，获取 token
      const res = await authService.login({ username, password });
      const { token: jwt } = res.data;
      setToken(jwt);
      // 2. 获取用户信息
      const userInfoRes = await userService.getMe();
      setUser(userInfoRes.data);
      // 3. 本地不生成密钥，只从 localStorage 读取私钥
      // 如果你想支持多端同步密钥，这里可以加密存储/导入
      return { success: true };
    } catch (e) {
      return { success: false, message: e.response?.data?.message || 'Login failed' };
    }
  }

  function logout() {
    setToken(null)
    setUser(null)
    setX25519PrivateKey(null)
    setEd25519PrivateKey(null)
  }

  return {
    user,
    token,
    x25519PrivateKey,
    ed25519PrivateKey,
    isAuthenticated,
    register,
    login,      // <--- 补上
    logout
  }
})

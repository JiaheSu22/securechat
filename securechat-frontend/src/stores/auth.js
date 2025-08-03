// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import { userService } from '@/services/userService'
import sodium from 'libsodium-wrappers'
import { ElMessageBox, ElMessage } from 'element-plus'
import { exportPrivateKeysToFile } from '@/utils/keyExport'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  // 修复语法错误：移除多余的括号
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
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

  // 改进的退出登录函数
  async function logout(showConfirmDialog = true) {
    // 检查是否有私钥需要保存
    const hasPrivateKeys = x25519PrivateKey.value || ed25519PrivateKey.value;
    
    if (showConfirmDialog && hasPrivateKeys) {
      // 合并的确认弹窗：退出确认 + 私钥保存提醒
      const result = await new Promise((resolve) => {
        ElMessageBox.confirm(
          'You are about to log out. It is recommended to export your private keys for backup, otherwise they will be lost. Would you like to export your private keys before logging out?',
          'Logout Confirmation',
          {
            confirmButtonText: 'Save Keys & Logout',
            cancelButtonText: 'Logout Only',
            distinguishCancelAndClose: true,
            type: 'warning',
          }
        ).then(() => {
          resolve('export');
        }).catch((action) => {
          if (action === 'cancel') {
            resolve('logout_only');
          } else {
            resolve('cancel'); // 右上角叉号取消退出
          }
        });
      });

      if (result === 'cancel') {
        return false; // 用户取消退出，返回 false
      }

      if (result === 'export') {
        // 导出私钥并下载文件
        const exportObj = { 
          x25519PrivateKey: x25519PrivateKey.value, 
          ed25519PrivateKey: ed25519PrivateKey.value 
        };
        
        // 使用公共工具函数导出私钥
        exportPrivateKeysToFile(exportObj, user.value?.username);
      }
    }

    // 清理所有认证相关数据
    setToken(null)
    setUser(null)
    setX25519PrivateKey(null)
    setEd25519PrivateKey(null)
    
    // 清理其他可能存在的认证相关数据
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('x25519PrivateKey')
    localStorage.removeItem('ed25519PrivateKey')
    
    // 清理 sessionStorage 中的相关数据
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    
    return true; // 成功退出，返回 true
  }

  // 检查 token 是否过期
  function isTokenExpired() {
    if (!token.value) return true;
    
    try {
      // 解析 JWT token 获取过期时间
      const payload = JSON.parse(atob(token.value.split('.')[1]));
      const expirationTime = payload.exp * 1000; // 转换为毫秒
      const currentTime = Date.now();
      
      return currentTime >= expirationTime;
    } catch (e) {
      // 如果解析失败，认为 token 无效
      return true;
    }
  }

  // 自动清理过期的 token
  function cleanExpiredToken() {
    if (isTokenExpired()) {
      logout();
      return true;
    }
    return false;
  }

  return {
    user,
    token,
    x25519PrivateKey,
    ed25519PrivateKey,
    isAuthenticated,
    register,
    login,
    logout,
    isTokenExpired,
    cleanExpiredToken
  }
})

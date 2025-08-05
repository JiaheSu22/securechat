// src/stores/auth.js - Authentication store using Pinia
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import { userService } from '@/services/userService'
import sodium from 'libsodium-wrappers'
import { ElMessageBox, ElMessage } from 'element-plus'
import { exportPrivateKeysToFile } from '@/utils/keyExport'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  // Fix syntax error: remove extra parentheses
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
    // 1. Register account and get token
      const registerRes = await authService.register({ username, password, nickname })
      const jwt = registerRes.data.token
      setToken(jwt)
    // 2. Get user information
      const userInfoRes = await userService.getMe()
      setUser(userInfoRes.data)
    // 3. Generate X25519 and Ed25519 key pairs
    const x25519KeyPair = sodium.crypto_kx_keypair()
    const ed25519KeyPair = sodium.crypto_sign_keypair()
    // 4. Upload public keys
    await userService.uploadX25519Key({ x25519PublicKey: sodium.to_base64(x25519KeyPair.publicKey, sodium.base64_variants.URLSAFE_NO_PADDING) })
    await userService.uploadEd25519Key({ ed25519PublicKey: sodium.to_base64(ed25519KeyPair.publicKey, sodium.base64_variants.URLSAFE_NO_PADDING) })
    // 5. Save private keys locally
    setX25519PrivateKey(sodium.to_base64(x25519KeyPair.privateKey, sodium.base64_variants.URLSAFE_NO_PADDING))
    setEd25519PrivateKey(sodium.to_base64(ed25519KeyPair.privateKey, sodium.base64_variants.URLSAFE_NO_PADDING))
      return { success: true }
  }

  async function login(username, password) {
    try {
      // 1. Login and get token
      const res = await authService.login({ username, password });
      const { token: jwt } = res.data;
      setToken(jwt);
      // 2. Get user information
      const userInfoRes = await userService.getMe();
      setUser(userInfoRes.data);
      // 3. Don't generate keys locally, only read private keys from localStorage
      // If you want to support multi-device key sync, you can encrypt store/import here
      return { success: true };
    } catch (e) {
      return { success: false, message: e.response?.data?.message || 'Login failed' };
    }
  }

  // Improved logout function
  async function logout(showConfirmDialog = true) {
    // Check if there are private keys that need to be saved
    const hasPrivateKeys = x25519PrivateKey.value || ed25519PrivateKey.value;
    
    if (showConfirmDialog && hasPrivateKeys) {
      // Combined confirmation dialog: logout confirmation + private key save reminder
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
            resolve('cancel'); // Top-right X button cancels logout
          }
        });
      });

      if (result === 'cancel') {
        return false; // User cancels logout, return false
      }

      if (result === 'export') {
        // Export private keys and download file
        const exportObj = { 
          x25519PrivateKey: x25519PrivateKey.value, 
          ed25519PrivateKey: ed25519PrivateKey.value 
        };
        
        // Use common utility function to export private keys
        exportPrivateKeysToFile(exportObj, user.value?.username);
      }
    }

    // Clean all authentication-related data
    setToken(null)
    setUser(null)
    setX25519PrivateKey(null)
    setEd25519PrivateKey(null)
    
    // Clean other potentially existing authentication-related data
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('x25519PrivateKey')
    localStorage.removeItem('ed25519PrivateKey')
    
    // Clean sessionStorage related data
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    
    return true; // Successfully logged out, return true
  }

  // Check if token is expired
  function isTokenExpired() {
    if (!token.value) return true;
    
    try {
      // Parse JWT token to get expiration time
      const payload = JSON.parse(atob(token.value.split('.')[1]));
      const expirationTime = payload.exp * 1000; // Convert to milliseconds
      const currentTime = Date.now();
      
      return currentTime >= expirationTime;
    } catch (e) {
      // If parsing fails, consider token invalid
      return true;
    }
  }

  // Automatically clean expired tokens
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
    cleanExpiredToken,
    setX25519PrivateKey,
    setEd25519PrivateKey
  }
})

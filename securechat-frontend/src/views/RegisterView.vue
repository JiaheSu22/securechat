<!-- RegisterView.vue - Modern Registration Interface -->
<template>
  <div :class="['register-container', { 'dark': isDarkMode }]">
    <!-- Background decoration -->
    <div class="absolute inset-0 bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/20 dark:from-dark-50 dark:via-slate-900 dark:to-purple-900/20"></div>
    <div class="absolute inset-0 bg-gradient-to-tr from-transparent via-blue-50/10 to-purple-50/10 dark:from-transparent dark:via-slate-800/10 dark:to-purple-900/10"></div>
    
    <!-- Dark mode toggle -->
    <button 
      @click="toggleDarkMode"
      class="fixed top-4 right-4 z-50 p-3 rounded-2xl glass-effect shadow-soft hover:shadow-float transition-all duration-200"
    >
      <svg v-if="!isDarkMode" class="w-5 h-5 text-gray-600 hover:text-purple-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path>
      </svg>
      <svg v-else class="w-5 h-5 text-gray-300 hover:text-yellow-400 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path>
      </svg>
    </button>

    <!-- Main register card -->
    <div class="relative z-10 flex items-center justify-center min-h-screen p-4">
      <div class="register-card">
        <!-- Header -->
        <div class="card-header">
          <div class="header-icon">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
            </svg>
          </div>
          <h1 class="card-title">Create Account</h1>
          <p class="card-subtitle">Join Secure Chat and start messaging</p>
        </div>

        <!-- Registration form -->
        <form @submit.prevent="handleRegister" class="register-form">
          <!-- Username field -->
          <div class="form-group">
            <label class="form-label">Username</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
              <input
                v-model="registerForm.username"
                type="text"
                placeholder="This will be used for login"
                class="form-input"
                :class="{ 'error': errors.username }"
                @blur="validateField('username')"
                @input="clearError('username')"
                required
              />
            </div>
            <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
          </div>

          <!-- Nickname field -->
          <div class="form-group">
            <label class="form-label">Nickname</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14"></path>
                </svg>
              </div>
              <input
                v-model="registerForm.nickname"
                type="text"
                placeholder="This will be displayed to others"
                class="form-input"
                :class="{ 'error': errors.nickname }"
                @blur="validateField('nickname')"
                @input="clearError('nickname')"
                required
              />
            </div>
            <span v-if="errors.nickname" class="error-message">{{ errors.nickname }}</span>
          </div>

          <!-- Password field -->
          <div class="form-group">
            <label class="form-label">Password</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
              </div>
              <input
                v-model="registerForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Enter your password"
                class="form-input"
                :class="{ 'error': errors.password }"
                @blur="validateField('password')"
                @input="clearError('password')"
                required
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="password-toggle"
              >
                <svg v-if="showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"></path>
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </button>
            </div>
            <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
          </div>

          <!-- Confirm Password field -->
          <div class="form-group">
            <label class="form-label">Confirm Password</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
              </div>
              <input
                v-model="registerForm.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="Enter your password again"
                class="form-input"
                :class="{ 'error': errors.confirmPassword }"
                @blur="validateField('confirmPassword')"
                @input="clearError('confirmPassword')"
                required
              />
              <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="password-toggle"
              >
                <svg v-if="showConfirmPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"></path>
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </button>
            </div>
            <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
          </div>

          <!-- Submit button -->
          <button
            type="submit"
            :disabled="loading || !isFormValid"
            class="submit-btn"
          >
            <svg v-if="loading" class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>{{ loading ? 'Creating account...' : 'Create Account' }}</span>
          </button>
        </form>

        <!-- Login link -->
        <div class="login-section">
          <p class="login-text">
            Already have an account? 
            <router-link to="/login" class="login-link">Sign in</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// --- State Management ---
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true' || false)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

const errors = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

// --- Computed Properties ---
const isFormValid = computed(() => {
  return registerForm.username.trim() && 
         registerForm.nickname.trim() && 
         registerForm.password.trim() && 
         registerForm.confirmPassword.trim() &&
         !errors.username && 
         !errors.nickname && 
         !errors.password && 
         !errors.confirmPassword
})

// --- Methods ---
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value.toString())
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

const validateField = (field) => {
  const value = registerForm[field]
  errors[field] = ''
  
  if (!value.trim()) {
    errors[field] = `${field.charAt(0).toUpperCase() + field.slice(1)} is required`
  } else if (field === 'username' && value.length < 3) {
    errors[field] = 'Username must be at least 3 characters'
  } else if (field === 'nickname' && value.length < 2) {
    errors[field] = 'Nickname must be at least 2 characters'
  } else if (field === 'password' && value.length < 6) {
    errors[field] = 'Password must be at least 6 characters'
  } else if (field === 'confirmPassword') {
    if (value !== registerForm.password) {
      errors[field] = 'Passwords do not match'
    }
  }
}

const clearError = (field) => {
  if (errors[field]) {
    errors[field] = ''
  }
}

const handleRegister = async () => {
  // Validate all fields
  validateField('username')
  validateField('nickname')
  validateField('password')
  validateField('confirmPassword')
  
  if (!isFormValid.value) return
  
  loading.value = true
  try {
    const result = await authStore.register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname
    })
    
    if (result && result.success) {
      showNotification('Success', 'Registration successful! Please log in.', 'success')
      router.push('/login')
    } else {
      showNotification('Error', result?.message || 'Registration failed', 'error')
    }
  } catch (error) {
    showNotification('Error', 'Registration failed. Please try again.', 'error')
  } finally {
    loading.value = false
  }
}

// Simple notification system (replacing Element Plus)
const showNotification = (title, message, type = 'info') => {
  const notification = document.createElement('div')
  notification.className = `fixed top-4 right-4 z-50 p-4 rounded-2xl shadow-float transition-all duration-300 transform translate-x-full ${
    type === 'success' ? 'bg-green-500 text-white' :
    type === 'error' ? 'bg-red-500 text-white' :
    'bg-blue-500 text-white'
  }`
  
  notification.innerHTML = `
    <div class="flex items-center space-x-3">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        ${type === 'success' ? '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>' :
          type === 'error' ? '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>' :
          '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>'}
      </svg>
      <div>
        <div class="font-semibold">${title}</div>
        <div class="text-sm opacity-90">${message}</div>
      </div>
    </div>
  `
  
  document.body.appendChild(notification)
  
  // Animate in
  setTimeout(() => {
    notification.classList.remove('translate-x-full')
  }, 100)
  
  // Remove after 3 seconds
  setTimeout(() => {
    notification.classList.add('translate-x-full')
    setTimeout(() => {
      document.body.removeChild(notification)
    }, 300)
  }, 3000)
}

// --- Lifecycle ---
onMounted(() => {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark')
  }
})
</script>

<style scoped>
/* Register container */
.register-container {
  @apply min-h-screen w-full overflow-hidden relative;
}

/* Register card */
.register-card {
  @apply w-full max-w-lg bg-white/90 dark:bg-dark-100/90 backdrop-blur-xl rounded-3xl p-8 border border-gray-200/50 dark:border-dark-200/50;
  box-shadow: var(--shadow-float);
}

/* Card header */
.card-header {
  @apply text-center mb-8;
}

.header-icon {
  @apply w-16 h-16 bg-green-500 text-white rounded-3xl flex items-center justify-center mx-auto mb-4;
  box-shadow: var(--shadow-soft);
}

.card-title {
  @apply text-2xl font-bold text-gray-900 dark:text-gray-100 mb-2;
}

.card-subtitle {
  @apply text-gray-600 dark:text-gray-400;
}

/* Register form */
.register-form {
  @apply space-y-6;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium text-gray-700 dark:text-gray-300;
}

.input-wrapper {
  @apply relative;
}

.input-icon {
  @apply absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-600 dark:text-gray-400 pointer-events-none;
}

.form-input {
  @apply w-full pl-10 pr-12 py-4 bg-gray-50/80 dark:bg-dark-200/80 border border-gray-200/50 dark:border-dark-300/50 rounded-2xl text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500/30 focus:border-green-500/50 transition-all duration-200;
}

.form-input.error {
  @apply border-red-300 dark:border-red-600 focus:ring-red-500/30 focus:border-red-500/50;
}

.password-toggle {
  @apply absolute right-3 top-1/2 transform -translate-y-1/2 p-1 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 rounded-lg transition-colors duration-200;
}

.error-message {
  @apply text-sm text-red-500 dark:text-red-400;
}

/* Submit button */
.submit-btn {
  @apply w-full flex items-center justify-center space-x-2 px-6 py-4 bg-green-500 hover:bg-green-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-2xl font-medium transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed;
  box-shadow: var(--shadow-soft);
}

.submit-btn:hover:not(:disabled) {
  box-shadow: var(--shadow-float);
}

/* Login section */
.login-section {
  @apply text-center mt-6 pt-6 border-t border-gray-200/50 dark:border-dark-200/50;
}

.login-text {
  @apply text-gray-600 dark:text-gray-400;
}

.login-link {
  @apply text-green-500 hover:text-green-600 dark:text-green-400 dark:hover:text-green-300 font-medium transition-colors duration-200;
}

/* Responsive design */
@media (max-width: 640px) {
  .register-card {
    @apply mx-4 p-6;
  }
  
  .card-title {
    @apply text-xl;
  }
  
  .form-input {
    @apply py-3;
  }
  
  .submit-btn {
    @apply py-3;
  }
}
</style>

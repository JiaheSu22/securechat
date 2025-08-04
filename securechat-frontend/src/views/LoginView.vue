<!-- LoginView.vue - User login page -->
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 20px; font-weight: bold;">Login</span>
        </div>
      </template>

      <!-- Login form with validation -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="Enter your username"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="Enter your password"
            show-password
            :prefix-icon="Lock"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%;"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? 'Logging in...' : 'Login' }}
          </el-button>
        </el-form-item>

        <div class="register-link">
          <p>Don't have an account? <router-link to="/register">Sign up</router-link></p>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue' // Import reactive and ref from vue
import { useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// --- Core Setup ---
// 1. Define ElForm component reference
const loginFormRef = ref(null)

// 2. Create reactive object to store form data
//    This perfectly matches the template's :model="loginForm" and v-model="loginForm.username"
const loginForm = reactive({
  username: '',
  password: '',
})

// 3. Define validation rules that match the template's :rules="loginRules"
const loginRules = reactive({
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please input password', trigger: 'blur' }],
})
// --- Core Setup End ---

const loading = ref(false)

const handleLogin = async () => {
  if (!loginFormRef.value) return; // Defensive programming, ensure form is mounted

  // Use Element Plus form validation method
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      // Call store's login action with data from loginForm
      const result = await authStore.login(loginForm.username, loginForm.password);
      loading.value = false;
      if (result && result.success) {
        ElNotification({
          title: 'Success',
          message: 'Login successful!',
          type: 'success',
          duration: 2000,
        });
        router.push('/chat');
      } else {
        ElNotification({
          title: 'Error',
          message: result?.message || 'Invalid username or password',
          type: 'error',
          duration: 2000,
        });
      }
    } else {
      // console.log('error submit!');
      return false;
    }
  });
};
</script>

<style scoped>
/* Styles remain unchanged */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100vw;
  height: 100vh;
  background-image: linear-gradient(to top, #a8edea 0%, #fed6e3 100%);
}
.login-card {
  width: 400px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.card-header {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
.register-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}
.register-link a {
  color: #409eff;
  text-decoration: none;
}
.register-link a:hover {
  text-decoration: underline;
}
</style>

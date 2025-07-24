<!-- src/views/LoginView.vue -->
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 20px; font-weight: bold;">Login</span>
        </div>
      </template>

      <!--
        这个表单现在可以正确地找到 script 中定义的 loginFormRef, loginForm 和 loginRules
      -->
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
import { reactive, ref } from 'vue' // 从 vue 导入 reactive 和 ref
import { useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// --- 核心修复 ---
// 1. 定义 ElForm 组件的引用
const loginFormRef = ref(null)

// 2. 使用 reactive 创建一个响应式对象来存储表单数据
//    这完美地匹配了模板中的 :model="loginForm" 和 v-model="loginForm.username"
const loginForm = reactive({
  username: '',
  password: '',
})

// 3. 定义与模板中的 :rules="loginRules" 匹配的验证规则
const loginRules = reactive({
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please input password', trigger: 'blur' }],
})
// --- 核心修复结束 ---

const loading = ref(false)

const handleLogin = async () => {
  if (!loginFormRef.value) return; // 防御式编程，确保表单已挂载

  // 使用 Element Plus 的表单验证方法
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      // 调用 store 的 login action, 传入 loginForm 中的数据
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
      console.log('error submit!');
      return false;
    }
  });
};
</script>

<style scoped>
/* 样式保持不变 */
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

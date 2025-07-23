<!-- src/views/RegisterView.vue -->
<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 20px; font-weight: bold;">Sign Up</span>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
      >
        <el-form-item label="Username" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="This will be used for login"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="Nickname" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="This will be displayed to others"
            :prefix-icon="MagicStick"
            size="large"
          />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="Enter your password"
            show-password
            :prefix-icon="Lock"
            size="large"
          />
        </el-form-item>

        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="Enter your password again"
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
            @click="handleRegister"
          >
            {{ loading ? 'Signing up...' : 'Sign Up' }}
          </el-button>
        </el-form-item>

        <div class="login-link">
          <p>Already have an account? <router-link to="/login">Sign in</router-link></p>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { User, Lock, MagicStick } from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';
import { useAuthStore } from '@/stores/auth'; // 直接静态导入

const router = useRouter();
const registerFormRef = ref(null);
const loading = ref(false);

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
});

// 自定义验证规则：确认密码
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input the password again'));
  } else if (value !== registerForm.password) {
    callback(new Error("Two inputs don't match!"));
  } else {
    callback();
  }
};

const authStore = useAuthStore();

const registerRules = reactive({
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  nickname: [{ required: true, message: 'Please input nickname', trigger: 'blur' }],
  password: [
    { required: true, message: 'Please input password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validatePass, trigger: 'blur' }],
});

const handleRegister = async () => {
  if (!registerFormRef.value) return;
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      // 调用 Pinia 的注册逻辑
      const result = await authStore.register({
        username: registerForm.username,
        password: registerForm.password,
        nickname: registerForm.nickname
      });
      loading.value = false;
      if (result && result.success) {
        ElNotification({
          title: 'Success',
          message: 'Registration successful! Please log in.',
          type: 'success',
        });
        router.push('/login');
      } else {
        ElNotification({
          title: 'Error',
          message: result?.message || 'Registration failed',
          type: 'error',
        });
      }
    } else {
      return false;
    }
  });
};
</script>

<style scoped>
/* 样式与 LoginView 保持一致，但使用不同的类名以防冲突 */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100vw;
  height: 100vh;
  background-image: linear-gradient(to top, #a8edea 0%, #fed6e3 100%);
}

.register-card {
  width: 450px; /* 卡片稍宽以容纳更多字段 */
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>

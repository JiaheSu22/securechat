<template>
  <div class="notfound-container">
    <el-empty description="404 Not Found">
      <el-button type="primary" @click="goLogin">Back to Login</el-button>
    </el-empty>
  </div>
</template>
<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();
const goLogin = () => {
  router.push('/login');
};
</script>
<style scoped>
.notfound-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f7fa;
}
</style> 
// src/services/api.js
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Spring Boot 后端地址
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器：自动加 token
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// 响应拦截器：处理 token 过期
apiClient.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response?.status === 401) {
      // Token 过期或无效
      console.log('Token expired or invalid, logging out...');
      
      // 清理本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('x25519PrivateKey');
      localStorage.removeItem('ed25519PrivateKey');
      
      // 重定向到登录页面
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient; 
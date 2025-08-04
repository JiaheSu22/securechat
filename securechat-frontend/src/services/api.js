// src/services/api.js - API client configuration
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Spring Boot backend address
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor: automatically add token
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// Response interceptor: handle token expiration
apiClient.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      if (process.env.NODE_ENV === 'development') {
        console.log('Token expired or invalid, logging out...');
      }
      
      // Clean local storage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('x25519PrivateKey');
      localStorage.removeItem('ed25519PrivateKey');
      
      // Redirect to login page
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient; 
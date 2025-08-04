// src/services/authService.js - Authentication service
import apiClient from './api';

export const authService = {
  register(data) { // { username, password, nickname }
    return apiClient.post('/auth/register', data);
  },
  login(data) { // { username, password }
    return apiClient.post('/auth/login', data);
  }
}; 
// src/services/userService.js
import apiClient from './api';

export const userService = {
  uploadPublicKey(data) { // { publicKey }
    return apiClient.put('/users/me/key', data);
  }
}; 
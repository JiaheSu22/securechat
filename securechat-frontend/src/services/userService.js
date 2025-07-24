// src/services/userService.js
import apiClient from './api';

export const userService = {
  uploadX25519Key(data) { // { x25519PublicKey }
    return apiClient.put('/users/me/x25519-key', data)
  },
  uploadEd25519Key(data) { // { ed25519PublicKey }
    return apiClient.put('/users/me/ed25519-key', data)
  },
  getMe() {
    return apiClient.get('/users/me')
  }
} 
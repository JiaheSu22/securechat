// src/services/fileService.js
import apiClient from './api';

export const fileService = {
  uploadFile(formData) {
    return apiClient.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
}; 
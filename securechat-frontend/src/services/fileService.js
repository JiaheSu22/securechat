// src/services/fileService.js - File service
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
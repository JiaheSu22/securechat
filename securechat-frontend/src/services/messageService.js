// src/services/messageService.js - Message service
import apiClient from './api';

export const messageService = {
  // Send message
  sendMessage(data) { // data: { receiverUsername, encryptedContent, messageType, fileUrl?, originalFilename? }
    return apiClient.post('/messages', data);
  },
  // Get conversation history with a user
  getConversation(otherUsername) {
    return apiClient.get(`/messages/${otherUsername}`);
  }
}; 
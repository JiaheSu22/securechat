// src/services/messageService.js
import apiClient from './api';

export const messageService = {
  // 发送消息
  sendMessage(data) { // data: { receiverUsername, encryptedContent, messageType, fileUrl?, originalFilename? }
    return apiClient.post('/messages', data);
  },
  // 获取与某用户的历史会话
  getConversation(otherUsername) {
    return apiClient.get(`/messages/${otherUsername}`);
  }
}; 
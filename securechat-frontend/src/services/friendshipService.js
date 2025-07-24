// src/services/friendshipService.js
import apiClient from './api';

export const friendshipService = {
  getMyFriends() {
    return apiClient.get('/friendships/my-friends');
  },
  getPendingRequests() {
    return apiClient.get('/friendships/requests/pending');
  },
  sendRequest(addresseeUsername) {
    return apiClient.post('/friendships/requests', { addresseeUsername });
  },
  acceptRequest(requesterUsername) {
    return apiClient.put('/friendships/requests/accept', { username: requesterUsername });
  },
  declineRequest(requesterUsername) {
    return apiClient.put('/friendships/requests/decline', { username: requesterUsername });
  },
  unfriend(friendUsername) {
    return apiClient.delete('/friendships/unfriend', { data: { username: friendUsername } });
  },
  blockUser(username) {
    return apiClient.post('/friendships/block', { username });
  },
  unblockUser(username) {
    return apiClient.post('/friendships/unblock', { username });
  }
};
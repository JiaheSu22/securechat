<!-- ChatView.vue - Main chat interface component -->
<template>
  <div class="main-layout">
    <!-- Left sidebar: Contact list -->
    <div class="sidebar">
      <div v-if="authStore.user" class="sidebar-header">
        <el-avatar :style="{ backgroundColor: getAvatarColor(authStore.user) }" class="user-avatar" size="default">
          {{ authStore.user.nickname[0].toUpperCase() }}
        </el-avatar>
        <div class="user-info">
          <span class="user-nickname">{{ authStore.user.nickname }}</span>
          <span class="user-id">@{{ authStore.user.username }}</span>
        </div>
        <el-button type="danger" plain @click="handleLogout" class="logout-btn">Logout</el-button>
      </div>

      <div class="contact-list-container">
        <!-- Search and add contact -->
        <div class="search-add-bar">
          <el-input v-model="searchQuery" placeholder="Search contacts..." :prefix-icon="Search" clearable />
          <el-tooltip effect="dark" content="Add New Contact" placement="top">
            <el-button :icon="Plus" circle @click="openAddContactDialog" class="add-btn" />
          </el-tooltip>
        </div>

        <!-- Friend requests section -->
        <div class="friend-requests-section" @click="openFriendRequestsDialog">
          <el-icon><Bell /></el-icon>
          <span>Friend Requests</span>
          <el-badge :value="friendRequests.length" class="request-badge" />
        </div>

        <!-- Contact list -->
        <div class="contact-list">
          <div
            v-for="user in filteredContactList"
            :key="user.id"
            :class="['contact-item', { 'active': currentChatTarget?.id === user.id }]"
            @click="selectChat(user)"
          >
            <el-avatar :style="{ backgroundColor: getAvatarColor(user) }" class="contact-avatar">
              {{ user.nickname[0].toUpperCase() }}
            </el-avatar>
            <div class="contact-info">
              <span class="contact-name">{{ user.nickname }}</span>
              <span class="contact-last-msg">{{ getLastMessageText(user.username) }}</span>
            </div>
            <!-- Unread message count -->
            <span v-if="unreadMap[user.username] > 0" class="unread-dot">{{ unreadMap[user.username] }}</span>
            <!-- Contact actions dropdown menu -->
            <el-dropdown class="more-actions" trigger="click" @command="handleContactCommand" :teleported="false">
              <el-button :icon="MoreFilled" text circle class="more-btn" @click.stop />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="user.status !== 'BLOCKED'"
                    :command="{ action: 'block', user: user }"
                  >
                    <el-icon><CircleClose /></el-icon>
                    <span>Block User</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-else
                    :command="{ action: 'unblock', user: user }"
                  >
                    <el-icon><SuccessFilled /></el-icon>
                    <span>Unblock User</span>
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'delete', user: user }" divided>
                    <el-icon color="#F56C6C"><Delete /></el-icon>
                    <span style="color: #F56C6C;">Delete Contact</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <!-- Key management buttons -->
        <div class="key-actions-bar" style="display: flex; justify-content: center; gap: 12px; margin: 16px 0 24px 0; padding-bottom: 16px;">
          <el-tooltip content="Export Private Key" placement="top">
            <el-button :icon="Download" circle size="small" @click="showExportDialog = true" />
          </el-tooltip>
          <el-tooltip content="Import Private Key" placement="top">
            <el-button :icon="Upload" circle size="small" @click="showImportDialog = true" />
          </el-tooltip>
        </div>
        <el-dialog v-model="showExportDialog" title="Export Private Key" width="400px">
          <el-input type="textarea" :rows="6" :value="exportPrivateKeys()" readonly style="margin-bottom: 10px;" />
          <el-button type="primary" @click="copyToClipboard(exportPrivateKeys())">Copy</el-button>
          <el-button type="info" @click="downloadAsFile(exportPrivateKeys())">Download as File</el-button>
        </el-dialog>
        <el-dialog v-model="showImportDialog" title="Import Private Key" width="400px">
          <el-input type="textarea" v-model="importKeyStr" :rows="6" placeholder="Paste your private key JSON here" style="margin-bottom: 10px;" />
          <el-button type="success" @click="importPrivateKeys(importKeyStr)">Import</el-button>
        </el-dialog>
      </div>
    </div>

    <!-- Right main area: Chat window -->
    <div class="chat-area">
      <template v-if="currentChatTarget">
        <div class="chat-header" style="position: relative; display: flex; align-items: center; justify-content: center;">
          <div class="chat-title" style="flex: 1; text-align: center;">
            <span>{{ currentChatTarget.nickname }}</span>
            <span class="chat-subtitle">@{{ currentChatTarget.username }}</span>
          </div>
        </div>
        <div ref="messageContainerRef" class="message-container">
          <!-- Message rendering -->
          <div v-for="msg in messages" :key="msg.id" :class="['message-row', getMessageClass(msg.sender)]">
            <!-- System messages -->
            <div v-if="msg.sender === 'system'" class="system-message">
              <el-icon v-if="msg.encrypted"><Lock /></el-icon>
              <span :class="{ 'encrypted-text': msg.encrypted }">{{ msg.text }}</span>
            </div>

            <!-- User messages -->
            <div v-else class="message-bubble" style="position: relative;">
              <div v-if="msg.messageType === 'FILE'">
                <div class="file-message-content" style="display: flex; align-items: center;">
                  <el-icon :size="24" style="margin-right: 8px;"><Document /></el-icon>
                  <div class="file-info">
                    <span class="file-name">{{ msg.originalFilename }}</span>
                    <!-- File description display -->
                    <div v-if="msg.text && msg.text !== '[File Encrypted]'" class="file-description">
                      {{ msg.text }}
                    </div>
                  </div>
                </div>
                <!-- Download button, positioned outside bubble -->
                <el-button
                  :icon="Download"
                  circle
                  size="small"
                  class="file-download-btn"
                  @click="handleFileDownload(msg)"
                  :style="getDownloadBtnStyle(msg)"
                  :title="'Download'"
                />
              </div>
              <p v-else>{{ msg.text }}</p>
            </div>
          </div>
        </div>
        <div class="message-input-box">
          <el-tooltip effect="dark" content="Send File" placement="top">
            <el-button :icon="Paperclip" @click="handleAttachFile" class="input-action-btn" />
          </el-tooltip>
          <input type="file" ref="fileInput" style="display:none" @change="onFileChange" />
          <div v-if="selectedFile" class="file-attachment-bubble">
            <span class="remove-attachment" @click="selectedFile = null">×</span>
            <el-icon><Paperclip /></el-icon>
            <span class="file-name">{{ selectedFile.name }}</span>
          </div>
          <el-input
            v-model="newMessage"
            type="textarea"
            placeholder="Type your message here..."
            :autosize="{ minRows: 1, maxRows: 4 }"
            @keydown.enter.prevent="handleEnterKey"
            :disabled="isChatBlocked || !canSendMessage"
            resize="none"
            class="chat-textarea"
          />
          <el-button :icon="Promotion" type="primary" @click="handleSend" :disabled="isChatBlocked || !canSendMessage" class="input-action-btn">Send</el-button>
        </div>
      </template>
      <template v-else>
        <div class="welcome-screen">
          <el-icon :size="80" color="#c0c4cc"><Promotion /></el-icon>
          <h2>Welcome to Secure Chat</h2>
          <p>Select a contact from the left to start a conversation.</p>
        </div>
      </template>
    </div>

    <!-- Add contact dialog -->
    <el-dialog v-model="addContactDialogVisible" title="Add New Contact" width="400px">
      <div class="add-contact-dialog-content">
        <div class="add-contact-search-bar">
          <el-input
            v-model="addFriendUsername"
            placeholder="Enter username to send friend request"
            :prefix-icon="Search"
            @keyup.enter="handleSendFriendRequest"
            clearable
            class="add-contact-input"
          />
          <el-button type="primary" @click="handleSendFriendRequest" class="search-dialog-btn">Send Request</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="addContactDialogVisible = false">Close</el-button>
      </template>
    </el-dialog>

    <!-- Friend requests dialog -->
    <el-dialog v-model="friendRequestsDialogVisible" title="Pending Friend Requests" width="400px">
      <div v-if="!friendRequests.length">No pending requests.</div>
      <div v-for="req in friendRequests" :key="req.id" class="friend-request-item">
        <el-avatar :style="{ backgroundColor: getAvatarColor(req) }" size="small" />
        <span>{{ req.nickname }} (@{{ req.username }})</span>
        <div class="request-actions">
          <el-button size="small" type="success" @click="handleAcceptRequest(req)">Accept</el-button>
          <el-button size="small" type="warning" @click="handleDeclineRequest(req)">Decline</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="friendRequestsDialogVisible = false">Close</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessageBox, ElNotification, ElMessage } from 'element-plus';
// Import required icons
import { Promotion, Search, Paperclip, Plus, MoreFilled, Delete, Bell, Lock, CircleClose, SuccessFilled, Download, Upload, Document } from '@element-plus/icons-vue';
import { friendshipService } from '@/services/friendshipService';
// Import STOMP client for WebSocket communication
import { Client } from '@stomp/stompjs';
// Import encryption utilities
import sodium from 'libsodium-wrappers'
// Import message and file services
import { messageService } from '@/services/messageService';
import { fileService } from '@/services/fileService';
import { exportPrivateKeysToFile, copyPrivateKeysToClipboard } from '@/utils/keyExport';

const router = useRouter();
const authStore = useAuthStore();

// --- State Management ---
const contactList = ref([]);
const currentChatTarget = ref(null);
const newMessage = ref('');
const messages = ref([]);
const messageContainerRef = ref(null);
const searchQuery = ref('');
const blockedUsers = ref(new Set()); // Store blocked user IDs
const friendRequests = ref([]);
const unreadMap = ref({}); // username -> count
const sessionKeyMap = ref({}); // username -> Uint8Array
const lastMessageMap = ref({}); // username -> last message object
const hasPrivateKeys = computed(() => {
  return !!(authStore.x25519PrivateKey && authStore.ed25519PrivateKey);
});

// --- Friend Management State ---
const addContactDialogVisible = ref(false);
const addFriendUsername = ref("");
const friendRequestsDialogVisible = ref(false);

// --- Key Import/Export State ---
const showExportDialog = ref(false);
const showImportDialog = ref(false);
const importKeyStr = ref('');

// --- File Attachment State ---
const selectedFile = ref(null);
const fileInput = ref(null);

// --- Computed Properties ---
const filteredContactList = computed(() => {
  if (!searchQuery.value) return contactList.value;
  return contactList.value.filter(contact =>
    contact.nickname.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    contact.username.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const isChatBlocked = computed(() => {
  return currentChatTarget.value?.status === 'BLOCKED';
});

const canSendMessage = computed(() => {
  // Check if current chat target exists and is not blocked
  return !!currentChatTarget.value && !isChatBlocked.value;
});

// --- Color Pool and Avatar Colors ---
const colorPool = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FED766', '#9B59B6', '#F39C12', '#1ABC9C', '#3498DB'];
const getAvatarColor = (user) => {
  if (!user) return '#CCCCCC';
  // Use username for hash to ensure consistent color for same user
  const key = user.username || user.nickname || '';
  let hash = 0;
  for (let i = 0; i < key.length; i++) {
    hash = key.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colorPool[Math.abs(hash) % colorPool.length];
};

const stompClient = ref(null); // Store STOMP client instance
const isSocketConnected = ref(false);

// --- WebSocket Connection and Subscription ---
const connectWebSocket = () => {
  const token = localStorage.getItem('token');
  if (stompClient.value && stompClient.value.connected) return;
  const client = new Client({
    brokerURL: `ws://localhost:8080/ws?token=${token}`,
    reconnectDelay: 5000,
    onConnect: () => {
      isSocketConnected.value = true;
      // Subscribe to personal message queue
      client.subscribe('/user/queue/private', async (message) => {
        try {
          const msgObj = JSON.parse(message.body);
          // console.log('Received WebSocket message:', msgObj);
          
          // Process all received messages, regardless of current chat window
          const sessionKey = sessionKeyMap.value[msgObj.senderUsername];
          let text = '[Decryption Failed]';
          if (msgObj.messageType === 'TEXT') {
            if (sessionKey) {
              // console.log('Decryption Parameters (Real-time Message)', {
              //   sessionKey,
              //   nonce: msgObj.nonce,
              //   encryptedContent: msgObj.encryptedContent,
              //   sender: msgObj.senderUsername,
              //   currentUser: authStore.user.username
              // });
              text = await decryptMessage(sessionKey, msgObj.nonce, msgObj.encryptedContent);
              if (!text) text = '[Decryption Failed]';
            }
          } else if (msgObj.messageType === 'FILE') {
            // File messages need description decryption
            if (sessionKey) {
              // console.log('Decryption Parameters (File Message)', {
              //   sessionKey,
              //   nonce: msgObj.nonce,
              //   encryptedContent: msgObj.encryptedContent,
              //   sender: msgObj.senderUsername,
              //   currentUser: authStore.user.username
              // });
              text = await decryptMessage(sessionKey, msgObj.nonce, msgObj.encryptedContent);
              if (!text) text = '[Decryption Failed]';
            } else {
              text = '[Decryption Failed]';
            }
          }
          
          const messageObj = {
            id: msgObj.id,
            text,
            sender: msgObj.senderUsername,
            messageType: msgObj.messageType,
            fileUrl: msgObj.fileUrl,
            originalFilename: msgObj.originalFilename,
            nonce: msgObj.nonce,
            timestamp: msgObj.timestamp,
            encrypted: true
          };
          
          // Update latest message
          if (!lastMessageMap.value[msgObj.senderUsername] || 
              new Date(msgObj.timestamp) > new Date(lastMessageMap.value[msgObj.senderUsername].timestamp)) {
            lastMessageMap.value[msgObj.senderUsername] = messageObj;
          }
          
          // Only process messages from current chat target
          if (!currentChatTarget.value || msgObj.senderUsername !== currentChatTarget.value.username) {
            unreadMap.value[msgObj.senderUsername] = (unreadMap.value[msgObj.senderUsername] || 0) + 1;
            ElNotification({
              title: 'New Message',
              message: `From ${msgObj.senderUsername}`,
              type: 'info'
            });
            return;
          }
          
          // If message is from current chat target, add to message list
          messages.value.push(messageObj);
          scrollToBottom();
        } catch (e) {
          messages.value.push({
            id: Date.now(),
            text: '[Message Processing Error]',
            sender: 'system',
            encrypted: false
          });
        }
      });
    },
    onDisconnect: () => {
      isSocketConnected.value = false;
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message'], frame.body);
    },
  });
  client.activate();
  stompClient.value = client;
};

// --- Lifecycle Hooks ---
onMounted(async () => {
  if (authStore.isAuthenticated) {
    // Check if token is expired
    if (authStore.isTokenExpired()) {
      // console.log('Token expired, logging out...');
      await authStore.logout(false); // Don't show confirmation dialog
      router.push('/login');
      return;
    }

    // Set timer to check token expiration every minute
    const tokenCheckInterval = setInterval(() => {
      if (authStore.isTokenExpired()) {
        // console.log('Token expired during session, logging out...');
        clearInterval(tokenCheckInterval);
        authStore.logout(false); // Don't show confirmation dialog
        router.push('/login');
      }
    }, 60000); // Check every minute

    // Get friend list
    try {
      const res = await friendshipService.getMyFriends();
      contactList.value = res.data;
      blockedUsers.value = new Set(contactList.value.filter(u => u.status === 'BLOCKED').map(u => u.id));
      
      // Load latest messages for all contacts
      await loadLastMessagesForAllContacts();
    } catch (e) {
      contactList.value = [];
    }
    // Get friend requests
    try {
      const reqRes = await friendshipService.getPendingRequests();
      friendRequests.value = reqRes.data;
    } catch (e) {
      friendRequests.value = [];
    }
    connectWebSocket(); // Establish WebSocket connection
  }
});

// --- Event Handlers ---
const selectChat = async (user) => {
  if (currentChatTarget.value?.id === user.id) return;

  currentChatTarget.value = user;
  messages.value = [];
  unreadMap.value[user.username] = 0; // Clear unread count when switching chats

  // System welcome message
  messages.value.push({
    id: 'system-welcome',
    text: 'Messages are end-to-end encrypted. No one outside of this chat, not even Secure Chat, can read them.',
    sender: 'system',
    encrypted: true
  });

  // Show system prompt if user is blocked
  if (user.status === 'BLOCKED') {
    messages.value.push({
      id: 'system-blocked',
      text: 'You have blocked this user. You can no longer send messages.',
      sender: 'system'
    });
  }

  // Negotiate session key
  const sessionKey = await getSessionKey(user.username);
  sessionKeyMap.value[user.username] = sessionKey;
  
  // Load conversation history
  try {
    const res = await messageService.getConversation(user.username);
    let hasHistoryMessages = false;
    let hasDecryptionFailed = false;
    
    for (const msg of res.data) {
      hasHistoryMessages = true;
      let text = '[Decryption Failed]';
      if (msg.messageType === 'TEXT') {
        if (sessionKey) {
          // console.log('Decryption Parameters (Historical Messages)', {
          //   sessionKey,
          //   nonce: msg.nonce,
          //   encryptedContent: msg.encryptedContent,
          //   sender: msg.senderUsername,
          //   currentUser: authStore.user.username
          // });
          text = await decryptMessage(sessionKey, msg.nonce, msg.encryptedContent);
          if (!text) {
            text = '[Decryption Failed]';
            hasDecryptionFailed = true;
          }
        } else {
          hasDecryptionFailed = true;
        }
      } else if (msg.messageType === 'FILE') {
        // File messages need description decryption
        if (sessionKey) {
          // console.log('Decryption Parameters (Historical File Messages)', {
          //   sessionKey,
          //   nonce: msg.nonce,
          //   encryptedContent: msg.encryptedContent,
          //   sender: msg.senderUsername,
          //   currentUser: authStore.user.username
          // });
          text = await decryptMessage(sessionKey, msg.nonce, msg.encryptedContent);
          if (!text) {
            text = '[Decryption Failed]';
            hasDecryptionFailed = true;
          }
        } else {
          text = '[Decryption Failed]';
          hasDecryptionFailed = true;
        }
      }
      
      const messageObj = {
        id: msg.id,
        text,
        sender: msg.senderUsername,
        messageType: msg.messageType,
        fileUrl: msg.fileUrl,
        originalFilename: msg.originalFilename,
        nonce: msg.nonce,
        timestamp: msg.timestamp,
        encrypted: true
      };
      
      messages.value.push(messageObj);
      
      // Update latest message
      if (!lastMessageMap.value[user.username] || 
          new Date(msg.timestamp) > new Date(lastMessageMap.value[user.username].timestamp)) {
        lastMessageMap.value[user.username] = messageObj;
      }
    }
    
    // Show prompt if there are historical messages but no keys
    if (hasHistoryMessages && !hasPrivateKeys.value) {
      messages.value.push({
        id: 'system-no-keys',
        text: 'Import your private keys to view encrypted message history.',
        sender: 'system',
        encrypted: false
      });
    }
    
    scrollToBottom();
  } catch (e) {
    messages.value.push({
      id: 'system-history-error',
      text: '[Failed to load history messages]',
      sender: 'system'
    });
  }
};

const handleEnterKey = (event) => {
  if (isChatBlocked.value) return;
  if (event.shiftKey) {
    // Shift + Enter for new line
    // Insert newline at current cursor position
    const textarea = event.target;
    const value = newMessage.value;
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    newMessage.value = value.slice(0, start) + '\n' + value.slice(end);
    // Move cursor to new line
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd = start + 1;
    });
    event.preventDefault();
    return;
  } else {
    // Enter to send
    handleSend();
  }
};

const handleSend = async () => {
  if (selectedFile.value) {
    // Send file message
    const sessionKey = sessionKeyMap.value[currentChatTarget.value.username];
    if (!sessionKey) {
      ElMessage.error('Key negotiation failed, cannot send file');
      return;
    }
    
    // Generate nonce for file encryption
    await sodium.ready;
    const NONCE_BYTES = sodium.crypto_aead_chacha20poly1305_ietf_NPUBBYTES;
    const fileNonce = sodium.randombytes_buf(NONCE_BYTES);
    const fileNonceB64 = sodium.to_base64(fileNonce, sodium.base64_variants.URLSAFE_NO_PADDING);
    
    // Encrypt file
    const arrayBuffer = await selectedFile.value.arrayBuffer();
    const plainBytes = new Uint8Array(arrayBuffer);
    const cipherBytes = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
      plainBytes,
      null,
      null,
      fileNonce,
      sessionKey
    );
    const encryptedBlob = new Blob([cipherBytes]);
    
    const uploadRes = await uploadEncryptedFile(encryptedBlob, selectedFile.value.name);
    
    // Encrypt file description (using same nonce)
    const fileDescription = newMessage.value || '[File Encrypted]';
    const encryptedDescription = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
      sodium.from_string(fileDescription),
      null,
      null,
      fileNonce,
      sessionKey
    );
    
    await messageService.sendMessage({
      receiverUsername: currentChatTarget.value.username,
      messageType: 'FILE',
      fileUrl: uploadRes.fileUrl,
      originalFilename: selectedFile.value.name,
      encryptedContent: sodium.to_base64(encryptedDescription, sodium.base64_variants.URLSAFE_NO_PADDING), // Encrypted file description
      nonce: fileNonceB64 // Use file encryption nonce
    });
    ElMessage.success('File encrypted and sent');
    // After file is sent successfully
    const fileMessageObj = {
      id: uploadRes.id || Date.now(), // Use backend returned id or local timestamp
      text: fileDescription, // Display plain text description locally
      sender: authStore.user.username,
      messageType: 'FILE',
      fileUrl: uploadRes.fileUrl,
      originalFilename: selectedFile.value.name,
      nonce: fileNonceB64,
      timestamp: new Date().toISOString(),
      encrypted: true
    };
    
    messages.value.push(fileMessageObj);
    
    // Update latest message
    if (!lastMessageMap.value[currentChatTarget.value.username] || 
        new Date(fileMessageObj.timestamp) > new Date(lastMessageMap.value[currentChatTarget.value.username].timestamp)) {
      lastMessageMap.value[currentChatTarget.value.username] = fileMessageObj;
    }
    
    selectedFile.value = null;
    newMessage.value = '';
    return;
  }
  // Send text message
  if (newMessage.value.trim() === '' || !currentChatTarget.value) return;
  const sessionKey = sessionKeyMap.value[currentChatTarget.value.username];
  if (!sessionKey) {
    ElMessage.error('Key negotiation failed, cannot send message');
    return;
  }
  const { nonce, ciphertext } = await encryptMessage(sessionKey, newMessage.value);
  // Send to backend
  try {
    const res = await messageService.sendMessage({
      receiverUsername: currentChatTarget.value.username,
      encryptedContent: ciphertext,
      nonce,
      messageType: 'TEXT'
    });
    // Insert message into local message list using backend response (ensuring id, time consistency)
    const msg = res.data;
    const textMessageObj = {
      id: msg.id,
      text: newMessage.value, // Display plain text locally
      sender: authStore.user.username,
      timestamp: msg.timestamp,
      encrypted: true
    };
    
    messages.value.push(textMessageObj);
    
    // Update latest message
    if (!lastMessageMap.value[currentChatTarget.value.username] || 
        new Date(textMessageObj.timestamp) > new Date(lastMessageMap.value[currentChatTarget.value.username].timestamp)) {
      lastMessageMap.value[currentChatTarget.value.username] = textMessageObj;
    }
    
    newMessage.value = '';
    scrollToBottom();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Message sending failed.');
  }
};

const getMessageClass = (sender) => {
  if (sender === 'system') return 'system';
  return sender === authStore.user.username ? 'sent' : 'received';
};

const getLastMessageText = (username) => {
  const lastMessage = lastMessageMap.value[username];
  if (!lastMessage) {
    return 'Click to start chatting...';
  }
  
  if (lastMessage.messageType === 'FILE') {
    return '[File]';
  }
  
  // Show special prompt for decryption failed messages
  if (lastMessage.text === '[Decryption Failed]') {
    return 'Import keys to view message';
  }
  
  // Limit message length
  const maxLength = 30;
  if (lastMessage.text.length > maxLength) {
    return lastMessage.text.substring(0, maxLength) + '...';
  }
  
  return lastMessage.text;
};

// Load latest messages for all contacts
const loadLastMessagesForAllContacts = async () => {
  for (const contact of contactList.value) {
    try {
      // Negotiate session key
      const sessionKey = await getSessionKey(contact.username);
      sessionKeyMap.value[contact.username] = sessionKey;
      
      // Get latest message
      const res = await messageService.getConversation(contact.username);
      if (res.data && res.data.length > 0) {
        // Get the latest message
        const latestMsg = res.data[res.data.length - 1];
        let text = '[Decryption Failed]';
        
        if (latestMsg.messageType === 'TEXT') {
          if (sessionKey) {
            text = await decryptMessage(sessionKey, latestMsg.nonce, latestMsg.encryptedContent);
            if (!text) text = '[Decryption Failed]';
          }
        } else if (latestMsg.messageType === 'FILE') {
          // File messages need description decryption
          if (sessionKey) {
            text = await decryptMessage(sessionKey, latestMsg.nonce, latestMsg.encryptedContent);
            if (!text) text = '[Decryption Failed]';
          } else {
            text = '[Decryption Failed]';
          }
        }
        
        const messageObj = {
          id: latestMsg.id,
          text,
          sender: latestMsg.senderUsername,
          messageType: latestMsg.messageType,
          fileUrl: latestMsg.fileUrl,
          originalFilename: latestMsg.originalFilename,
          nonce: latestMsg.nonce,
          timestamp: latestMsg.timestamp,
          encrypted: true
        };
        
        lastMessageMap.value[contact.username] = messageObj;
      }
    } catch (e) {
      // console.log(`Failed to load last message for ${contact.username}:`, e);
    }
  }
};

const handleLogout = async () => {
  // Call authStore.logout(), which will show a combined confirmation dialog
  const logoutSuccess = await authStore.logout();
  
  // Only redirect to login page if logout is successful
  if (logoutSuccess) {
    router.push('/login');
  }
  // If user cancels logout (clicks X), stay on current page
};

const handleAttachFile = () => {
  if (fileInput.value) fileInput.value.click();
};

// --- 好友管理方法 ---
const openAddContactDialog = () => {
  addFriendUsername.value = "";
  addContactDialogVisible.value = true;
};

const handleSendFriendRequest = async () => {
  const username = addFriendUsername.value.trim();
  if (!username) {
    ElMessage.warning("Please enter a username.");
    return;
  }
  try {
    await friendshipService.sendRequest(username);
    ElMessage.success(`Friend request sent to ${username}.`);
    addFriendUsername.value = "";
    addContactDialogVisible.value = false;
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "Failed to send friend request.");
  }
};

const handleContactCommand = (command) => {
  const { action, user } = command;
  if (action === 'delete') {
    handleDeleteContact(user);
  } else if (action === 'block') {
    handleBlockContact(user);
  } else if (action === 'unblock') {
    handleUnblockContact(user);
  }
};

const handleDeleteContact = async (contactToDelete) => {
  ElMessageBox.confirm(
    `Are you sure you want to delete ${contactToDelete.nickname} from your contacts?`,
    'Confirm Deletion',
    {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
    }
  ).then(async () => {
    // 调用后端API
    await friendshipService.unfriend(contactToDelete.username);
    // 刷新数据
    await refreshFriendData();
    // 如果当前聊天对象被删，清空聊天
    if (currentChatTarget.value?.id === contactToDelete.id) {
      currentChatTarget.value = null;
      messages.value = [];
    }
    ElMessage({ type: 'success', message: `${contactToDelete.nickname} has been deleted.` });
  }).catch(() => {});
};

// --- 新增：拉黑与解除拉黑 ---
const isBlocked = (userId) => {
  return blockedUsers.value.has(userId);
};

const handleBlockContact = async (userToBlock) => {
  await friendshipService.blockUser(userToBlock.username);
  ElMessage({ type: 'warning', message: `${userToBlock.nickname} has been blocked.` });
  await refreshFriendData();
  // 如果正在与该用户聊天，则添加系统消息
  if (currentChatTarget.value?.id === userToBlock.id) {
    messages.value.push({
      id: 'system-blocked-action',
      text: 'You have blocked this user. You can no longer send messages.',
      sender: 'system'
    });
    scrollToBottom();
  }
};

const handleUnblockContact = async (userToUnblock) => {
  await friendshipService.unblockUser(userToUnblock.username);
  ElMessage({ type: 'success', message: `${userToUnblock.nickname} has been unblocked.` });
  await refreshFriendData();
  // 如果正在与该用户聊天，则添加系统消息
  if (currentChatTarget.value?.id === userToUnblock.id) {
    messages.value.push({
      id: 'system-unblocked-action',
      text: 'You have unblocked this user.',
      sender: 'system'
    });
    scrollToBottom();
  }
};

const openFriendRequestsDialog = () => {
  friendRequestsDialogVisible.value = true;
};

const refreshFriendData = async () => {
  try {
    const res = await friendshipService.getMyFriends();
    contactList.value = res.data;
    // 更新 blockedUsers
    blockedUsers.value = new Set(res.data.filter(u => u.status === 'BLOCKED').map(u => u.id));
    // 关键：同步 currentChatTarget 的最新状态
    if (currentChatTarget.value) {
      const updated = res.data.find(u => u.id === currentChatTarget.value.id);
      if (updated) {
        currentChatTarget.value = updated;
      }
    }
    // 新增：同步 friendPublicKeys
    // friendPublicKeys.value = new Map(); // 已废弃
    // for (const user of res.data) {
    //   keyMap.set(user.username, user.publicKey);
    // }
    // friendPublicKeys.value = keyMap;
  } catch {
    contactList.value = [];
    blockedUsers.value = new Set();
    // friendPublicKeys.value = new Map(); // 已废弃
  }
  try {
    const reqRes = await friendshipService.getPendingRequests();
    friendRequests.value = reqRes.data;
  } catch {
    friendRequests.value = [];
  }
};

const handleAcceptRequest = async (req) => {
  await friendshipService.acceptRequest(req.username);
  ElMessage.success('Friend request accepted!');
  await refreshFriendData();
};

const handleDeclineRequest = async (req) => {
  await friendshipService.declineRequest(req.username);
  ElMessage.success('Friend request declined!');
  await refreshFriendData();
};

const handleDeleteRequest = async (req) => {
  await friendshipService.declineRequest(req.username); // 如果没有单独 delete API
  ElMessage.success('Deleted!');
  await refreshFriendData();
};

const handleBlockRequest = async (req) => {
  await friendshipService.blockUser(req.username);
  ElMessage.success('Blocked!');
  await refreshFriendData();
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainerRef.value) {
      messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight;
    }
  });
};

// 假设 authStore.x25519PrivateKey, friend.x25519PublicKey 都是 base64 字符串
async function getSessionKey(friendUsername) {
  await sodium.ready;
  const myPrivateKey = sodium.from_base64(authStore.x25519PrivateKey, sodium.base64_variants.URLSAFE_NO_PADDING);
  const theirPublicKey = sodium.from_base64(
    contactList.value.find(u => u.username === friendUsername)?.x25519PublicKey,
    sodium.base64_variants.URLSAFE_NO_PADDING
  );
  if (!myPrivateKey || !theirPublicKey) {
    console.error('Key missing', myPrivateKey, theirPublicKey);
    return null;
  }
  return sodium.crypto_scalarmult(myPrivateKey, theirPublicKey);
}

async function encryptMessage(sessionKey, plainText) {
  await sodium.ready;
  const NONCE_BYTES = sodium.crypto_aead_chacha20poly1305_ietf_NPUBBYTES;
  const nonce = sodium.randombytes_buf(NONCE_BYTES);
  const cipher = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
    sodium.from_string(plainText),
    null,
    null,
    nonce,
    sessionKey
  );
  return {
    nonce: sodium.to_base64(nonce, sodium.base64_variants.URLSAFE_NO_PADDING),
    ciphertext: sodium.to_base64(cipher, sodium.base64_variants.URLSAFE_NO_PADDING)
  };
}

async function decryptMessage(sessionKey, nonceB64, cipherB64) {
  await sodium.ready;
  const nonce = sodium.from_base64(nonceB64, sodium.base64_variants.URLSAFE_NO_PADDING);
  const cipher = sodium.from_base64(cipherB64, sodium.base64_variants.URLSAFE_NO_PADDING);
  try {
    const plain = sodium.crypto_aead_chacha20poly1305_ietf_decrypt(
      null,
      cipher,
      null,
      nonce,
      sessionKey
    );
    return sodium.to_string(plain);
  } catch (e) {
    return null;
  }
}

async function encryptFile(file, sessionKey) {
  await sodium.ready;
  const NONCE_BYTES = sodium.crypto_aead_chacha20poly1305_ietf_NPUBBYTES;
  const nonce = sodium.randombytes_buf(NONCE_BYTES);

  // 读取文件为 ArrayBuffer
  const arrayBuffer = await file.arrayBuffer();
  const plainBytes = new Uint8Array(arrayBuffer);

  // 加密
  const cipherBytes = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
    plainBytes,
    null, // 无附加数据
    null,
    nonce,
    sessionKey
  );

  // 返回密文 Blob、nonce
  return {
    encryptedBlob: new Blob([cipherBytes]),
    nonce: sodium.to_base64(nonce, sodium.base64_variants.URLSAFE_NO_PADDING)
  };
}

async function uploadEncryptedFile(encryptedBlob, originalFilename) {
  const formData = new FormData();
  formData.append('file', encryptedBlob, originalFilename);
  // 假设 fileService.uploadFile 已实现
  const res = await fileService.uploadFile(formData);
  return res.data; // { fileUrl, ... }
}

async function handleSendFile(file) {
  const sessionKey = sessionKeyMap.value[currentChatTarget.value.username];
  if (!sessionKey) {
    ElMessage.error('Key negotiation failed, cannot send file');
    return;
  }
  
  // 生成一个nonce用于文件加密
  await sodium.ready;
  const NONCE_BYTES = sodium.crypto_aead_chacha20poly1305_ietf_NPUBBYTES;
  const fileNonce = sodium.randombytes_buf(NONCE_BYTES);
  const fileNonceB64 = sodium.to_base64(fileNonce, sodium.base64_variants.URLSAFE_NO_PADDING);
  
  // 加密文件
  const arrayBuffer = await file.arrayBuffer();
  const plainBytes = new Uint8Array(arrayBuffer);
  const cipherBytes = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
    plainBytes,
    null,
    null,
    fileNonce,
    sessionKey
  );
  const encryptedBlob = new Blob([cipherBytes]);
  
  // 上传密文
  const uploadRes = await uploadEncryptedFile(encryptedBlob, file.name);
  
  // 加密文件描述（使用相同的nonce）
  const fileDescription = newMessage.value || '[File Encrypted]';
  const encryptedDescription = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
    sodium.from_string(fileDescription),
    null,
    null,
    fileNonce,
    sessionKey
  );
  
  // 发送文件消息
  await messageService.sendMessage({
    receiverUsername: currentChatTarget.value.username,
    messageType: 'FILE',
    fileUrl: uploadRes.fileUrl,
    originalFilename: file.name,
    encryptedContent: sodium.to_base64(encryptedDescription, sodium.base64_variants.URLSAFE_NO_PADDING), // 加密的文件描述
    nonce: fileNonceB64 // 使用文件加密的nonce
  });
  ElMessage.success('File encrypted and sent');
  // 清空输入框和附件标识
  selectedFile.value = null;
  newMessage.value = '';
}

async function handleFileDownload(msg) {
  const token = localStorage.getItem('token');
  const response = await fetch(msg.fileUrl, {
    headers: { Authorization: 'Bearer ' + token }
  });
  const cipherBuffer = await response.arrayBuffer();
  await sodium.ready;
  const nonceBytes = sodium.from_base64(msg.nonce, sodium.base64_variants.URLSAFE_NO_PADDING);
  const cipherBytes = new Uint8Array(cipherBuffer);
  // 这里要区分：自己发的用 currentChatTarget，收到的用 msg.senderUsername
  const sessionKey = sessionKeyMap.value[
    msg.sender === authStore.user.username ? currentChatTarget.value.username : msg.sender
  ];
  const plainBytes = sodium.crypto_aead_chacha20poly1305_ietf_decrypt(
    null, cipherBytes, null, nonceBytes, sessionKey
  );
  const blob = new Blob([plainBytes]);
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = msg.originalFilename;
  a.click();
  URL.revokeObjectURL(url);
}

function exportPrivateKeys() {
  const x25519 = localStorage.getItem('x25519PrivateKey');
  const ed25519 = localStorage.getItem('ed25519PrivateKey');
  const exportObj = { x25519PrivateKey: x25519, ed25519PrivateKey: ed25519 };
  return JSON.stringify(exportObj, null, 2);
}

function importPrivateKeys(jsonStr) {
  try {
    const obj = JSON.parse(jsonStr);
    if (obj.x25519PrivateKey && obj.ed25519PrivateKey) {
      localStorage.setItem('x25519PrivateKey', obj.x25519PrivateKey);
      localStorage.setItem('ed25519PrivateKey', obj.ed25519PrivateKey);
      // 可选：刷新页面或重载 Pinia 状态
      window.location.reload();
    } else {
      ElMessage.error('Invalid private key data');
    }
  } catch (e) {
    ElMessage.error('Failed to parse private key');
  }
}

function copyToClipboard(text) {
  copyPrivateKeysToClipboard(JSON.parse(text));
}

function downloadAsFile(text) {
  const keys = JSON.parse(text);
  exportPrivateKeysToFile(keys, authStore.user?.username);
}

function onFileChange(e) {
  const file = e.target.files[0];
  if (file) {
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error('File size must not exceed 50MB');
      return;
    }
    selectedFile.value = file; // 设置文件引用
    newMessage.value = `[Attachment] ${file.name}`; // 设置文件描述
  }
  e.target.value = '';
}

// 新增：图片预览功能
const previewImage = (url) => {
  ElMessageBox.open({
    title: 'Image Preview',
    message: `<img src="${url}" style="max-width: 100%; max-height: 80vh; display: block; margin: 0 auto;" />`,
    customClass: 'image-preview-dialog',
    showConfirmButton: false,
    showCancelButton: true,
    cancelButtonText: 'Close',
    confirmButtonText: 'Download',
    confirmButtonClass: 'el-button--primary',
    cancelButtonClass: 'el-button--danger',
    beforeClose: (done, type) => {
      if (type === 'cancel') {
        done();
      } else if (type === 'confirm') {
        const a = document.createElement('a');
        a.href = url;
        a.download = url.substring(url.lastIndexOf('/') + 1);
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        done();
      }
    }
  });
};

function getDownloadBtnStyle(msg) {
  // 自己发的放左侧，收到的放右侧
  if (msg.sender === authStore.user.username) {
    return 'position: absolute; left: -38px; top: 50%; transform: translateY(-50%);';
  } else {
    return 'position: absolute; right: -38px; top: 50%; transform: translateY(-50%);';
  }
}

</script>

<style scoped>
/* 样式与之前版本相同，无需修改 */
/* --- Layout & Sidebar --- */
.main-layout { display: flex; height: 100vh; width: 100vw; background-color: #f0f2f5; }
.sidebar { display: flex; flex-direction: column; width: 320px; background-color: #e9ecf1; border-right: 1px solid #dcdfe6; }
.sidebar-header { display: flex; align-items: center; padding: 10px 15px; height: 70px; border-bottom: 1px solid #dcdfe6; flex-shrink: 0; }
.user-avatar { flex-shrink: 0; font-weight: bold; }
.user-info { display: flex; flex-direction: column; margin-left: 12px; flex-grow: 1; overflow: hidden; }
.user-nickname { font-size: 16px; font-weight: bold; color: #303133; }
.user-id { font-size: 12px; color: #a8aeb3; }
.logout-btn { margin-left: auto; flex-shrink: 0; }
.contact-list-container { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; }

/* --- Search & Add Bar --- */
.search-add-bar { display: flex; align-items: center; padding: 10px; border-bottom: 1px solid #dcdfe6; gap: 8px; }
.add-btn { flex-shrink: 0; }

/* --- Friend Requests --- */
.friend-requests-section { display: flex; align-items: center; padding: 12px 15px; cursor: pointer; border-bottom: 1px solid #e4e7ed; background-color: #fff; color: #409eff; font-weight: 500; }
.friend-requests-section:hover { background-color: #f0f8ff; }
.friend-requests-section span { margin-left: 8px; flex-grow: 1; }
.request-badge { margin-right: 5px; }

/* --- Contact List --- */
.contact-list { flex-grow: 1; overflow-y: auto; }
.contact-item { position: relative; display: flex; align-items: center; padding: 12px 15px; cursor: pointer; border-bottom: 1px solid #e4e7ed; transition: background-color 0.2s; }
.contact-item:hover { background-color: #d9e1e8; }
.contact-item.active { background-color: #cdd3da; }
.contact-avatar { flex-shrink: 0; font-weight: bold; }
.contact-info { margin-left: 12px; display: flex; flex-direction: column; flex-grow: 1; overflow: hidden; }
.contact-name { font-weight: 500; font-size: 15px; color: #303133; }
.contact-last-msg { font-size: 12px; color: #909399; text-overflow: ellipsis; white-space: nowrap; overflow: hidden; }
.more-actions { position: absolute; right: 10px; opacity: 0; transition: opacity 0.2s; }
.contact-item:hover .more-actions { opacity: 1; }
.more-btn { --el-button-text-color: #8c939d; }

/* --- Chat Area --- */
.chat-area { flex-grow: 1; display: flex; flex-direction: column; background-color: #f5f7fa; }
.chat-header { display: flex; align-items: center; justify-content: center; height: 70px; padding: 0 20px; border-bottom: 1px solid #e4e7ed; flex-shrink: 0; background-color: #fff; }
.chat-title { text-align: center; }
.chat-title span { display: block; }
.chat-title span:first-child { font-size: 18px; font-weight: bold; color: #000000; }
.chat-title span.chat-subtitle { font-size: 12px; color: #b0b4ba; }
.message-container { flex-grow: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px; }
.message-row { display: flex; max-width: 70%; word-break: break-word; }
.message-row.sent { align-self: flex-end; }
.message-row.received { align-self: flex-start; }
.message-row.system { align-self: center; width: 100%; justify-content: center; }
.message-bubble { padding: 10px 15px; border-radius: 18px; word-wrap: break-word; max-width: 420px; white-space: pre-wrap; }
.message-bubble p { margin: 0; line-height: 1.4; font-size: 14px; }
.message-row.sent .message-bubble { background-color: #409eff; color: #fff; border-bottom-right-radius: 4px; }
.message-row.received .message-bubble { background-color: #ffffff; color: #303133; border-bottom-left-radius: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.system-message { display: flex; align-items: center; justify-content: center; text-align: center; color: #909399; font-size: 12px; background-color: #e9e9eb; padding: 4px 12px; border-radius: 10px; }
.system-message .el-icon { margin-right: 6px; }
.system-message .encrypted-text { font-weight: 500; }
.message-input-box {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #f8f9fa;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 10px;
  position: relative; /* 让气泡绝对定位于输入框上方 */
}
.input-action-btn {
  align-self: flex-end;
  flex: 0 0 auto;
}
.chat-textarea {
  flex: 1 1 0;
  min-width: 0;
  max-width: 600px;
  min-width: 200px;
}

/* --- Welcome Screen --- */
.welcome-screen { display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; color: #909399; }
.welcome-screen h2 { font-size: 24px; font-weight: 300; margin: 20px 0 10px; }

/* --- Add Contact Dialog --- */
.add-contact-dialog-content {
  margin-bottom: 10px;
}
.add-contact-search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}
.add-contact-input {
  flex: 1 1 0;
}
.search-dialog-btn {
  flex-shrink: 0;
}
.search-results-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.search-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.search-result-item:last-child {
  border-bottom: none;
}
.empty-results {
  color: #b0b4ba;
  text-align: center; 
  padding: 16px 0;
}

/* --- Friend Request Dialog --- */
.friend-request-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.friend-request-item:last-child {
  border-bottom: none;
}
.request-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}
.input-warning {
  display: flex;
  align-items: center;
  color: #F56C6C;
  font-size: 14px;
  margin: 8px 0 0 0;
  gap: 6px;
}
.unread-dot {
  display: inline-block;
  min-width: 18px;
  padding: 0 6px;
  background: #f56c6c;
  color: #fff;
  border-radius: 10px;
  font-size: 12px;
  text-align: center;
  margin-left: 8px;
  position: absolute;
  right: 40px; /* 根据你的布局调整 */
  top: 50%;
  transform: translateY(-50%);
}
.key-actions {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 8px;
}

/* 新增：图片预览弹窗样式 */
.image-preview-dialog .el-message-box__content {
  padding: 20px;
}
.image-preview-dialog .el-message-box__content img {
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.image-preview-dialog .el-message-box__content .el-message-box__title {
  text-align: left;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}
.image-preview-dialog .el-message-box__content .el-message-box__message {
  text-align: left;
  padding-top: 10px;
}
.image-preview-dialog .el-message-box__content .el-message-box__btns {
  justify-content: flex-start;
}
.image-preview-dialog .el-message-box__content .el-message-box__btns .el-button {
  margin-left: 10px;
}

/* 新增：附件气泡样式 */
.file-attachment-bubble {
  display: inline-flex;
  align-items: center;
  background: #f3f6fa;
  color: #333;
  border-radius: 16px;
  padding: 6px 16px 6px 10px;
  margin-bottom: 6px;
  position: absolute;
  left: 20px;
  top: -38px; /* 视输入框高度微调 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  z-index: 10;
}
.remove-attachment {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
  margin-right: 8px;
  cursor: pointer;
  position: absolute;
  left: 4px;
  top: 2px;
}
.file-name {
  font-weight: 500;
  margin-left: 6px;
}

/* 新增：文件描述样式 */
.file-description {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  font-style: italic;
  line-height: 1.3;
}
</style>

<!-- ChatView.vue - Refactored Main View -->
<template>
  <div class="main-layout">
    <!-- Left sidebar -->
    <ChatSidebar
      :user="authStore.user"
      :contacts="filteredContactList"
      :friend-requests="friendRequests"
      :current-chat-target="currentChatTarget"
      :unread-map="unreadMap"
      :search-query="searchQuery"
      :get-last-message-text="getLastMessageText"
      :get-avatar-color="getAvatarColor"
      @logout="handleLogout"
      @open-add-contact="addContactDialogVisible = true"
      @open-friend-requests="friendRequestsDialogVisible = true"
      @select-chat="selectChat"
      @contact-command="handleContactCommand"
      @update:searchQuery="searchQuery = $event"
      @import-keys="importPrivateKeys"
    />

    <!-- Right main area: Chat window -->
    <ChatWindow
      ref="chatWindowRef"
      :current-chat-target="currentChatTarget"
      :messages="messages"

      :selected-file="selectedFile"
      :is-chat-blocked="isChatBlocked"
      :can-send-message="canSendMessage"
      :get-message-class="getMessageClass"
      :get-download-btn-style="getDownloadBtnStyle"
      v-model:newMessage="newMessage"
      @send-message="handleSend"
      @attach-file="handleAttachFile"
      @remove-attachment="selectedFile = null"
      @file-changed="onFileChange"
      @download-file="handleFileDownload"
    />

    <!-- Dialogs -->
    <AddContactDialog
      v-model:visible="addContactDialogVisible"
      @send-friend-request="handleSendFriendRequest"
    />

    <FriendRequestsDialog
      v-model:visible="friendRequestsDialogVisible"
      :requests="friendRequests"
      :get-avatar-color="getAvatarColor"
      @accept-request="handleAcceptRequest"
      @decline-request="handleDeclineRequest"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessageBox, ElNotification, ElMessage } from 'element-plus';
import { friendshipService } from '@/services/friendshipService';
import { Client } from '@stomp/stompjs';
import sodium from 'libsodium-wrappers';
import { messageService } from '@/services/messageService';
import { fileService } from '@/services/fileService';

// Import child components
import ChatSidebar from '@/components/chat/ChatSidebar.vue';
import ChatWindow from '@/components/chat/ChatWindow.vue';
import AddContactDialog from '@/components/chat/AddContactDialog.vue';
import FriendRequestsDialog from '@/components/chat/FriendRequestsDialog.vue';

const router = useRouter();
const authStore = useAuthStore();

// --- State Management ---
const contactList = ref([]);
const currentChatTarget = ref(null);
const newMessage = ref('');
const messages = ref([]);
const searchQuery = ref('');
const blockedUsers = ref(new Set());
const friendRequests = ref([]);
const unreadMap = ref({});
const sessionKeyMap = ref({});
const lastMessageMap = ref({});
const selectedFile = ref(null);

// --- Dialog visibility state ---
const addContactDialogVisible = ref(false);
const friendRequestsDialogVisible = ref(false);

// --- Child component refs ---
const chatWindowRef = ref(null);

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
  return !!currentChatTarget.value && !isChatBlocked.value;
});

const hasPrivateKeys = computed(() => {
  return !!(authStore.x25519PrivateKey && authStore.ed25519PrivateKey);
});

// --- Avatar Colors ---
const colorPool = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FED766', '#9B59B6', '#F39C12', '#1ABC9C', '#3498DB'];
const getAvatarColor = (user) => {
  if (!user) return '#CCCCCC';
  const key = user.username || user.nickname || '';
  let hash = 0;
  for (let i = 0; i < key.length; i++) {
    hash = key.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colorPool[Math.abs(hash) % colorPool.length];
};

// --- WebSocket ---
const stompClient = ref(null);
const isSocketConnected = ref(false);

const connectWebSocket = () => {
  const token = localStorage.getItem('token');
  if (stompClient.value && stompClient.value.connected) return;
  const client = new Client({
    brokerURL: `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws?token=${token}`,
    reconnectDelay: 5000,
    onConnect: () => {
      isSocketConnected.value = true;
      client.subscribe('/user/queue/private', async (message) => {
        try {
          const msgObj = JSON.parse(message.body);
          const sessionKey = sessionKeyMap.value[msgObj.senderUsername];
          let text = '[Decryption Failed]';

          if (msgObj.messageType === 'TEXT' || msgObj.messageType === 'FILE') {
            if (sessionKey) {
              text = await decryptMessage(sessionKey, msgObj.nonce, msgObj.encryptedContent);
              if (!text) text = '[Decryption Failed]';
            }
          }
          
          const messageObject = {
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

          if (!lastMessageMap.value[msgObj.senderUsername] || new Date(msgObj.timestamp) > new Date(lastMessageMap.value[msgObj.senderUsername].timestamp)) {
            lastMessageMap.value[msgObj.senderUsername] = messageObject;
          }
          
          if (!currentChatTarget.value || msgObj.senderUsername !== currentChatTarget.value.username) {
            unreadMap.value[msgObj.senderUsername] = (unreadMap.value[msgObj.senderUsername] || 0) + 1;
            ElNotification({ title: 'New Message', message: `From ${msgObj.senderUsername}`, type: 'info' });
            return;
          }
          
          messages.value.push(messageObject);
          scrollToBottom();
        } catch (e) {
          messages.value.push({ id: Date.now(), text: '[Message Processing Error]', sender: 'system', encrypted: false });
        }
      });
    },
    onDisconnect: () => { isSocketConnected.value = false; },
    onStompError: (frame) => { console.error('STOMP error:', frame.headers['message'], frame.body); },
  });
  client.activate();
  stompClient.value = client;
};

// --- Lifecycle Hooks ---
onMounted(async () => {
  if (authStore.isAuthenticated) {
    if (authStore.isTokenExpired()) {
      await authStore.logout(false);
      router.push('/login');
      return;
    }
    const tokenCheckInterval = setInterval(() => {
      if (authStore.isTokenExpired()) {
        clearInterval(tokenCheckInterval);
        authStore.logout(false);
        router.push('/login');
      }
    }, 60000);

    await refreshFriendData();
    await loadLastMessagesForAllContacts();
    connectWebSocket();
  }
});


// --- Event Handlers ---
const selectChat = async (user) => {
  if (currentChatTarget.value?.id === user.id) return;

  currentChatTarget.value = user;
  messages.value = [];
  unreadMap.value[user.username] = 0;

  messages.value.push({
    id: 'system-welcome',
    text: 'Messages are end-to-end encrypted. No one outside of this chat, not even Secure Chat, can read them.',
    sender: 'system',
    encrypted: true
  });

  if (user.status === 'BLOCKED') {
    messages.value.push({ id: 'system-blocked', text: 'You have blocked this user. You can no longer send messages.', sender: 'system' });
  }

  const sessionKey = await getSessionKey(user.username);
  sessionKeyMap.value[user.username] = sessionKey;
  
  try {
    const res = await messageService.getConversation(user.username);
    let hasHistoryMessages = res.data && res.data.length > 0;
    
    for (const msg of res.data) {
      let text = '[Decryption Failed]';
      if (msg.messageType === 'TEXT' || msg.messageType === 'FILE') {
          if(sessionKey) {
            text = await decryptMessage(sessionKey, msg.nonce, msg.encryptedContent);
            if (!text) text = '[Decryption Failed]';
          }
      }
      
      const messageObj = {
        id: msg.id, text, sender: msg.senderUsername, messageType: msg.messageType,
        fileUrl: msg.fileUrl, originalFilename: msg.originalFilename, nonce: msg.nonce,
        timestamp: msg.timestamp, encrypted: true
      };
      
      messages.value.push(messageObj);
      
      if (!lastMessageMap.value[user.username] || new Date(msg.timestamp) > new Date(lastMessageMap.value[user.username].timestamp)) {
        lastMessageMap.value[user.username] = messageObj;
      }
    }
    
    if (hasHistoryMessages && !hasPrivateKeys.value) {
      messages.value.push({ id: 'system-no-keys', text: 'Import your private keys to view encrypted message history.', sender: 'system', encrypted: false });
    }
    
    scrollToBottom();
  } catch (e) {
    messages.value.push({ id: 'system-history-error', text: '[Failed to load history messages]', sender: 'system' });
  }
};

const handleSend = async () => {
    if (selectedFile.value) {
        await sendFile();
    } else {
        await sendTextMessage();
    }
};

const sendTextMessage = async () => {
    if (newMessage.value.trim() === '' || !currentChatTarget.value) return;
    const sessionKey = sessionKeyMap.value[currentChatTarget.value.username];
    if (!sessionKey) {
        ElMessage.error('Key negotiation failed, cannot send message');
        return;
    }
    const { nonce, ciphertext } = await encryptMessage(sessionKey, newMessage.value);
    try {
        const res = await messageService.sendMessage({
            receiverUsername: currentChatTarget.value.username,
            encryptedContent: ciphertext,
            nonce,
            messageType: 'TEXT'
        });
        const msg = res.data;
        const textMessageObj = {
            id: msg.id,
            text: newMessage.value,
            sender: authStore.user.username,
            timestamp: msg.timestamp,
            encrypted: true
        };
        messages.value.push(textMessageObj);
        if (!lastMessageMap.value[currentChatTarget.value.username] || new Date(textMessageObj.timestamp) > new Date(lastMessageMap.value[currentChatTarget.value.username].timestamp)) {
            lastMessageMap.value[currentChatTarget.value.username] = textMessageObj;
        }
        newMessage.value = '';
        scrollToBottom();
    } catch (e) {
        ElMessage.error(e?.response?.data?.message || 'Message sending failed.');
    }
};

const sendFile = async () => {
    const sessionKey = sessionKeyMap.value[currentChatTarget.value.username];
    if (!sessionKey) {
        ElMessage.error('Key negotiation failed, cannot send file');
        return;
    }

    await sodium.ready;
    const NONCE_BYTES = sodium.crypto_aead_chacha20poly1305_ietf_NPUBBYTES;
    const fileNonce = sodium.randombytes_buf(NONCE_BYTES);
    const fileNonceB64 = sodium.to_base64(fileNonce, sodium.base64_variants.URLSAFE_NO_PADDING);

    const arrayBuffer = await selectedFile.value.arrayBuffer();
    const plainBytes = new Uint8Array(arrayBuffer);
    const cipherBytes = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(plainBytes, null, null, fileNonce, sessionKey);
    const encryptedBlob = new Blob([cipherBytes]);

    const formData = new FormData();
    formData.append('file', encryptedBlob, selectedFile.value.name);
    const uploadRes = await fileService.uploadFile(formData).then(res => res.data);

    const fileDescription = newMessage.value || '[File Encrypted]';
    const encryptedDescription = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(sodium.from_string(fileDescription), null, null, fileNonce, sessionKey);

    await messageService.sendMessage({
        receiverUsername: currentChatTarget.value.username,
        messageType: 'FILE',
        fileUrl: uploadRes.fileUrl,
        originalFilename: selectedFile.value.name,
        encryptedContent: sodium.to_base64(encryptedDescription, sodium.base64_variants.URLSAFE_NO_PADDING),
        nonce: fileNonceB64
    });

    ElMessage.success('File encrypted and sent');
    
    const fileMessageObj = {
        id: uploadRes.id || Date.now(),
        text: fileDescription,
        sender: authStore.user.username,
        messageType: 'FILE',
        fileUrl: uploadRes.fileUrl,
        originalFilename: selectedFile.value.name,
        nonce: fileNonceB64,
        timestamp: new Date().toISOString(),
        encrypted: true
    };
    messages.value.push(fileMessageObj);
    if (!lastMessageMap.value[currentChatTarget.value.username] || new Date(fileMessageObj.timestamp) > new Date(lastMessageMap.value[currentChatTarget.value.username].timestamp)) {
        lastMessageMap.value[currentChatTarget.value.username] = fileMessageObj;
    }
    selectedFile.value = null;
    newMessage.value = '';
    scrollToBottom();
};


const getMessageClass = (sender) => {
  if (sender === 'system') return 'system';
  return sender === authStore.user.username ? 'sent' : 'received';
};

const getLastMessageText = (username) => {
  const lastMessage = lastMessageMap.value[username];
  if (!lastMessage) return 'Click to start chatting...';
  if (lastMessage.messageType === 'FILE') return '[File]';
  if (lastMessage.text === '[Decryption Failed]') return 'Import keys to view message';
  const maxLength = 30;
  return lastMessage.text.length > maxLength ? lastMessage.text.substring(0, maxLength) + '...' : lastMessage.text;
};

const loadLastMessagesForAllContacts = async () => {
  for (const contact of contactList.value) {
    try {
      const sessionKey = await getSessionKey(contact.username);
      sessionKeyMap.value[contact.username] = sessionKey;
      
      const res = await messageService.getConversation(contact.username);
      if (res.data && res.data.length > 0) {
        const latestMsg = res.data[res.data.length - 1];
        let text = '[Decryption Failed]';
        
        if (latestMsg.messageType === 'TEXT' || latestMsg.messageType === 'FILE') {
            if(sessionKey) {
                text = await decryptMessage(sessionKey, latestMsg.nonce, latestMsg.encryptedContent);
                if (!text) text = '[Decryption Failed]';
            }
        }
        
        const messageObj = {
            id: latestMsg.id, text, sender: latestMsg.senderUsername, messageType: latestMsg.messageType,
            fileUrl: latestMsg.fileUrl, originalFilename: latestMsg.originalFilename, nonce: latestMsg.nonce,
            timestamp: latestMsg.timestamp, encrypted: true
        };
        lastMessageMap.value[contact.username] = messageObj;
      }
    } catch (e) {
      // console.error(`Failed to load last message for ${contact.username}:`, e);
    }
  }
};

const handleLogout = async () => {
  const logoutSuccess = await authStore.logout();
  if (logoutSuccess) router.push('/login');
};

const handleAttachFile = () => {
    if(chatWindowRef.value) chatWindowRef.value.triggerFileInput();
};

const onFileChange = (file) => {
  if (file) {
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error('File size must not exceed 50MB');
      return;
    }
    selectedFile.value = file;
    newMessage.value = `[Attachment] ${file.name}`;
  }
};

// --- Friend Management ---
const handleSendFriendRequest = async (username) => {
  if (!username.trim()) {
    ElMessage.warning("Please enter a username.");
    return;
  }
  try {
    await friendshipService.sendRequest(username.trim());
    ElMessage.success(`Friend request sent to ${username.trim()}.`);
    addContactDialogVisible.value = false;
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "Failed to send friend request.");
  }
};

const handleContactCommand = (command) => {
  const { action, user } = command;
  if (action === 'delete') handleDeleteContact(user);
  else if (action === 'block') handleBlockContact(user);
  else if (action === 'unblock') handleUnblockContact(user);
};

const handleDeleteContact = async (contactToDelete) => {
  ElMessageBox.confirm(`Are you sure you want to delete ${contactToDelete.nickname}?`, 'Confirm Deletion', { type: 'warning' })
    .then(async () => {
      await friendshipService.unfriend(contactToDelete.username);
      await refreshFriendData();
      if (currentChatTarget.value?.id === contactToDelete.id) {
        currentChatTarget.value = null;
        messages.value = [];
      }
      ElMessage({ type: 'success', message: `${contactToDelete.nickname} has been deleted.` });
    }).catch(() => {});
};

const handleBlockContact = async (userToBlock) => {
  await friendshipService.blockUser(userToBlock.username);
  ElMessage({ type: 'warning', message: `${userToBlock.nickname} has been blocked.` });
  await refreshFriendData();
  if (currentChatTarget.value?.id === userToBlock.id) {
    messages.value.push({ id: 'system-blocked-action', text: 'You have blocked this user.', sender: 'system' });
    scrollToBottom();
  }
};

const handleUnblockContact = async (userToUnblock) => {
  await friendshipService.unblockUser(userToUnblock.username);
  ElMessage({ type: 'success', message: `${userToUnblock.nickname} has been unblocked.` });
  await refreshFriendData();
  if (currentChatTarget.value?.id === userToUnblock.id) {
    messages.value.push({ id: 'system-unblocked-action', text: 'You have unblocked this user.', sender: 'system' });
    scrollToBottom();
  }
};

const refreshFriendData = async () => {
    try {
        const [friendsRes, requestsRes] = await Promise.all([
            friendshipService.getMyFriends(),
            friendshipService.getPendingRequests()
        ]);
        
        contactList.value = friendsRes.data;
        blockedUsers.value = new Set(friendsRes.data.filter(u => u.status === 'BLOCKED').map(u => u.id));
        
        if (currentChatTarget.value) {
            const updatedTarget = friendsRes.data.find(u => u.id === currentChatTarget.value.id);
            if (updatedTarget) {
                currentChatTarget.value = updatedTarget;
            } else {
                // If current chat target is no longer a friend, clear the chat
                currentChatTarget.value = null;
                messages.value = [];
            }
        }
        
        friendRequests.value = requestsRes.data;
    } catch (e) {
        contactList.value = [];
        friendRequests.value = [];
        blockedUsers.value = new Set();
        // console.error("Failed to refresh friend data:", e);
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

// --- E2EE ---
async function getSessionKey(friendUsername) {
  await sodium.ready;
  const myPrivateKeyB64 = authStore.x25519PrivateKey;
  const theirPublicKeyB64 = contactList.value.find(u => u.username === friendUsername)?.x25519PublicKey;
  
  if (!myPrivateKeyB64 || !theirPublicKeyB64) {
    // console.error('Key missing for session key negotiation.');
    return null;
  }
  const myPrivateKey = sodium.from_base64(myPrivateKeyB64, sodium.base64_variants.URLSAFE_NO_PADDING);
  const theirPublicKey = sodium.from_base64(theirPublicKeyB64, sodium.base64_variants.URLSAFE_NO_PADDING);
  return sodium.crypto_scalarmult(myPrivateKey, theirPublicKey);
}

async function encryptMessage(sessionKey, plainText) {
  await sodium.ready;
  const NONCE_BYTES = sodium.crypto_aead_chacha20poly1305_ietf_NPUBBYTES;
  const nonce = sodium.randombytes_buf(NONCE_BYTES);
  const cipher = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(sodium.from_string(plainText), null, null, nonce, sessionKey);
  return {
    nonce: sodium.to_base64(nonce, sodium.base64_variants.URLSAFE_NO_PADDING),
    ciphertext: sodium.to_base64(cipher, sodium.base64_variants.URLSAFE_NO_PADDING)
  };
}

async function decryptMessage(sessionKey, nonceB64, cipherB64) {
  try {
    await sodium.ready;
    const nonce = sodium.from_base64(nonceB64, sodium.base64_variants.URLSAFE_NO_PADDING);
    const cipher = sodium.from_base64(cipherB64, sodium.base64_variants.URLSAFE_NO_PADDING);
    const plain = sodium.crypto_aead_chacha20poly1305_ietf_decrypt(null, cipher, null, nonce, sessionKey);
    return sodium.to_string(plain);
  } catch (e) {
    // console.error("Decryption failed:", e);
    return null;
  }
}

async function handleFileDownload(msg) {
  const token = localStorage.getItem('token');
  const response = await fetch(msg.fileUrl, { headers: { Authorization: 'Bearer ' + token } });
  const cipherBuffer = await response.arrayBuffer();
  
  await sodium.ready;
  const nonceBytes = sodium.from_base64(msg.nonce, sodium.base64_variants.URLSAFE_NO_PADDING);
  const cipherBytes = new Uint8Array(cipherBuffer);
  
  // Always use the sender's session key for decryption.
  const sessionKey = sessionKeyMap.value[msg.sender];
  
  if(!sessionKey) {
      ElMessage.error("Session key not found for file decryption. Please ensure you have an active session with the sender.");
      return;
  }

  try {
    const plainBytes = sodium.crypto_aead_chacha20poly1305_ietf_decrypt(null, cipherBytes, null, nonceBytes, sessionKey);
    const blob = new Blob([plainBytes]);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = msg.originalFilename;
    a.click();
    URL.revokeObjectURL(url);
  } catch(e) {
      ElMessage.error("File decryption failed. The key might be incorrect or the file corrupted.");
  }
}

function importPrivateKeys(jsonStr) {
  try {
    const obj = JSON.parse(jsonStr);
    if (obj.x25519PrivateKey && obj.ed25519PrivateKey) {
      // It's better to delegate state updates to the store
      authStore.setX25519PrivateKey(obj.x25519PrivateKey);
      authStore.setEd25519PrivateKey(obj.ed25519PrivateKey);
      
      ElMessage.success('Private keys imported successfully! Reloading...');
      setTimeout(() => window.location.reload(), 1500);
    } else {
      ElMessage.error('Invalid private key data. The file must contain x25519PrivateKey and ed25519PrivateKey.');
    }
  } catch (e) {
    ElMessage.error('Failed to parse private key JSON. Please ensure the format is correct.');
  }
}

function getDownloadBtnStyle(msg) {
  if (msg.sender === authStore.user.username) {
    return 'position: absolute; left: -38px; top: 50%; transform: translateY(-50%);';
  } else {
    return 'position: absolute; right: -38px; top: 50%; transform: translateY(-50%);';
  }
}

// --- Utils ---
const scrollToBottom = () => {
    if(chatWindowRef.value) {
        chatWindowRef.value.scrollToBottom();
    }
};

</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f0f2f5;
}
</style>
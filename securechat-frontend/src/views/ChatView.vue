<!-- src/views/ChatView.vue (FIXED ICON NAME) -->
<template>
  <div class="main-layout">
    <!-- 左侧边栏: 联系人列表 -->
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
        <!-- 搜索与添加好友 -->
        <div class="search-add-bar">
          <el-input v-model="searchQuery" placeholder="Search contacts..." :prefix-icon="Search" clearable />
          <el-tooltip effect="dark" content="Add New Contact" placement="top">
            <!-- *** FIX: 使用正确的图标名称 Plus *** -->
            <el-button :icon="Plus" circle @click="openAddContactDialog" class="add-btn" />
          </el-tooltip>
        </div>

        <!-- 好友申请 -->
        <div class="friend-requests-section" @click="openFriendRequestsDialog">
          <el-icon><Bell /></el-icon>
          <span>Friend Requests</span>
          <el-badge :value="friendRequests.length" class="request-badge" />
        </div>

        <!-- 联系人列表 -->
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
              <span class="contact-last-msg">Click to start chatting...</span>
            </div>
            <!-- 红点未读数 -->
            <span v-if="unreadMap[user.username] > 0" class="unread-dot">{{ unreadMap[user.username] }}</span>
            <!-- 删除好友/拉黑下拉菜单 -->
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
      </div>
    </div>

    <!-- 右侧主区域: 聊天窗口 -->
    <div class="chat-area">
      <template v-if="currentChatTarget">
        <div class="chat-header" style="position: relative; display: flex; align-items: center; justify-content: center;">
          <div class="chat-title" style="flex: 1; text-align: center;">
            <span>{{ currentChatTarget.nickname }}</span>
            <span class="chat-subtitle">@{{ currentChatTarget.username }}</span>
          </div>
          <div class="key-actions" style="position: absolute; right: 20px; top: 50%; transform: translateY(-50%); display: flex; gap: 8px;">
            <el-tooltip content="Export Private Key" placement="bottom">
              <el-button :icon="Download" circle size="small" @click="showExportDialog = true" />
            </el-tooltip>
            <el-tooltip content="Import Private Key" placement="bottom">
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
        <div ref="messageContainerRef" class="message-container">
          <!-- 消息渲染 -->
          <div v-for="msg in messages" :key="msg.id" :class="['message-row', getMessageClass(msg.sender)]">
            <div v-if="msg.sender === 'system'" class="system-message">
              <el-icon v-if="msg.encrypted"><Lock /></el-icon>
              <span :class="{ 'encrypted-text': msg.encrypted }">{{ msg.text }}</span>
            </div>
            <div v-else-if="msg.fileUrl" class="message-bubble">
              <p>
                <el-icon><Paperclip /></el-icon>
                <span>{{ msg.originalFilename || 'Encrypted File' }}</span>
                <el-button size="small" type="primary" @click="downloadAndDecryptFile(msg.fileUrl, msg.nonce, sessionKeyMap.value[msg.sender === authStore.user.username ? currentChatTarget.value.username : msg.sender], msg.originalFilename)">Download</el-button>
              </p>
            </div>
            <div v-else class="message-bubble">
              <p>{{ msg.text }}</p>
            </div>
          </div>
        </div>
        <div class="message-input-box">
          <el-tooltip effect="dark" content="Send File" placement="top">
            <el-button :icon="Paperclip" @click="() => $refs.fileInput.click()" class="input-action-btn" />
          </el-tooltip>
          <input type="file" ref="fileInput" style="display:none" @change="onFileChange" />
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

    <!-- 添加好友弹窗 -->
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

    <!-- 好友请求弹窗 -->
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
// *** FIX: 引入所需图标 ***
import { Promotion, Search, Paperclip, Plus, MoreFilled, Delete, Bell, Lock, CircleClose, SuccessFilled, Download, Upload } from '@element-plus/icons-vue';
import { friendshipService } from '@/services/friendshipService';
// 新增：引入 STOMP 客户端
import { Client } from '@stomp/stompjs';
// 新增：引入加密工具
import sodium from 'libsodium-wrappers'
// import { encryptMessage } from '@/services/crypto'; // 删除此行，避免重复声明
// 新增：引入解密工具
// import { decryptMessage } from '@/services/crypto'; // 删除此行，避免重复声明
// 新增：引入消息服务
import { messageService } from '@/services/messageService';

const router = useRouter();
const authStore = useAuthStore();

// --- 状态管理 ---
const contactList = ref([]);
const currentChatTarget = ref(null);
const newMessage = ref('');
const messages = ref([]);
const messageContainerRef = ref(null);
const searchQuery = ref('');
const blockedUsers = ref(new Set()); // 新增：用于存储被拉黑的用户ID
const friendRequests = ref([]);
// 删除 friendPublicKeys 相关的老公钥缓存
// const friendPublicKeys = ref(new Map()); // 已废弃
const unreadMap = ref({}); // username -> count
const sessionKeyMap = ref({}); // username -> Uint8Array

// --- 好友管理状态 ---
const addContactDialogVisible = ref(false);
const addFriendUsername = ref("");
const friendRequestsDialogVisible = ref(false);

// --- 密钥导入/导出状态 ---
const showExportDialog = ref(false);
const showImportDialog = ref(false);
const importKeyStr = ref('');

// --- 计算属性 ---
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
  // 只需判断当前聊天对象是否存在和未被拉黑
  return !!currentChatTarget.value && !isChatBlocked.value;
});

// --- 颜色池和头像颜色 ---
const colorPool = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FED766', '#9B59B6', '#F39C12', '#1ABC9C', '#3498DB'];
const getAvatarColor = (user) => {
  if (!user) return '#CCCCCC';
  // 用 username 做 hash，保证同一用户颜色一致
  const key = user.username || user.nickname || '';
  let hash = 0;
  for (let i = 0; i < key.length; i++) {
    hash = key.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colorPool[Math.abs(hash) % colorPool.length];
};

const stompClient = ref(null); // 保存 STOMP 客户端实例
const isSocketConnected = ref(false);

// --- WebSocket 连接与订阅 ---
const connectWebSocket = () => {
  const token = localStorage.getItem('token');
  if (stompClient.value && stompClient.value.connected) return;
  const client = new Client({
    brokerURL: `ws://localhost:8080/ws?token=${token}`,
    reconnectDelay: 5000,
    onConnect: () => {
      isSocketConnected.value = true;
      // 订阅个人消息队列
      client.subscribe('/user/queue/private', async (message) => {
        // 新增：解析消息并解密
        try {
          const msgObj = JSON.parse(message.body);
          // 只处理当前聊天对象的消息
          if (!currentChatTarget.value || msgObj.senderUsername !== currentChatTarget.value.username) {
            // 不是当前聊天对象，做未读标记
            unreadMap.value[msgObj.senderUsername] = (unreadMap.value[msgObj.senderUsername] || 0) + 1;
            // 可选：弹窗通知
            ElNotification({
              title: 'New Message',
              message: `From ${msgObj.senderUsername}`,
              type: 'info'
            });
            return;
          }
          const sessionKey = sessionKeyMap.value[msgObj.senderUsername];
          let text = '[Decryption Failed]';
          if (sessionKey) {
            console.log('Decryption Parameters (Real-time Message)', {
              sessionKey,
              nonce: msgObj.nonce,
              encryptedContent: msgObj.encryptedContent,
              sender: msgObj.senderUsername,
              currentUser: authStore.user.username
            });
            text = await decryptMessage(sessionKey, msgObj.nonce, msgObj.encryptedContent);
            if (!text) text = '[Decryption Failed]';
          }
          messages.value.push({
            id: msgObj.id,
            text,
            sender: msgObj.senderUsername,
          });
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

// --- 生命周期钩子 ---
onMounted(async () => {
  if (authStore.isAuthenticated) {
    // 获取好友列表
    try {
      const res = await friendshipService.getMyFriends();
      contactList.value = res.data;
      blockedUsers.value = new Set(contactList.value.filter(u => u.status === 'BLOCKED').map(u => u.id));
      // 新增：缓存好友公钥
      const keyMap = new Map();
      for (const user of res.data) {
        keyMap.set(user.username, user.x25519PublicKey); // 缓存 x25519 公钥
      }
      // friendPublicKeys.value = keyMap; // 已废弃
    } catch (e) {
      contactList.value = [];
      // friendPublicKeys.value = new Map(); // 已废弃
    }
    // 获取好友请求
    try {
      const reqRes = await friendshipService.getPendingRequests();
      friendRequests.value = reqRes.data;
    } catch (e) {
      friendRequests.value = [];
    }
    connectWebSocket(); // 新增：建立 WebSocket 连接
  }
});

// --- 事件处理 ---
const selectChat = async (user) => {
  if (currentChatTarget.value?.id === user.id) return;

  currentChatTarget.value = user;
  messages.value = [];
  unreadMap.value[user.username] = 0; // 在切换聊天时清除未读

  // 系统欢迎消息
  messages.value.push({
    id: 'system-welcome',
    text: 'Messages are end-to-end encrypted. No one outside of this chat, not even Secure Chat, can read them.',
    sender: 'system',
    encrypted: true
  });

  // 如果被拉黑，显示系统提示
  if (user.status === 'BLOCKED') {
    messages.value.push({
      id: 'system-blocked',
      text: 'You have blocked this user. You can no longer send messages.',
      sender: 'system'
    });
  }

  // 协商密钥
  const sessionKey = await getSessionKey(user.username);
  sessionKeyMap.value[user.username] = sessionKey;
  // 拉取历史消息
  try {
    const res = await messageService.getConversation(user.username);
    for (const msg of res.data) {
      let text = '[Decryption Failed]';
      if (sessionKey) {
        console.log('Decryption Parameters (Historical Messages)', {
          sessionKey,
          nonce: msg.nonce,
          encryptedContent: msg.encryptedContent,
          sender: msg.senderUsername,
          currentUser: authStore.user.username
        });
        text = await decryptMessage(sessionKey, msg.nonce, msg.encryptedContent);
        if (!text) text = '[Decryption Failed]';
      }
      messages.value.push({
        id: msg.id,
        text,
        sender: msg.senderUsername,
        timestamp: msg.timestamp,
        encrypted: true
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
    // Shift + Enter 换行
    // 在当前光标处插入换行符
    const textarea = event.target;
    const value = newMessage.value;
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    newMessage.value = value.slice(0, start) + '\n' + value.slice(end);
    // 移动光标到新行
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd = start + 1;
    });
    event.preventDefault();
    return;
  } else {
    // Enter 发送
    handleSend();
  }
};

const handleSend = async () => {
  if (newMessage.value.trim() === '' || !currentChatTarget.value) return;
  const sessionKey = sessionKeyMap.value[currentChatTarget.value.username];
  if (!sessionKey) {
    ElMessage.error('Key negotiation failed, cannot send message');
    return;
  }
  const { nonce, ciphertext } = await encryptMessage(sessionKey, newMessage.value);
  // 发送到后端
  try {
    const res = await messageService.sendMessage({
      receiverUsername: currentChatTarget.value.username,
      encryptedContent: ciphertext,
      nonce,
      messageType: 'TEXT'
    });
    // 用后端返回的消息插入本地消息列表（保证 id、时间等一致）
    const msg = res.data;
    messages.value.push({
      id: msg.id,
      text: newMessage.value, // 本地显示明文
      sender: authStore.user.username,
      timestamp: msg.timestamp,
      encrypted: true
    });
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

const handleLogout = () => {
  ElMessageBox.confirm('Are you sure you want to log out?', 'Confirm Logout', {
    confirmButtonText: 'Yes',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    authStore.logout();
    router.push('/login');
  });
};

const handleAttachFile = () => {
  ElNotification({ title: 'Feature Hint', message: 'File attachment functionality is planned!', type: 'info' });
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
  // 1. 本地加密
  const { encryptedBlob, nonce } = await encryptFile(file, sessionKey);
  // 2. 上传密文
  const uploadRes = await uploadEncryptedFile(encryptedBlob, file.name);
  // 3. 发送文件消息
  await messageService.sendMessage({
    receiverUsername: currentChatTarget.value.username,
    messageType: 'FILE',
    fileUrl: uploadRes.fileUrl,
    originalFilename: file.name,
    encryptedContent: '[File Encrypted]', // 可选，或加密文件描述
    nonce
  });
  ElMessage.success('File encrypted and sent');
}

async function downloadAndDecryptFile(fileUrl, nonce, sessionKey, originalFilename) {
  await sodium.ready;
  // 1. 下载密文
  const response = await fetch(fileUrl);
  const cipherBuffer = await response.arrayBuffer();
  const cipherBytes = new Uint8Array(cipherBuffer);
  // 2. 解密
  const nonceBytes = sodium.from_base64(nonce, sodium.base64_variants.URLSAFE_NO_PADDING);
  const plainBytes = sodium.crypto_aead_chacha20poly1305_ietf_decrypt(
    null,
    cipherBytes,
    null,
    nonceBytes,
    sessionKey
  );
  // 3. 生成 Blob 并下载
  const blob = new Blob([plainBytes]);
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = originalFilename;
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
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('Copied to clipboard!');
  });
}
function downloadAsFile(text) {
  const blob = new Blob([text], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'securechat-private-key.json';
  a.click();
  URL.revokeObjectURL(url);
}

function onFileChange(e) {
  const file = e.target.files[0];
  if (file) {
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error('File size must not exceed 50MB');
      return;
    }
    handleSendFile(file);
  }
  e.target.value = '';
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
</style>

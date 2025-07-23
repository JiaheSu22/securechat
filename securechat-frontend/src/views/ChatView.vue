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
        <div class="chat-header">
          <div class="chat-title">
            <span>{{ currentChatTarget.nickname }}</span>
            <span class="chat-subtitle">@{{ currentChatTarget.username }}</span>
          </div>
        </div>
        <div ref="messageContainerRef" class="message-container">
          <!-- 消息渲染 -->
          <div v-for="msg in messages" :key="msg.id" :class="['message-row', getMessageClass(msg.sender)]">
            <div v-if="msg.sender === 'system'" class="system-message">
              <el-icon v-if="msg.encrypted"><Lock /></el-icon>
              <span :class="{ 'encrypted-text': msg.encrypted }">{{ msg.text }}</span>
            </div>
            <div v-else class="message-bubble">
              <p>{{ msg.text }}</p>
            </div>
          </div>
        </div>
        <div class="message-input-box">
          <el-tooltip effect="dark" content="Send File (coming soon)" placement="top">
            <el-button :icon="Paperclip" @click="handleAttachFile" class="input-action-btn" />
          </el-tooltip>
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
        <div v-if="currentChatTarget && !canSendMessage" class="input-warning">
          <el-icon color="#F56C6C"><Lock /></el-icon>
          <span>对方未上传公钥，无法发送消息</span>
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
import { Promotion, Search, Paperclip, Plus, MoreFilled, Delete, Bell, Lock, CircleClose, SuccessFilled } from '@element-plus/icons-vue';
import { friendshipService } from '@/services/friendshipService';
// 新增：引入 STOMP 客户端
import { Client } from '@stomp/stompjs';
// 新增：引入加密工具
import { encryptMessage } from '@/services/crypto';
// 新增：引入解密工具
import { decryptMessage } from '@/services/crypto';
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
const friendPublicKeys = ref(new Map()); // 新增：缓存 username -> publicKey

// --- 好友管理状态 ---
const addContactDialogVisible = ref(false);
const addFriendUsername = ref("");
const friendRequestsDialogVisible = ref(false);

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
  if (!currentChatTarget.value) return false;
  if (isChatBlocked.value) return false;
  const publicKey = friendPublicKeys.value.get(currentChatTarget.value.username);
  return !!publicKey;
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
      client.subscribe('/user/queue/private', (message) => {
        // 新增：解析消息并解密
        try {
          const msgObj = JSON.parse(message.body);
          // 只处理当前聊天对象的消息
          if (!currentChatTarget.value || msgObj.senderUsername !== currentChatTarget.value.username) {
            // 可选：可做未读消息提醒
            return;
          }
          const privateKey = authStore.privateKey;
          let decrypted = null;
          if (privateKey) {
            decrypted = decryptMessage(privateKey, msgObj.encryptedContent);
          }
          if (decrypted) {
            messages.value.push({
              id: msgObj.id,
              text: decrypted,
              sender: msgObj.senderUsername,
            });
          } else {
            messages.value.push({
              id: msgObj.id,
              text: '[解密失败] 此消息无法读取',
              sender: 'system',
              encrypted: false
            });
          }
          scrollToBottom();
        } catch (e) {
          messages.value.push({
            id: Date.now(),
            text: '[消息处理异常]',
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
        keyMap.set(user.username, user.publicKey);
      }
      friendPublicKeys.value = keyMap;
    } catch (e) {
      contactList.value = [];
      friendPublicKeys.value = new Map();
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
const selectChat = (user) => {
  if (currentChatTarget.value?.id === user.id) return;

  currentChatTarget.value = user;
  messages.value = [];

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

  scrollToBottom();
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
  if (newMessage.value.trim() === '' || !currentChatTarget.value || isChatBlocked.value) return;

  // 查找公钥
  const targetUsername = currentChatTarget.value.username;
  const publicKey = friendPublicKeys.value.get(targetUsername);
  if (!publicKey) {
    ElMessage.error('You cannot send an encrypted message to a user who has not uploaded a public key.');
    return;
  }

  // 加密消息
  const encrypted = encryptMessage(publicKey, newMessage.value);
  if (!encrypted) {
    ElMessage.error('Encryption failed, message not sent.');
    return;
  }

  // 发送到后端
  try {
    const res = await messageService.sendMessage({
      receiverUsername: targetUsername,
      encryptedContent: encrypted,
      messageType: 'TEXT',
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
    const keyMap = new Map();
    for (const user of res.data) {
      keyMap.set(user.username, user.publicKey);
    }
    friendPublicKeys.value = keyMap;
  } catch {
    contactList.value = [];
    blockedUsers.value = new Set();
    friendPublicKeys.value = new Map();
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
</style>

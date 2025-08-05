<!-- ChatView.vue - Modern Chat Interface -->
<template>
  <div :class="['chat-app', { 'dark': isDarkMode }]" class="h-screen w-screen overflow-hidden">
    <!-- Background decoration -->
    <div class="absolute inset-0 bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/20 dark:from-dark-50 dark:via-slate-900 dark:to-purple-900/20"></div>
    <div class="absolute inset-0 bg-gradient-to-tr from-transparent via-blue-50/10 to-purple-50/10 dark:from-transparent dark:via-slate-800/10 dark:to-purple-900/10"></div>
    
    <!-- Main container -->
    <div class="relative z-10 flex h-full">
      <!-- Dark mode toggle button -->
      <button 
        @click="toggleDarkMode"
        class="fixed top-4 right-4 z-50 p-3 rounded-2xl glass-effect shadow-soft hover:shadow-float transition-all duration-200"
      >
        <svg v-if="!isDarkMode" class="w-5 h-5 text-gray-600 hover:text-purple-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path>
        </svg>
        <svg v-else class="w-5 h-5 text-gray-300 hover:text-yellow-400 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path>
        </svg>
      </button>

      <!-- Left sidebar - hideable on mobile -->
      <div :class="['sidebar-container', { 'sidebar-hidden': !sidebarVisible && isMobile }]">
        <ChatSidebar
          :user="authStore.user"
          :contacts="filteredContactList"
          :friend-requests="friendRequests"
          :current-chat-target="currentChatTarget"
          :unread-map="unreadMap"
          :search-query="searchQuery"
          :get-last-message-text="getLastMessageText"
          :get-avatar-color="getAvatarColor"
          :is-dark-mode="isDarkMode"
          @logout="handleLogout"
          @open-add-contact="addContactDialogVisible = true"
          @open-friend-requests="friendRequestsDialogVisible = true"
          @select-chat="selectChat"
          @contact-command="handleContactCommand"
          @update:searchQuery="searchQuery = $event"
          @import-keys="importPrivateKeys"
          @mobile-chat-selected="handleMobileChatSelected"
        />
      </div>

      <!-- Right chat area -->
      <div class="chat-main-container">
        <!-- Mobile header -->
        <div v-if="isMobile" class="mobile-header">
          <button @click="sidebarVisible = true" class="p-2 rounded-xl hover:bg-black/5 dark:hover:bg-white/5 transition-colors text-gray-700 dark:text-gray-300">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
            </svg>
          </button>
          <div v-if="currentChatTarget" class="flex-1 text-center">
            <h2 class="font-semibold text-gray-900 dark:text-gray-100">{{ currentChatTarget.nickname }}</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400">@{{ currentChatTarget.username }}</p>
          </div>
          <div v-else class="flex-1 text-center">
            <h2 class="font-semibold text-gray-900 dark:text-gray-100">Secure Chat</h2>
          </div>
          <div class="w-10"></div> <!-- Spacer for symmetry -->
        </div>

        <ChatWindow
          ref="chatWindowRef"
          :current-chat-target="currentChatTarget"
          :messages="messages"
          :selected-file="selectedFile"
          :is-chat-blocked="isChatBlocked"
          :can-send-message="canSendMessage"
          :get-message-class="getMessageClass"
          :get-download-btn-style="getDownloadBtnStyle"
          :get-avatar-color="getAvatarColor"
          :is-dark-mode="isDarkMode"
          :is-mobile="isMobile"
          v-model:newMessage="newMessage"
          @send-message="handleSend"
          @attach-file="handleAttachFile"
          @remove-attachment="selectedFile = null"
          @file-changed="onFileChange"
          @download-file="handleFileDownload"
        />
      </div>

      <!-- Mobile overlay -->
      <div 
        v-if="isMobile && sidebarVisible" 
        @click="sidebarVisible = false"
        class="fixed inset-0 bg-black/20 z-30 lg:hidden"
      ></div>
    </div>

    <!-- Dialogs -->
    <AddContactDialog
      v-model:visible="addContactDialogVisible"
      :is-dark-mode="isDarkMode"
      @send-friend-request="handleSendFriendRequest"
    />

    <FriendRequestsDialog
      v-model:visible="friendRequestsDialogVisible"
      :requests="friendRequests"
      :get-avatar-color="getAvatarColor"
      :is-dark-mode="isDarkMode"
      @accept-request="handleAcceptRequest"
      @decline-request="handleDeclineRequest"
    />

    <!-- Custom Logout Dialog -->
    <transition name="modal-overlay">
      <div v-if="showLogoutDialog" class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click="closeLogoutDialog">
        <transition name="modal-content">
          <div v-if="showLogoutDialog" class="w-full max-w-md bg-white dark:bg-dark-100 rounded-3xl shadow-float overflow-hidden" @click.stop>
            <!-- Header -->
            <div class="flex items-center p-6 border-b border-gray-100/50 dark:border-dark-200/50">
              <div class="p-3 rounded-2xl bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                </svg>
              </div>
              <div class="flex-1 ml-4">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">Confirm Logout</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Choose your logout option</p>
              </div>
              <button @click="closeLogoutDialog" class="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl transition-colors duration-200">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>

            <!-- Body -->
            <div class="p-6">
              <div class="flex items-center space-x-3 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200/50 dark:border-yellow-800/50 rounded-2xl text-yellow-800 dark:text-yellow-200 mb-6">
                <svg class="w-6 h-6 text-yellow-600 dark:text-yellow-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <p class="text-sm">Make sure to save your private keys before logging out. They cannot be recovered if lost!</p>
              </div>

              <p class="text-gray-600 dark:text-gray-400 text-sm mb-6">
                You are about to logout. Your private keys are stored locally and will be needed to decrypt your messages when you login again.
              </p>
            </div>

            <!-- Footer -->
            <div class="flex items-center justify-end space-x-3 p-6 bg-gray-50 dark:bg-dark-200/50 border-t border-gray-100/50 dark:border-dark-200/50">
              <button 
                @click="handleLogoutOnly" 
                :disabled="isLoggingOut"
                class="flex items-center space-x-2 px-6 py-2.5 bg-gray-500 hover:bg-gray-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-xl font-medium transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed"
                style="box-shadow: var(--shadow-soft);"
              >
                <svg v-if="isLoggingOut" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>Logout Only</span>
              </button>
              <button 
                @click="handleLogoutAndSaveKeys" 
                :disabled="isLoggingOut"
                class="flex items-center space-x-2 px-6 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-xl font-medium transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed"
                style="box-shadow: var(--shadow-soft);"
              >
                <svg v-if="isLoggingOut" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                </svg>
                <span>Logout & Save Keys</span>
              </button>
            </div>
          </div>
        </transition>
      </div>
    </transition>

    <!-- Custom Delete Confirmation Dialog -->
    <transition name="modal-overlay">
      <div v-if="showDeleteDialog" class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click="closeDeleteDialog">
        <transition name="modal-content">
          <div v-if="showDeleteDialog" class="w-full max-w-md bg-white dark:bg-dark-100 rounded-3xl shadow-float overflow-hidden" @click.stop>
            <!-- Header -->
            <div class="flex items-center p-6 border-b border-gray-100/50 dark:border-dark-200/50">
              <div class="p-3 rounded-2xl bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
              </div>
              <div class="flex-1 ml-4">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">Delete Contact</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">This action cannot be undone</p>
              </div>
              <button @click="closeDeleteDialog" class="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl transition-colors duration-200">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>

            <!-- Body -->
            <div class="p-6">
              <div class="flex items-center space-x-3 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200/50 dark:border-red-800/50 rounded-2xl text-red-800 dark:text-red-200 mb-6">
                <svg class="w-6 h-6 text-red-600 dark:text-red-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                </svg>
                <p class="text-sm font-medium">This will permanently delete your conversation history with this contact.</p>
              </div>

              <p class="text-gray-600 dark:text-gray-400 text-sm" v-if="contactToDelete">
                Are you sure you want to delete <strong>{{ contactToDelete.nickname }}</strong>? All your messages and shared files will be permanently removed.
              </p>
            </div>

            <!-- Footer -->
            <div class="flex items-center justify-end space-x-3 p-6 bg-gray-50 dark:bg-dark-200/50 border-t border-gray-100/50 dark:border-dark-200/50">
              <button 
                @click="closeDeleteDialog" 
                :disabled="isDeletingContact"
                class="px-6 py-2.5 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Cancel
              </button>
              <button 
                @click="confirmDeleteContact" 
                :disabled="isDeletingContact"
                class="flex items-center space-x-2 px-6 py-2.5 bg-red-500 hover:bg-red-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-xl font-medium transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed"
                style="box-shadow: var(--shadow-soft);"
              >
                <svg v-if="isDeletingContact" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
                <span>Delete Contact</span>
              </button>
            </div>
          </div>
        </transition>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElNotification, ElMessage } from 'element-plus';
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

// --- UI State Management ---
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true' || false);
const isMobile = ref(window.innerWidth < 1024);
const sidebarVisible = ref(!isMobile.value);

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
          
          // Move the sender to the top of the contact list
          moveContactToTop(msgObj.senderUsername);
          
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

// --- UI Event Handlers ---
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value;
  localStorage.setItem('darkMode', isDarkMode.value.toString());
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
};

const handleResize = () => {
  isMobile.value = window.innerWidth < 1024;
  if (!isMobile.value) {
    sidebarVisible.value = true;
  }
};

const handleMobileChatSelected = () => {
  if (isMobile.value) {
    sidebarVisible.value = false;
  }
};

// --- Lifecycle Hooks ---
onMounted(async () => {
  // Set initial dark mode state
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark');
  }
  
  // Listen for window resize
  window.addEventListener('resize', handleResize);
  
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
    sortContactList(); // Sort once after all data is loaded
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
    
    // Force scroll to bottom after all messages are loaded
    await nextTick();
    scrollToBottom();
  } catch (e) {
    // If the user is blocked, don't show error - they just can't see history
    if (user.status === 'BLOCKED') {
      messages.value.push({ id: 'system-blocked-history', text: 'Previous message history is not available for blocked contacts.', sender: 'system' });
    } else {
      messages.value.push({ id: 'system-history-error', text: '[Failed to load history messages]', sender: 'system' });
    }
    // Still scroll to bottom even if loading failed
    await nextTick();
    scrollToBottom();
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
        
        // Move current chat target to top of contact list
        moveContactToTop(currentChatTarget.value.username);
        
        newMessage.value = '';
        // Ensure scrolling happens after message is added
        nextTick(() => {
          scrollToBottom();
        });
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
    
    // Move current chat target to top of contact list
    moveContactToTop(currentChatTarget.value.username);
    
    selectedFile.value = null;
    newMessage.value = '';
    // Ensure scrolling happens after message is added
    nextTick(() => {
      scrollToBottom();
    });
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

// Dialog states
const showLogoutDialog = ref(false);
const isLoggingOut = ref(false);
const showDeleteDialog = ref(false);
const isDeletingContact = ref(false);
const contactToDelete = ref(null);

const handleLogout = () => {
  showLogoutDialog.value = true;
};

const closeLogoutDialog = () => {
  showLogoutDialog.value = false;
  isLoggingOut.value = false;
};

const handleLogoutOnly = async () => {
  isLoggingOut.value = true;
  try {
    const logoutSuccess = await authStore.logout(false); // Skip confirmation dialog
    if (logoutSuccess) {
      router.push('/login');
    }
  } catch (error) {
    ElMessage.error('Logout failed');
  } finally {
    isLoggingOut.value = false;
    showLogoutDialog.value = false;
  }
};

const handleLogoutAndSaveKeys = async () => {
  // First export and download the keys
  try {
    const x25519 = localStorage.getItem('x25519PrivateKey');
    const ed25519 = localStorage.getItem('ed25519PrivateKey');
    const exportObj = { x25519PrivateKey: x25519, ed25519PrivateKey: ed25519 };
    
    // Create download
    const blob = new Blob([JSON.stringify(exportObj, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${authStore.user?.username || 'user'}-private-keys.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    ElMessage.success('Private keys downloaded! Logging out...');
    
    // Small delay to ensure download completes
    setTimeout(async () => {
      await handleLogoutOnly();
    }, 1000);
    
  } catch (error) {
    ElMessage.error('Failed to save keys');
    isLoggingOut.value = false;
  }
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

const handleDeleteContact = (contact) => {
  contactToDelete.value = contact;
  showDeleteDialog.value = true;
};

const closeDeleteDialog = () => {
  showDeleteDialog.value = false;
  isDeletingContact.value = false;
  contactToDelete.value = null;
};

const confirmDeleteContact = async () => {
  if (!contactToDelete.value) return;
  
  isDeletingContact.value = true;
  try {
    await friendshipService.unfriend(contactToDelete.value.username);
    await refreshFriendData();
    if (currentChatTarget.value?.id === contactToDelete.value.id) {
      currentChatTarget.value = null;
      messages.value = [];
    }
    ElMessage({ type: 'success', message: `${contactToDelete.value.nickname} has been deleted.` });
    closeDeleteDialog();
  } catch (error) {
    ElMessage.error('Failed to delete contact');
    isDeletingContact.value = false;
  }
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

function importPrivateKeys(keysObj) {
  try {
    // Check if the input is already an object or needs parsing
    let obj;
    if (typeof keysObj === 'string') {
      obj = JSON.parse(keysObj);
    } else {
      obj = keysObj;
    }
    
    if (obj.x25519PrivateKey && obj.ed25519PrivateKey) {
      // It's better to delegate state updates to the store
      authStore.setX25519PrivateKey(obj.x25519PrivateKey);
      authStore.setEd25519PrivateKey(obj.ed25519PrivateKey);
      
      ElMessage.success('Private keys imported successfully! Page will reload in 3 seconds to decrypt messages...');
      setTimeout(() => window.location.reload(), 3000);
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
const moveContactToTop = (senderUsername) => {
  const contactIndex = contactList.value.findIndex(contact => contact.username === senderUsername);
  if (contactIndex > 0) { // Only move if not already at top (index > 0)
    const contact = contactList.value[contactIndex];
    
    // Don't move blocked users to top
    if (contact.status === 'BLOCKED') {
      return;
    }
    
    // Remove from current position and add to beginning
    contactList.value.splice(contactIndex, 1);
    contactList.value.unshift(contact);
  }
};

const sortContactList = () => {
  contactList.value.sort((a, b) => {
    // Always keep blocked users at bottom
    if (a.status === 'BLOCKED' && b.status !== 'BLOCKED') return 1;
    if (b.status === 'BLOCKED' && a.status !== 'BLOCKED') return -1;
    if (a.status === 'BLOCKED' && b.status === 'BLOCKED') return 0;
    
    // Sort by last message timestamp (most recent first)
    const aLastMsg = lastMessageMap.value[a.username];
    const bLastMsg = lastMessageMap.value[b.username];
    
    if (!aLastMsg && !bLastMsg) return 0;
    if (!aLastMsg) return 1;
    if (!bLastMsg) return -1;
    
    return new Date(bLastMsg.timestamp) - new Date(aLastMsg.timestamp);
  });
};

const scrollToBottom = () => {
    if(chatWindowRef.value) {
        chatWindowRef.value.scrollToBottom();
    }
};

</script>

<style scoped>
/* Main app styles */
.chat-app {
  @apply font-sans antialiased;
}

/* Sidebar container */
.sidebar-container {
  @apply w-80 lg:w-96 flex-shrink-0 transition-all duration-300 ease-in-out z-40;
}

.sidebar-container.sidebar-hidden {
  @apply -translate-x-full lg:translate-x-0;
  position: fixed;
  height: 100vh;
}

@media (min-width: 1024px) {
  .sidebar-container.sidebar-hidden {
    @apply translate-x-0;
    position: relative;
  }
}

/* Chat main container */
.chat-main-container {
  @apply flex-1 flex flex-col min-w-0;
  height: 100vh;
}

@media (max-width: 1023px) {
  .chat-main-container {
    height: 100vh;
    overflow: hidden; /* Prevent scrolling on the container */
  }
}

/* Mobile header */
.mobile-header {
  @apply flex items-center p-4 bg-white/80 dark:bg-dark-100/80 backdrop-blur-xl border-b border-gray-200/50 dark:border-dark-200/50 lg:hidden;
  min-height: 80px; /* Ensure consistent height */
  flex-shrink: 0; /* Prevent header from shrinking */
}

/* Responsive design */
@media (max-width: 1023px) {
  .sidebar-container {
    @apply absolute inset-y-0 left-0 w-80 bg-white dark:bg-dark-50 shadow-2xl;
  }
  
  .sidebar-container.sidebar-hidden {
    @apply -translate-x-full;
  }
  
  .chat-main-container {
    @apply w-full;
  }
}

/* Modal transitions */
.modal-overlay-enter-active,
.modal-overlay-leave-active {
  transition: all 0.3s ease;
}

.modal-overlay-enter-from,
.modal-overlay-leave-to {
  opacity: 0;
  backdrop-filter: blur(0px);
}

.modal-content-enter-active,
.modal-content-leave-active {
  transition: all 0.25s ease-out;
}

.modal-content-enter-from,
.modal-content-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(-20px);
}
</style>
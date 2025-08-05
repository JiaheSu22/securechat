<!-- ChatWindow.vue - Modern Chat Interface -->
<template>
  <div class="chat-window">
    <template v-if="currentChatTarget">
      <!-- Chat header (desktop only) -->
      <div v-if="!isMobile" class="chat-header">
        <div class="header-user-info">
          <div 
            :style="{ backgroundColor: getAvatarColor?.(currentChatTarget) || '#6366f1' }" 
            class="header-avatar"
          >
            <span>{{ currentChatTarget.nickname[0].toUpperCase() }}</span>
          </div>
          <div class="header-details">
            <h3 class="header-name">{{ currentChatTarget.nickname }}</h3>
            <p class="header-status">
              @{{ currentChatTarget.username }}
            </p>
          </div>
        </div>
      </div>

      <!-- Messages container -->
      <div ref="messageContainerRef" class="messages-container">
        <div class="messages-list">
          <div 
            v-for="(msg, index) in messages" 
            :key="msg.id" 
            :class="['message-wrapper', getMessageClass(msg.sender)]"
          >
          <!-- System messages -->
          <div v-if="msg.sender === 'system'" class="system-message">
              <div class="system-icon">
                <svg v-if="msg.encrypted" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
              <span class="system-text">{{ msg.text }}</span>
          </div>

          <!-- User messages -->
            <div v-else class="message-bubble-container">
              <div :class="['message-bubble', getMessageClass(msg.sender)]">
                <!-- File messages -->
                <div v-if="msg.messageType === 'FILE'" class="file-message">
                  <div class="file-content">
                    <div :class="['file-icon', getMessageClass(msg.sender)]">
                      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                      </svg>
                    </div>
                    <div class="file-details">
                      <div class="file-name">{{ msg.originalFilename }}</div>
                  <div v-if="msg.text && msg.text !== '[File Encrypted]'" class="file-description">
                    {{ msg.text }}
                      </div>
                    </div>
                  </div>
                  <button 
                    @click="$emit('download-file', msg)"
                    class="download-btn"
                    title="Download file"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                  </button>
                </div>
                
                <!-- Text messages -->
                <div v-else class="text-message">
                  {{ msg.text }}
                </div>
                
                <!-- Message time -->
                <div class="message-time">
                  {{ formatTime(msg.timestamp) }}
                </div>
              </div>
            </div>
          </div>
          <!-- Auto-scroll anchor at the end -->
          <div ref="scrollAnchor" class="scroll-anchor"></div>
        </div>
      </div>

      <!-- Input area -->
      <div class="input-area">
        <!-- File attachment preview -->
        <div v-if="selectedFile" class="file-preview">
          <div class="file-preview-content">
            <div class="file-preview-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"></path>
              </svg>
            </div>
            <span class="file-preview-name">{{ selectedFile.name }}</span>
            <button 
              @click="$emit('remove-attachment')"
              class="file-preview-remove"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- Input box -->
        <div class="input-container">
        <input type="file" ref="fileInput" style="display:none" @change="onFileChange" />
          
          <button 
            @click="$emit('attach-file')" 
            class="attach-btn"
            title="Send file"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"></path>
            </svg>
          </button>

          <div class="text-input-wrapper">
            <textarea
              :value="newMessage"
              @input="$emit('update:newMessage', $event.target.value)"
              @keydown="handleKeyDown"
              :disabled="isChatBlocked || !canSendMessage"
              :placeholder="isChatBlocked ? 'User is blocked' : 'Type a message...'"
              class="message-input"
              rows="1"
              ref="textareaRef"
            ></textarea>
          </div>

          <button 
            @click="handleSend"
            :disabled="isChatBlocked || !canSendMessage || (!newMessage.trim() && !selectedFile)"
            class="send-btn"
            title="Send message"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
            </svg>
          </button>
        </div>
      </div>
    </template>
    
    <!-- Welcome screen -->
    <template v-else>
      <div class="welcome-screen">
        <div class="welcome-content">
          <div class="welcome-icon">
            <svg class="w-20 h-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
            </svg>
          </div>
          <h2 class="welcome-title">Welcome to Secure Chat</h2>
          <p class="welcome-subtitle">Select a contact from the left to start a secure end-to-end encrypted conversation</p>
          <div class="welcome-features">
            <div class="feature-item">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
              </svg>
              <span>End-to-end encryption</span>
            </div>
            <div class="feature-item">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
              <span>File transfer</span>
            </div>
            <div class="feature-item">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
              <span>Real-time messaging</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, onMounted } from 'vue';

const props = defineProps({
  currentChatTarget: Object,
  messages: Array,
  newMessage: String,
  selectedFile: Object,
  isChatBlocked: Boolean,
  canSendMessage: Boolean,
  getMessageClass: Function,
  getDownloadBtnStyle: Function,
  isDarkMode: Boolean,
  isMobile: Boolean,
  getAvatarColor: Function
});

const emit = defineEmits([
  'send-message',
  'attach-file',
  'remove-attachment',
  'update:newMessage',
  'file-changed',
  'download-file'
]);

// Component refs
const messageContainerRef = ref(null);
const fileInput = ref(null);
const textareaRef = ref(null);
const scrollAnchor = ref(null);

// Format time function
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now - date;
  const diffHours = diffMs / (1000 * 60 * 60);
  
  if (diffHours < 24) {
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false 
    });
  } else if (diffHours < 24 * 7) {
    const days = Math.floor(diffHours / 24);
    return `${days} days ago`;
  } else {
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric' 
    });
  }
};

// Auto adjust textarea height
const adjustTextareaHeight = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto';
    const maxHeight = 120; // Maximum height
    const scrollHeight = textareaRef.value.scrollHeight;
    textareaRef.value.style.height = Math.min(scrollHeight, maxHeight) + 'px';
  }
};

// Keyboard event handling
const handleKeyDown = (event) => {
  if (props.isChatBlocked) return;
  
  // Auto adjust height
  nextTick(() => adjustTextareaHeight());
  
  // Enter key sends message
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    if (props.newMessage.trim() || props.selectedFile) {
    handleSend();
    }
  }
};

// Send message
const handleSend = () => {
  if (props.isChatBlocked || !props.canSendMessage) return;
  if (!props.newMessage.trim() && !props.selectedFile) return;
  
    emit('send-message');
  
  // Reset textarea height
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto';
    }
  });
};

// File selection handling
const onFileChange = (e) => {
  const file = e.target.files[0];
  emit('file-changed', file);
  e.target.value = ''; // Reset input
};

// Scroll to bottom - use scroll anchor method with fallback
const scrollToBottom = () => {
  // Method 1: Try scroll anchor
  if (scrollAnchor.value) {
    scrollAnchor.value.scrollIntoView({ 
      behavior: 'instant',
      block: 'end'
    });
  }
  
  // Method 2: Fallback to manual scroll
  if (messageContainerRef.value) {
    const container = messageContainerRef.value;
    container.scrollTop = container.scrollHeight;
  }
};

// Smooth scroll to bottom (for new messages)
const smoothScrollToBottom = () => {
  if (scrollAnchor.value) {
    scrollAnchor.value.scrollIntoView({ 
      behavior: 'smooth',
      block: 'end'
    });
  }
};

// Listen for message changes, auto scroll
watch(() => props.messages, (newMessages, oldMessages) => {
  // Always scroll to bottom when messages change
  if (newMessages && newMessages.length > 0) {
    nextTick(() => {
      scrollToBottom();
    });
  }
}, { deep: true, immediate: true });

// Listen for new message input, auto adjust height
watch(() => props.newMessage, () => {
  nextTick(() => adjustTextareaHeight());
});

// Listen for chat target changes to scroll to bottom
watch(() => props.currentChatTarget, (newTarget) => {
  if (newTarget) {
    // Scroll to bottom when switching to a different chat
    nextTick(() => {
      scrollToBottom();
    });
  }
}, { immediate: false });

// File input trigger
const triggerFileInput = () => {
  if (fileInput.value) fileInput.value.click();
};

// Scroll to bottom after component mount
onMounted(() => {
  nextTick(() => {
    scrollToBottom();
  });
});

// Expose methods to parent component
defineExpose({
  scrollToBottom,
  triggerFileInput,
});
</script>

<style scoped>
/* Chat window main container */
.chat-window {
  @apply flex-1 flex flex-col h-full bg-transparent;
}

/* Chat header */
.chat-header {
  @apply flex items-center justify-between p-6 bg-white/80 dark:bg-dark-100/80 backdrop-blur-xl border-b border-gray-200/50 dark:border-dark-200/50;
}

.header-user-info {
  @apply flex items-center space-x-4;
}

.header-avatar {
  @apply w-10 h-10 rounded-2xl flex items-center justify-center text-white font-semibold;
  box-shadow: var(--shadow-soft);
}

.header-details {
  @apply flex flex-col;
}

.header-name {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100;
}

.header-status {
  @apply flex items-center text-sm text-gray-500 dark:text-gray-400;
}

.status-indicator {
  @apply w-2 h-2 bg-green-500 rounded-full mr-2;
}

.header-actions {
  @apply flex items-center space-x-2;
}

.header-action-btn {
  @apply p-2.5 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl transition-all duration-200;
}

/* Messages container */
.messages-container {
  @apply flex-1 overflow-hidden;
  background: linear-gradient(145deg, rgba(248,250,252,0.5), rgba(241,245,249,0.3));
}

.dark .messages-container {
  background: linear-gradient(145deg, rgba(15,23,42,0.3), rgba(30,41,59,0.2));
}

.messages-list {
  @apply h-full overflow-y-auto px-6 py-4;
  scroll-behavior: auto;
  /* Ensure the scroll container works properly */
  display: flex;
  flex-direction: column;
  gap: 1rem; /* Replace space-y-4 with gap */
}

.scroll-anchor {
  height: 1px;
  margin-top: auto;
  flex-shrink: 0;
}

/* Message wrapper */
.message-wrapper {
  @apply flex max-w-full;
}

.message-wrapper.sent {
  @apply justify-end;
}

.message-wrapper.received {
  @apply justify-start;
}

.message-wrapper.system {
  @apply justify-center;
}

/* System messages */
.system-message {
  @apply flex items-center space-x-2 bg-gray-100/60 dark:bg-dark-200/60 text-gray-600 dark:text-gray-400 px-4 py-2 rounded-2xl text-sm backdrop-blur-sm border border-gray-200/50 dark:border-dark-300/50;
}

.system-icon {
  @apply flex-shrink-0 text-blue-500;
}

.system-text {
  @apply font-medium;
}

/* Message bubble container */
.message-bubble-container {
  @apply max-w-md lg:max-w-lg;
}

/* Message bubble */
.message-bubble {
  @apply relative p-4 rounded-3xl backdrop-blur-sm border transition-all duration-200;
  box-shadow: var(--shadow-soft);
}

.message-bubble:hover {
  box-shadow: var(--shadow-float);
}

.message-bubble.sent {
  @apply bg-gradient-to-r from-blue-500 to-blue-600 text-white border-blue-500/20 rounded-br-lg;
}

.message-bubble.received {
  @apply bg-white/90 dark:bg-dark-100/90 text-gray-800 dark:text-gray-200 border-gray-200/50 dark:border-dark-200/50 rounded-bl-lg;
}

/* Text messages */
.text-message {
  @apply whitespace-pre-wrap break-words leading-relaxed;
}

/* File messages */
.file-message {
  @apply flex items-center justify-between space-x-3;
}

.file-content {
  @apply flex items-center space-x-3 flex-1 min-w-0;
}

.file-icon {
  @apply flex-shrink-0 p-2 bg-blue-500/10 text-blue-500 rounded-xl;
}

.file-icon.sent {
  @apply bg-white/20 text-white;
}

.file-icon.received {
  @apply bg-blue-500/10 text-blue-500;
}

.file-details {
  @apply flex-1 min-w-0;
}

.file-name {
  @apply font-medium text-sm truncate;
}

.file-description {
  @apply text-xs opacity-75 mt-1;
}

.download-btn {
  @apply p-2 hover:bg-black/5 dark:hover:bg-white/5 rounded-lg transition-colors duration-200;
}

/* Message time */
.message-time {
  @apply text-xs opacity-60 mt-2 text-right;
}

.message-bubble.received .message-time {
  @apply text-left;
}

/* Input area */
.input-area {
  @apply p-6 bg-white/80 dark:bg-dark-100/80 backdrop-blur-xl border-t border-gray-200/50 dark:border-dark-200/50;
}

/* File preview */
.file-preview {
  @apply mb-4;
}

.file-preview-content {
  @apply flex items-center space-x-3 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-2xl border border-blue-200/50 dark:border-blue-800/50;
}

.file-preview-icon {
  @apply flex-shrink-0 p-2 bg-blue-500 text-white rounded-lg;
}

.file-preview-name {
  @apply flex-1 text-sm font-medium text-blue-800 dark:text-blue-200 truncate;
}

.file-preview-remove {
  @apply p-1 text-blue-600 dark:text-blue-400 hover:text-red-500 dark:hover:text-red-400 rounded-lg transition-colors duration-200;
}

/* Input container */
.input-container {
  @apply flex items-end space-x-3;
}

.attach-btn {
  @apply p-3 text-gray-500 dark:text-gray-400 hover:text-blue-500 dark:hover:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-2xl transition-all duration-200;
}

.text-input-wrapper {
  @apply flex-1 relative;
}

.message-input {
  @apply w-full p-4 bg-gray-50/80 dark:bg-dark-200/80 border border-gray-200/50 dark:border-dark-300/50 rounded-2xl resize-none focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500/50 transition-all duration-200 backdrop-blur-sm text-gray-900 dark:text-gray-100;
  min-height: 52px;
  max-height: 120px;
}

.message-input::placeholder {
  @apply text-gray-500 dark:text-gray-400;
}

.message-input:disabled {
  @apply bg-gray-100 dark:bg-dark-300 text-gray-400 dark:text-gray-500 cursor-not-allowed;
}

.send-btn {
  @apply p-3 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-2xl transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed;
  box-shadow: var(--shadow-soft);
}

.send-btn:hover:not(:disabled) {
  box-shadow: var(--shadow-float);
}

/* Welcome screen */
.welcome-screen {
  @apply flex-1 flex items-center justify-center p-8;
}

.welcome-content {
  @apply text-center max-w-md mx-auto;
}

.welcome-icon {
  @apply text-gray-300 dark:text-gray-600 mb-6;
}

.welcome-title {
  @apply text-2xl lg:text-3xl font-bold text-gray-800 dark:text-gray-200 mb-4;
}

.welcome-subtitle {
  @apply text-gray-600 dark:text-gray-400 mb-8 leading-relaxed;
}

.welcome-features {
  @apply space-y-4;
}

.feature-item {
  @apply flex items-center space-x-3 text-gray-500 dark:text-gray-400;
}

.feature-item svg {
  @apply text-blue-500;
}

/* Scrollbar styles */
.messages-list::-webkit-scrollbar {
  @apply w-2;
}

.messages-list::-webkit-scrollbar-track {
  @apply bg-transparent;
}

.messages-list::-webkit-scrollbar-thumb {
  @apply bg-gray-300 dark:bg-dark-300 rounded-full;
}

.messages-list::-webkit-scrollbar-thumb:hover {
  @apply bg-gray-400 dark:bg-dark-400;
}



/* Mobile responsive */
@media (max-width: 1023px) {
  .chat-window {
    height: calc(100vh - 80px); /* Subtract mobile header height */
    max-height: calc(100vh - 80px);
    overflow: hidden;
  }
  
  .messages-container {
    flex: 1;
    min-height: 0; /* Important for flex child overflow */
    overflow: hidden;
  }
  
  .messages-list {
    height: 100%;
    overflow-y: auto;
  }
}

@media (max-width: 768px) {
  .messages-list {
    @apply px-4 py-3;
  }
  
  .input-area {
    @apply p-4;
    flex-shrink: 0; /* Prevent input area from shrinking */
  }
  
  .message-bubble-container {
    @apply max-w-xs;
  }
  
  .input-container {
    @apply space-x-2;
  }
  
  .message-input {
    @apply p-3 text-sm;
  }
  
  .attach-btn, .send-btn {
    @apply p-2.5;
  }
}
</style>
<!-- FriendRequestsDialog.vue - Modern Friend Requests Dialog -->
<template>
  <!-- Overlay -->
  <transition name="modal-overlay">
    <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
      <!-- Modal -->
      <transition name="modal-content">
        <div v-if="visible" class="modal-container" @click.stop>
          <div class="modal-header">
            <div class="header-icon">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20a3 3 0 01-3-3v-2a3 3 0 013-3h3a3 3 0 013 3v2a3 3 0 01-3 3H7zM8 9a3 3 0 116 0 3 3 0 01-6 0z"></path>
              </svg>
            </div>
            <div class="header-text">
              <h3 class="modal-title">Friend Requests</h3>
              <p class="modal-subtitle">
                {{ requests.length > 0 ? `${requests.length} pending request${requests.length > 1 ? 's' : ''}` : 'No pending requests' }}
              </p>
            </div>
            <button @click="$emit('update:visible', false)" class="close-btn">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <!-- Empty state -->
            <div v-if="!requests.length" class="empty-state">
              <div class="empty-icon">
                <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20a3 3 0 01-3-3v-2a3 3 0 013-3h3a3 3 0 013 3v2a3 3 0 01-3 3H7zM8 9a3 3 0 116 0 3 3 0 01-6 0z"></path>
                </svg>
              </div>
              <h4 class="empty-title">No friend requests</h4>
              <p class="empty-description">When someone sends you a friend request, it will appear here.</p>
            </div>

            <!-- Requests list -->
            <div v-else class="requests-list">
              <div 
                v-for="req in requests" 
                :key="req.id" 
                class="request-item"
              >
                <div class="request-avatar">
                  <div 
                    :style="{ backgroundColor: getAvatarColor(req) }" 
                    class="avatar"
                  >
                    <span>{{ req.nickname[0].toUpperCase() }}</span>
                  </div>
                </div>
                
                <div class="request-info">
                  <h5 class="request-name">{{ req.nickname }}</h5>
                  <p class="request-username">@{{ req.username }}</p>
                </div>
                
                <div class="request-actions">
                  <button 
                    @click="$emit('accept-request', req)"
                    :disabled="processingRequests.has(req.id)"
                    class="accept-btn"
                  >
                    <svg v-if="!processingRequests.has(req.id)" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    <svg v-else class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  </button>
                  
                  <button 
                    @click="$emit('decline-request', req)"
                    :disabled="processingRequests.has(req.id)"
                    class="decline-btn"
                  >
                    <svg v-if="!processingRequests.has(req.id)" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                    <svg v-else class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="requests.length > 0" class="modal-footer">
            <button 
              @click="$emit('update:visible', false)" 
              class="close-btn-footer"
            >
              Close
            </button>
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  visible: Boolean,
  requests: Array,
  getAvatarColor: Function,
  isDarkMode: Boolean
});

const emit = defineEmits(['update:visible', 'accept-request', 'decline-request']);

// 处理中的请求集合
const processingRequests = ref(new Set());

// 处理遮罩层点击
const handleOverlayClick = () => {
  emit('update:visible', false);
};

// 处理接受请求（添加loading状态）
const handleAcceptRequest = async (req) => {
  processingRequests.value.add(req.id);
  try {
    await emit('accept-request', req);
  } finally {
    processingRequests.value.delete(req.id);
  }
};

// 处理拒绝请求（添加loading状态）  
const handleDeclineRequest = async (req) => {
  processingRequests.value.add(req.id);
  try {
    await emit('decline-request', req);
  } finally {
    processingRequests.value.delete(req.id);
  }
};
</script>

<style scoped>
/* 模态框遮罩层 */
.modal-overlay {
  @apply fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4;
}

/* 模态框容器 */
.modal-container {
  @apply w-full max-w-lg bg-white dark:bg-dark-100 rounded-3xl overflow-hidden;
  max-height: 80vh;
  box-shadow: var(--shadow-float);
}

/* 模态框头部 */
.modal-header {
  @apply flex items-center p-6 border-b border-gray-100 dark:border-dark-200;
}

.header-icon {
  @apply flex-shrink-0 w-12 h-12 bg-green-500 text-white rounded-2xl flex items-center justify-center mr-4;
}

.header-text {
  @apply flex-1;
}

.modal-title {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100;
}

.modal-subtitle {
  @apply text-sm text-gray-500 dark:text-gray-400 mt-1;
}

.close-btn {
  @apply p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl transition-all duration-200;
}

/* 模态框主体 */
.modal-body {
  @apply p-6 max-h-96 overflow-y-auto;
}

/* 空状态 */
.empty-state {
  @apply text-center py-8;
}

.empty-icon {
  @apply text-gray-300 dark:text-gray-600 mb-4 flex justify-center;
}

.empty-title {
  @apply text-lg font-semibold text-gray-800 dark:text-gray-200 mb-2;
}

.empty-description {
  @apply text-gray-500 dark:text-gray-400 text-sm leading-relaxed;
}

/* 请求列表 */
.requests-list {
  @apply space-y-3;
}

.request-item {
  @apply flex items-center p-4 bg-gray-50 dark:bg-dark-200/50 rounded-2xl transition-all duration-200 hover:bg-gray-100 dark:hover:bg-dark-200/70;
}

.request-avatar {
  @apply flex-shrink-0 mr-4;
}

.avatar {
  @apply w-12 h-12 rounded-2xl flex items-center justify-center text-white font-semibold;
  box-shadow: var(--shadow-soft);
}

.request-info {
  @apply flex-1 min-w-0;
}

.request-name {
  @apply font-semibold text-gray-900 dark:text-gray-100 truncate;
}

.request-username {
  @apply text-sm text-gray-500 dark:text-gray-400 truncate;
}

.request-actions {
  @apply flex items-center space-x-2;
}

.accept-btn {
  @apply p-2.5 bg-green-500 hover:bg-green-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-xl transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed;
}

.decline-btn {
  @apply p-2.5 bg-red-500 hover:bg-red-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-xl transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed;
}

/* 模态框底部 */
.modal-footer {
  @apply flex items-center justify-center p-6 bg-gray-50 dark:bg-dark-200/50 border-t border-gray-100 dark:border-dark-200;
}

.close-btn-footer {
  @apply px-6 py-2.5 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl font-medium transition-all duration-200;
}

/* 过渡动画 */
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

/* 滚动条样式 */
.modal-body::-webkit-scrollbar {
  @apply w-2;
}

.modal-body::-webkit-scrollbar-track {
  @apply bg-transparent;
}

.modal-body::-webkit-scrollbar-thumb {
  @apply bg-gray-300 dark:bg-dark-300 rounded-full;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  @apply bg-gray-400 dark:bg-dark-400;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .modal-container {
    @apply mx-4 w-auto max-h-full;
  }
  
  .modal-header {
    @apply p-4;
  }
  
  .modal-body {
    @apply p-4 max-h-80;
  }
  
  .modal-footer {
    @apply p-4;
  }
  
  .request-item {
    @apply p-3;
  }
  
  .avatar {
    @apply w-10 h-10;
  }
}
</style>
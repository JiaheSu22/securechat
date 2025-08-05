<!-- AddContactDialog.vue - Modern Add Contact Dialog -->
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
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
              </svg>
            </div>
            <div class="header-text">
              <h3 class="modal-title">Add New Contact</h3>
              <p class="modal-subtitle">Enter username to send friend request</p>
            </div>
            <button @click="$emit('update:visible', false)" class="close-btn">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <div class="input-group">
              <label class="input-label">Username</label>
              <div class="input-container">
                <div class="input-icon">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                  </svg>
                </div>
                <input
                  v-model="addFriendUsername"
                  type="text"
                  placeholder="Enter username..."
                  class="text-input"
                  @keydown.enter="handleSend"
                  ref="usernameInput"
                />
                <button
                  v-if="addFriendUsername"
                  @click="addFriendUsername = ''"
                  class="clear-btn"
                  type="button"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                </button>
              </div>
              <p class="input-hint">Enter the complete username</p>
            </div>
          </div>

          <div class="modal-footer">
            <button
              @click="$emit('update:visible', false)"
              class="cancel-btn"
            >
              Cancel
            </button>
            <button
              @click="handleSend"
              :disabled="!addFriendUsername.trim()"
              class="submit-btn"
            >
              <span v-if="!isLoading">Send Request</span>
              <div v-else class="loading-spinner">
                <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>Sending...</span>
              </div>
            </button>
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';

const props = defineProps({
  visible: Boolean,
  isDarkMode: Boolean
});

const emit = defineEmits(['update:visible', 'send-friend-request']);

// Internal state
const addFriendUsername = ref('');
const isLoading = ref(false);
const usernameInput = ref(null);

// Handle overlay click
const handleOverlayClick = () => {
  emit('update:visible', false);
};

// Send friend request
const handleSend = async () => {
  if (!addFriendUsername.value.trim() || isLoading.value) return;

  isLoading.value = true;
  try {
    await emit('send-friend-request', addFriendUsername.value.trim());
    addFriendUsername.value = '';
    emit('update:visible', false);
  } catch (error) {
    // Error handling is done by parent component
  } finally {
    isLoading.value = false;
  }
};

// Listen for dialog visibility state, auto focus input
watch(() => props.visible, (newVisible) => {
  if (newVisible) {
    nextTick(() => {
      if (usernameInput.value) {
        usernameInput.value.focus();
      }
    });
  } else {
    // Reset state when dialog closes
    isLoading.value = false;
    addFriendUsername.value = '';
  }
});
</script>

<style scoped>
/* Modal overlay */
.modal-overlay {
  @apply fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4;
}

/* Modal container */
.modal-container {
  @apply w-full max-w-md bg-white dark:bg-dark-100 rounded-3xl overflow-hidden;
  transform: scale(1);
  box-shadow: var(--shadow-float);
}

/* 模态框头部 */
.modal-header {
  @apply flex items-center p-6 border-b border-gray-100 dark:border-dark-200;
}

.header-icon {
  @apply flex-shrink-0 w-12 h-12 bg-blue-500 text-white rounded-2xl flex items-center justify-center mr-4;
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
  @apply p-6;
}

.input-group {
  @apply space-y-3;
}

.input-label {
  @apply block text-sm font-medium text-gray-700 dark:text-gray-300;
}

.input-container {
  @apply relative;
}

.input-icon {
  @apply absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none;
}

.text-input {
  @apply w-full pl-10 pr-10 py-3 bg-gray-50 dark:bg-dark-200 border border-gray-200 dark:border-dark-300 rounded-2xl text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500/50 transition-all duration-200;
}

.clear-btn {
  @apply absolute right-3 top-1/2 transform -translate-y-1/2 p-1 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 rounded-lg transition-colors duration-200;
}

.input-hint {
  @apply text-xs text-gray-500 dark:text-gray-400;
}

/* 模态框底部 */
.modal-footer {
  @apply flex items-center justify-end space-x-3 p-6 bg-gray-50 dark:bg-dark-200/50;
}

.cancel-btn {
  @apply px-6 py-2.5 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl font-medium transition-all duration-200;
}

.submit-btn {
  @apply px-6 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 dark:disabled:bg-dark-300 text-white disabled:text-gray-500 rounded-xl font-medium transition-all duration-200 transform hover:scale-105 active:scale-95 disabled:transform-none disabled:cursor-not-allowed;
  box-shadow: var(--shadow-soft);
}

.submit-btn:hover:not(:disabled) {
  box-shadow: var(--shadow-float);
}

.submit-btn:disabled {
  box-shadow: none;
}

.loading-spinner {
  @apply flex items-center space-x-2;
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

/* 移动端适配 */
@media (max-width: 640px) {
  .modal-container {
    @apply mx-4 w-auto;
  }

  .modal-header {
    @apply p-4;
  }

  .modal-body {
    @apply p-4;
  }

  .modal-footer {
    @apply p-4;
  }
}
</style>

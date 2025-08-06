<!-- ChatSidebar.vue - Modern Sidebar Design -->
<template>
  <div class="modern-sidebar">
    <!-- 用户信息头部 -->
    <div v-if="user" class="user-section">
      <div class="user-card">
        <div class="avatar-container">
                     <div
             :style="{ backgroundColor: getAvatarColor(user) }"
             class="user-avatar"
           >
             <span class="avatar-text">{{ user.nickname[0].toUpperCase() }}</span>
           </div>
        </div>
        <div class="user-details">
          <h3 class="user-name">{{ user.nickname }}</h3>
          <p class="user-handle">@{{ user.username }}</p>
        </div>
        <button
          @click="$emit('logout')"
          class="logout-button"
          title="Logout"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
          </svg>
        </button>
      </div>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="action-bar">
      <div class="search-container">
        <div class="search-input-wrapper">
          <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <input
            v-model="internalSearchQuery"
            placeholder="Search contacts..."
            class="search-input"
          />
        </div>
      </div>
      <button
        @click="$emit('open-add-contact')"
        class="add-contact-btn"
        title="Add Contact"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
        </svg>
      </button>
    </div>

    <!-- 好友请求区域 -->
    <div
      v-if="friendRequests.length > 0"
      class="friend-requests-card"
      @click="$emit('open-friend-requests')"
    >
      <div class="request-icon">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-5 5-5-5h5v-4a6 6 0 00-12 0v4z"></path>
        </svg>
      </div>
      <span class="request-text">Friend Requests</span>
      <div class="request-badge">{{ friendRequests.length }}</div>
    </div>

    <!-- Contacts list -->
    <div class="contacts-section">
      <div class="section-header">
        <h4>Contacts</h4>
        <span class="contact-count">{{ contacts.length }}</span>
      </div>

      <div class="contacts-list">
        <div
          v-for="contact in contacts"
          :key="contact.id"
          :class="['contact-card', { 'active': currentChatTarget?.id === contact.id }]"
          @click="handleContactClick(contact)"
        >
          <div class="contact-avatar-container">
            <div
              :style="{ backgroundColor: getAvatarColor(contact) }"
              class="contact-avatar"
            >
              <span>{{ contact.nickname[0].toUpperCase() }}</span>
            </div>
            <div v-if="contact.status === 'BLOCKED'" class="blocked-indicator">
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.368zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z" clip-rule="evenodd"></path>
              </svg>
            </div>
          </div>

          <div class="contact-content">
            <div class="contact-info">
              <h5 class="contact-name">{{ contact.nickname }}</h5>
              <p class="contact-preview">{{ getLastMessageText(contact.username) }}</p>
            </div>

            <div class="contact-meta">
              <div v-if="unreadMap[contact.username] > 0" class="unread-badge">
                {{ unreadMap[contact.username] }}
              </div>

              <!-- More actions button -->
              <div class="contact-actions" @click.stop>
                <button
                  @click="toggleContactMenu(contact.id)"
                  class="more-btn"
                >
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z"></path>
                  </svg>
                </button>

                <!-- Dropdown menu -->
                <div
                  v-if="activeContactMenu === contact.id"
                  class="contact-menu"
                >
                  <button
                    v-if="contact.status !== 'BLOCKED'"
                    @click="$emit('contact-command', { action: 'block', user: contact })"
                    class="menu-item warning"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                    </svg>
                    Block User
                  </button>
                  <button
                    v-else
                    @click="$emit('contact-command', { action: 'unblock', user: contact })"
                    class="menu-item"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path>
                    </svg>
                    Unblock User
                  </button>
                  <button
                    @click="$emit('contact-command', { action: 'delete', user: contact })"
                    class="menu-item danger"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                    </svg>
                    Delete Contact
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Key management -->
    <div class="key-management-section">
      <KeyManagement
        :is-dark-mode="isDarkMode"
        @export-keys="$emit('export-keys')"
        @import-keys="$emit('import-keys', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import KeyManagement from './KeyManagement.vue';

const props = defineProps({
  user: Object,
  contacts: Array,
  friendRequests: Array,
  currentChatTarget: Object,
  unreadMap: Object,
  searchQuery: String,
  getLastMessageText: Function,
  getAvatarColor: Function,
  isDarkMode: Boolean
});

const emit = defineEmits([
  'logout',
  'open-add-contact',
  'open-friend-requests',
  'select-chat',
  'contact-command',
  'update:searchQuery',
  'export-keys',
  'import-keys',
  'mobile-chat-selected'
]);

// 内部状态
const internalSearchQuery = ref(props.searchQuery);
const activeContactMenu = ref(null);

// 联系人点击处理
const handleContactClick = (contact) => {
  emit('select-chat', contact);
  emit('mobile-chat-selected'); // 移动端选择聊天后通知父组件
};

// 联系人菜单切换
const toggleContactMenu = (contactId) => {
  activeContactMenu.value = activeContactMenu.value === contactId ? null : contactId;
};

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (!event.target.closest('.contact-actions')) {
    activeContactMenu.value = null;
  }
};

// 监听搜索查询变化
watch(internalSearchQuery, (newValue) => {
  emit('update:searchQuery', newValue);
});

watch(() => props.searchQuery, (newValue) => {
  if (internalSearchQuery.value !== newValue) {
    internalSearchQuery.value = newValue;
  }
});

// 生命周期钩子
onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/* 现代侧边栏样式 */
.modern-sidebar {
  @apply h-full flex flex-col backdrop-blur-xl border-r border-gray-200/50 dark:border-dark-200/50;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  background: linear-gradient(145deg, rgba(255,255,255,0.9), rgba(248,250,252,0.9));
}

.dark .modern-sidebar {
  background: linear-gradient(145deg, rgba(24,24,27,0.95), rgba(39,39,42,0.9));
}

/* 用户信息区域 */
.user-section {
  @apply p-6 border-b border-gray-100/50 dark:border-dark-200/50;
}

.user-card {
  @apply flex items-center space-x-4;
}

.avatar-container {
  @apply relative;
}

.user-avatar {
  @apply w-10 h-10 rounded-2xl flex items-center justify-center text-white font-semibold relative overflow-hidden;
  box-shadow: var(--shadow-soft);
}

.avatar-text {
  @apply relative z-10;
}



.user-details {
  @apply flex-1 min-w-0;
}

.user-name {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100 truncate;
}

.user-handle {
  @apply text-sm text-gray-500 dark:text-gray-400 truncate;
}

.logout-button {
  @apply p-2.5 rounded-xl text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all duration-200;
}

.logout-button:hover svg {
  @apply scale-110;
}

/* 搜索和操作栏 */
.action-bar {
  @apply p-4 flex items-center space-x-3;
}

.search-container {
  @apply flex-1;
}

.search-input-wrapper {
  @apply relative;
}

.search-icon {
  @apply absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400;
}

.search-input {
  @apply w-full pl-10 pr-4 py-3 bg-white/60 dark:bg-dark-100/60 border border-gray-200/50 dark:border-dark-200/50 rounded-2xl text-sm text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500/50 transition-all duration-200;
}

.add-contact-btn {
  @apply p-3 bg-blue-500 hover:bg-blue-600 text-white rounded-2xl transition-all duration-200 transform hover:scale-105 active:scale-95;
  box-shadow: var(--shadow-soft);
}

.add-contact-btn:hover {
  box-shadow: var(--shadow-float);
}

/* 好友请求卡片 */
.friend-requests-card {
  @apply mx-4 mb-4 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-2xl cursor-pointer transition-all duration-200 hover:bg-blue-100 dark:hover:bg-blue-900/30 flex items-center space-x-3 border border-blue-200/50 dark:border-blue-800/50;
}

.request-icon {
  @apply p-2 bg-blue-500 text-white rounded-lg;
}

.request-text {
  @apply flex-1 font-medium text-blue-800 dark:text-blue-200;
}

.request-badge {
  @apply px-2.5 py-1 bg-blue-500 text-white text-xs font-bold rounded-full min-w-[20px] text-center;
}

/* 联系人区域 */
.contacts-section {
  @apply flex-1 flex flex-col overflow-hidden;
}

.section-header {
  @apply flex items-center justify-between px-6 py-3 border-b border-gray-100/50 dark:border-dark-200/50;
}

.section-header h4 {
  @apply text-sm font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wide;
}

.contact-count {
  @apply text-xs text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-dark-200 px-2 py-1 rounded-lg;
}

.contacts-list {
  @apply flex-1 overflow-y-auto px-3 py-2 space-y-2;
}

/* 联系人卡片 */
.contact-card {
  @apply p-4 rounded-2xl cursor-pointer transition-all duration-200 hover:bg-white/60 dark:hover:bg-dark-100/60 flex items-center space-x-3 relative;
}

.contact-card:hover {
  box-shadow: var(--shadow-soft);
}

.contact-card.active {
  @apply bg-white dark:bg-dark-100 border border-blue-100 dark:border-blue-900/30;
  box-shadow: var(--shadow-soft);
}

.contact-card::before {
  content: '';
  @apply absolute left-0 top-1/2 transform -translate-y-1/2 w-1 h-8 bg-blue-500 rounded-r-lg opacity-0 transition-opacity duration-200;
}

.contact-card.active::before {
  @apply opacity-100;
}

.contact-avatar-container {
  @apply relative;
}

.contact-avatar {
  @apply w-12 h-12 rounded-2xl flex items-center justify-center text-white font-semibold;
  box-shadow: var(--shadow-soft);
}

.blocked-indicator {
  @apply absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center;
}

.contact-content {
  @apply flex-1 flex items-center justify-between min-w-0;
}

.contact-info {
  @apply flex-1 min-w-0;
}

.contact-name {
  @apply font-semibold text-gray-900 dark:text-gray-100 truncate text-sm;
}

.contact-preview {
  @apply text-xs text-gray-500 dark:text-gray-400 truncate mt-1;
}

.contact-meta {
  @apply flex items-center space-x-2;
}

.unread-badge {
  @apply px-2 py-1 bg-red-500 text-white text-xs font-bold rounded-full min-w-[20px] text-center;
}

/* 联系人操作 */
.contact-actions {
  @apply relative;
}

.more-btn {
  @apply p-1.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-dark-200 transition-colors duration-200 opacity-0;
}

.contact-card:hover .more-btn {
  @apply opacity-100;
}

.contact-menu {
  @apply absolute right-0 top-8 bg-white dark:bg-dark-100 border border-gray-200 dark:border-dark-200 rounded-xl py-2 z-50 min-w-[150px];
  box-shadow: var(--shadow-float);
}

.menu-item {
  @apply w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-200 transition-colors duration-150 flex items-center space-x-3;
}

.menu-item.warning {
  @apply text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-900/20;
}

.menu-item.danger {
  @apply text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20;
}

/* 密钥管理区域 */
.key-management-section {
  @apply p-5 border-t border-gray-100/50 dark:border-dark-200/50;
}

/* 滚动条样式 */
.contacts-list::-webkit-scrollbar {
  @apply w-2;
}

.contacts-list::-webkit-scrollbar-track {
  @apply bg-transparent;
}

.contacts-list::-webkit-scrollbar-thumb {
  @apply bg-gray-300 dark:bg-dark-300 rounded-full;
}

.contacts-list::-webkit-scrollbar-thumb:hover {
  @apply bg-gray-400 dark:bg-dark-400;
}

/* 动画效果 */
.contact-card {
  animation: slideInLeft 0.3s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>

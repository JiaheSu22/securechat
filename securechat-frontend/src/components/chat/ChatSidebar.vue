<!-- securechat-frontend/src/components/chat/ChatSidebar.vue -->
<template>
  <div class="sidebar">
    <div v-if="user" class="sidebar-header">
      <el-avatar :style="{ backgroundColor: getAvatarColor(user) }" class="user-avatar" size="default">
        {{ user.nickname[0].toUpperCase() }}
      </el-avatar>
      <div class="user-info">
        <span class="user-nickname">{{ user.nickname }}</span>
        <span class="user-id">@{{ user.username }}</span>
      </div>
      <el-button type="danger" plain @click="$emit('logout')" class="logout-btn">Logout</el-button>
    </div>

    <div class="contact-list-container">
      <div class="search-add-bar">
        <el-input v-model="internalSearchQuery" placeholder="Search contacts..." :prefix-icon="Search" clearable />
        <el-tooltip effect="dark" content="Add New Contact" placement="top">
          <el-button :icon="Plus" circle @click="$emit('open-add-contact')" class="add-btn" />
        </el-tooltip>
      </div>

      <div class="friend-requests-section" @click="$emit('open-friend-requests')">
        <el-icon><Bell /></el-icon>
        <span>Friend Requests</span>
        <el-badge :value="friendRequests.length" class="request-badge" />
      </div>

      <div class="contact-list">
        <div
          v-for="contact in contacts"
          :key="contact.id"
          :class="['contact-item', { 'active': currentChatTarget?.id === contact.id }]"
          @click="$emit('select-chat', contact)"
        >
          <el-avatar :style="{ backgroundColor: getAvatarColor(contact) }" class="contact-avatar">
            {{ contact.nickname[0].toUpperCase() }}
          </el-avatar>
          <div class="contact-info">
            <span class="contact-name">{{ contact.nickname }}</span>
            <span class="contact-last-msg">{{ getLastMessageText(contact.username) }}</span>
          </div>
          <span v-if="unreadMap[contact.username] > 0" class="unread-dot">{{ unreadMap[contact.username] }}</span>
          <el-dropdown class="more-actions" trigger="click" @command="$emit('contact-command', $event)" :teleported="false">
            <el-button :icon="MoreFilled" text circle class="more-btn" @click.stop />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-if="contact.status !== 'BLOCKED'"
                  :command="{ action: 'block', user: contact }"
                >
                  <el-icon><CircleClose /></el-icon>
                  <span>Block User</span>
                </el-dropdown-item>
                <el-dropdown-item
                  v-else
                  :command="{ action: 'unblock', user: contact }"
                >
                  <el-icon><SuccessFilled /></el-icon>
                  <span>Unblock User</span>
                </el-dropdown-item>
                <el-dropdown-item :command="{ action: 'delete', user: contact }" divided>
                  <el-icon color="#F56C6C"><Delete /></el-icon>
                  <span style="color: #F56C6C;">Delete Contact</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <!-- Key Management Component -->
      <KeyManagement @export-keys="$emit('export-keys')" @import-keys="$emit('import-keys', $event)" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { Search, Plus, Bell, MoreFilled, CircleClose, SuccessFilled, Delete } from '@element-plus/icons-vue';
import KeyManagement from './KeyManagement.vue';

const props = defineProps({
  user: Object,
  contacts: Array,
  friendRequests: Array,
  currentChatTarget: Object,
  unreadMap: Object,
  searchQuery: String,
  getLastMessageText: Function,
  getAvatarColor: Function
});

const emit = defineEmits([
  'logout',
  'open-add-contact',
  'open-friend-requests',
  'select-chat',
  'contact-command',
  'update:searchQuery',
  'export-keys',
  'import-keys'
]);

const internalSearchQuery = ref(props.searchQuery);

watch(internalSearchQuery, (newValue) => {
  emit('update:searchQuery', newValue);
});

watch(() => props.searchQuery, (newValue) => {
  if (internalSearchQuery.value !== newValue) {
    internalSearchQuery.value = newValue;
  }
});
</script>

<style scoped>
/* Copied from ChatView.vue */
.sidebar { display: flex; flex-direction: column; width: 320px; background-color: #e9ecf1; border-right: 1px solid #dcdfe6; height: 100vh; }
.sidebar-header { display: flex; align-items: center; padding: 10px 15px; height: 70px; border-bottom: 1px solid #dcdfe6; flex-shrink: 0; }
.user-avatar { flex-shrink: 0; font-weight: bold; }
.user-info { display: flex; flex-direction: column; margin-left: 12px; flex-grow: 1; overflow: hidden; }
.user-nickname { font-size: 16px; font-weight: bold; color: #303133; }
.user-id { font-size: 12px; color: #a8aeb3; }
.logout-btn { margin-left: auto; flex-shrink: 0; }
.contact-list-container { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; }
.search-add-bar { display: flex; align-items: center; padding: 10px; border-bottom: 1px solid #dcdfe6; gap: 8px; }
.add-btn { flex-shrink: 0; }
.friend-requests-section { display: flex; align-items: center; padding: 12px 15px; cursor: pointer; border-bottom: 1px solid #e4e7ed; background-color: #fff; color: #409eff; font-weight: 500; }
.friend-requests-section:hover { background-color: #f0f8ff; }
.friend-requests-section span { margin-left: 8px; flex-grow: 1; }
.request-badge { margin-right: 5px; }
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
  right: 40px; /* Adjust layout */
  top: 50%;
  transform: translateY(-50%);
}
</style>
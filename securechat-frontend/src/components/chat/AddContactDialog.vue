<!-- securechat-frontend/src/components/chat/AddContactDialog.vue -->
<template>
  <el-dialog
    :model-value="visible"
    @update:modelValue="$emit('update:visible', $event)"
    title="Add New Contact"
    width="400px"
  >
    <div class="add-contact-dialog-content">
      <div class="add-contact-search-bar">
        <el-input
          v-model="addFriendUsername"
          placeholder="Enter username to send friend request"
          :prefix-icon="Search"
          @keyup.enter="handleSend"
          clearable
          class="add-contact-input"
        />
        <el-button type="primary" @click="handleSend" class="search-dialog-btn">Send Request</el-button>
      </div>
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">Close</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue';
import { Search } from '@element-plus/icons-vue';

defineProps({
  visible: Boolean
});

const emit = defineEmits(['update:visible', 'send-friend-request']);

const addFriendUsername = ref('');

const handleSend = () => {
  emit('send-friend-request', addFriendUsername.value);
  addFriendUsername.value = '';
};
</script>

<style scoped>
/* Copied from ChatView.vue */
.add-contact-dialog-content { margin-bottom: 10px; }
.add-contact-search-bar { display: flex; gap: 10px; align-items: center; }
.add-contact-input { flex: 1 1 0; }
.search-dialog-btn { flex-shrink: 0; }
</style>
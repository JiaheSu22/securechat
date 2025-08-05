<!-- securechat-frontend/src/components/chat/FriendRequestsDialog.vue -->
<template>
  <el-dialog
    :model-value="visible"
    @update:modelValue="$emit('update:visible', $event)"
    title="Pending Friend Requests"
    width="400px"
  >
    <div v-if="!requests.length">No pending requests.</div>
    <div v-for="req in requests" :key="req.id" class="friend-request-item">
      <el-avatar :style="{ backgroundColor: getAvatarColor(req) }" size="small" />
      <span>{{ req.nickname }} (@{{ req.username }})</span>
      <div class="request-actions">
        <el-button size="small" type="success" @click="$emit('accept-request', req)">Accept</el-button>
        <el-button size="small" type="warning" @click="$emit('decline-request', req)">Decline</el-button>
      </div>
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">Close</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  visible: Boolean,
  requests: Array,
  getAvatarColor: Function,
});

defineEmits(['update:visible', 'accept-request', 'decline-request']);
</script>

<style scoped>
/* Copied from ChatView.vue */
.friend-request-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.friend-request-item:last-child { border-bottom: none; }
.request-actions { margin-left: auto; display: flex; gap: 8px; }
</style>
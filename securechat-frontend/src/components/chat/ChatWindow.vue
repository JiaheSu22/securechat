<!-- securechat-frontend/src/components/chat/ChatWindow.vue -->
<template>
  <div class="chat-area">
    <template v-if="currentChatTarget">
      <div class="chat-header">
        <div class="chat-title">
          <span>{{ currentChatTarget.nickname }}</span>
          <span class="chat-subtitle">@{{ currentChatTarget.username }}</span>
        </div>
      </div>
      <div ref="messageContainerRef" class="message-container">
        <div v-for="msg in messages" :key="msg.id" :class="['message-row', getMessageClass(msg.sender)]">
          <!-- System messages -->
          <div v-if="msg.sender === 'system'" class="system-message">
            <el-icon v-if="msg.encrypted"><Lock /></el-icon>
            <span :class="{ 'encrypted-text': msg.encrypted }">{{ msg.text }}</span>
          </div>

          <!-- User messages -->
          <div v-else class="message-bubble" style="position: relative;">
            <div v-if="msg.messageType === 'FILE'">
              <div class="file-message-content">
                <el-icon :size="24" style="margin-right: 8px;"><Document /></el-icon>
                <div class="file-info">
                  <span class="file-name">{{ msg.originalFilename }}</span>
                  <div v-if="msg.text && msg.text !== '[File Encrypted]'" class="file-description">
                    {{ msg.text }}
                  </div>
                </div>
              </div>
              <el-button
                :icon="Download"
                circle
                size="small"
                class="file-download-btn"
                @click="$emit('download-file', msg)"
                :style="getDownloadBtnStyle(msg)"
                title="Download"
              />
            </div>
            <p v-else>{{ msg.text }}</p>
          </div>
        </div>
      </div>
      <div class="message-input-box">
        <el-tooltip effect="dark" content="Send File" placement="top">
          <el-button :icon="Paperclip" @click="$emit('attach-file')" class="input-action-btn" />
        </el-tooltip>
        <input type="file" ref="fileInput" style="display:none" @change="onFileChange" />
        <div v-if="selectedFile" class="file-attachment-bubble">
          <span class="remove-attachment" @click="$emit('remove-attachment')">×</span>
          <el-icon><Paperclip /></el-icon>
          <span class="file-name">{{ selectedFile.name }}</span>
        </div>
        <el-input
          :model-value="newMessage"
          @update:modelValue="$emit('update:newMessage', $event)"
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
</template>

<script setup>
import { ref, nextTick, watch } from 'vue';
import { Promotion, Paperclip, Lock, Document, Download } from '@element-plus/icons-vue';

const props = defineProps({
  currentChatTarget: Object,
  messages: Array,
  newMessage: String,
  selectedFile: Object,
  isChatBlocked: Boolean,
  canSendMessage: Boolean,
  getMessageClass: Function,
  getDownloadBtnStyle: Function,
});

const emit = defineEmits([
  'send-message',
  'attach-file',
  'remove-attachment',
  'update:newMessage',
  'file-changed',
  'download-file'
]);

const messageContainerRef = ref(null);
const fileInput = ref(null);

const handleEnterKey = (event) => {
  if (props.isChatBlocked) return;
  if (event.shiftKey) {
    const textarea = event.target;
    const value = props.newMessage;
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const newText = value.slice(0, start) + '\n' + value.slice(end);
    emit('update:newMessage', newText);
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd = start + 1;
    });
    event.preventDefault();
  } else {
    handleSend();
  }
};

const handleSend = () => {
    emit('send-message');
}

const onFileChange = (e) => {
  const file = e.target.files[0];
  emit('file-changed', file);
  e.target.value = ''; // Reset input
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainerRef.value) {
      messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight;
    }
  });
};

watch(() => props.messages, () => {
  scrollToBottom();
}, { deep: true });

// Expose method to parent
const triggerFileInput = () => {
    if(fileInput.value) fileInput.value.click();
}

defineExpose({
  scrollToBottom,
  triggerFileInput,
});
</script>

<style scoped>
/* Copied from ChatView.vue */
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
.message-input-box { padding: 15px 20px; border-top: 1px solid #e4e7ed; background-color: #f8f9fa; display: flex; justify-content: center; align-items: flex-end; gap: 10px; position: relative; }
.input-action-btn { align-self: flex-end; flex: 0 0 auto; }
.chat-textarea { flex: 1 1 0; min-width: 0; max-width: 600px; min-width: 200px; }
.welcome-screen { display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; color: #909399; }
.welcome-screen h2 { font-size: 24px; font-weight: 300; margin: 20px 0 10px; }
.file-attachment-bubble { display: inline-flex; align-items: center; background: #f3f6fa; color: #333; border-radius: 16px; padding: 6px 16px 6px 10px; margin-bottom: 6px; position: absolute; left: 20px; top: -38px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); z-index: 10; }
.remove-attachment { color: #f56c6c; font-weight: bold; font-size: 16px; margin-right: 8px; cursor: pointer; position: absolute; left: 4px; top: 2px; }
.file-name { font-weight: 500; margin-left: 6px; }
.file-message-content { display: flex; align-items: center; }
.file-info { display: flex; flex-direction: column; }
.file-description { font-size: 12px; color: #606266; margin-top: 4px; font-style: italic; line-height: 1.3; }
.file-download-btn { /* Style for download button */ }
</style>
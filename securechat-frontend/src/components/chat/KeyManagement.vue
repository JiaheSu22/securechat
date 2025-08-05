<!-- securechat-frontend/src/components/chat/KeyManagement.vue -->
<template>
  <div class="key-actions-bar">
    <el-tooltip content="Export Private Key" placement="top">
      <el-button :icon="Download" circle size="small" @click="showExportDialog = true" />
    </el-tooltip>
    <el-tooltip content="Import Private Key" placement="top">
      <el-button :icon="Upload" circle size="small" @click="showImportDialog = true" />
    </el-tooltip>

    <el-dialog v-model="showExportDialog" title="Export Private Key" width="400px">
      <el-input type="textarea" :rows="6" :value="exportedKeys" readonly style="margin-bottom: 10px;" />
      <el-button type="primary" @click="copyToClipboard(exportedKeys)">Copy</el-button>
      <el-button type="info" @click="downloadAsFile(exportedKeys)">Download as File</el-button>
    </el-dialog>

    <el-dialog v-model="showImportDialog" title="Import Private Key" width="400px">
      <el-input type="textarea" v-model="importKeyStr" :rows="6" placeholder="Paste your private key JSON here" style="margin-bottom: 10px;" />
      <el-button type="success" @click="handleImport">Import</el-button>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Download, Upload } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';
import { exportPrivateKeysToFile, copyPrivateKeysToClipboard } from '@/utils/keyExport';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const showExportDialog = ref(false);
const showImportDialog = ref(false);
const importKeyStr = ref('');

const emit = defineEmits(['import-keys']);

const exportedKeys = ref('');

// Function to be called by parent to get latest keys
function exportPrivateKeys() {
  const x25519 = localStorage.getItem('x25519PrivateKey');
  const ed25519 = localStorage.getItem('ed25519PrivateKey');
  const exportObj = { x25519PrivateKey: x25519, ed25519PrivateKey: ed25519 };
  exportedKeys.value = JSON.stringify(exportObj, null, 2);
  return exportedKeys.value;
}

function copyToClipboard(text) {
  if (!text) return;
  copyPrivateKeysToClipboard(JSON.parse(text));
}

function downloadAsFile(text) {
    if (!text) return;
  const keys = JSON.parse(text);
  exportPrivateKeysToFile(keys, authStore.user?.username);
}

function handleImport() {
  emit('import-keys', importKeyStr.value);
  showImportDialog.value = false;
  importKeyStr.value = '';
}

// Expose the function to the parent component
defineExpose({
    exportPrivateKeys
});
</script>

<style scoped>
.key-actions-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin: 16px 0 24px 0;
  padding-bottom: 16px;
  border-top: 1px solid #dcdfe6;
}
</style>
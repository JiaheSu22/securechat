<!-- KeyManagement.vue - Modern Key Management -->
<template>
  <div class="key-management">
    <div class="key-actions">
      <button 
        @click="openExportDialog"
        class="key-action-btn export-btn"
        title="Export Private Key"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
        </svg>
        <span>Export Keys</span>
      </button>
      
      <button 
        @click="openImportDialog"
        class="key-action-btn import-btn"
        title="Import Private Key"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10"></path>
        </svg>
        <span>Import Keys</span>
      </button>
    </div>

    <!-- Export keys dialog -->
    <transition name="modal-overlay">
      <div v-if="showExportDialog" class="modal-overlay" @click="closeExportDialog">
        <transition name="modal-content">
          <div v-if="showExportDialog" class="modal-container" @click.stop>
            <div class="modal-header">
              <div class="header-icon export-icon">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
              </div>
              <div class="header-text">
                <h3 class="modal-title">Export Private Keys</h3>
                <p class="modal-subtitle">Keep your private keys safe and never share them with anyone</p>
              </div>
              <button @click="closeExportDialog" class="close-btn">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>

            <div class="modal-body">
              <div class="warning-banner">
                <svg class="w-8 h-8 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.082 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                </svg>
                <span class="font-medium">Keep your private keys in a safe place - they cannot be recovered if lost!</span>
              </div>
              
              <div class="key-content">
                <label class="content-label">Private Key Content</label>
                <div class="key-display">
                  <textarea 
                    :value="exportedKeys" 
                    readonly 
                    class="key-textarea"
                    rows="8"
                  ></textarea>
                </div>
              </div>
            </div>

            <div class="modal-footer">
              <button @click="copyToClipboard" class="action-btn copy-btn">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                </svg>
                Copy to Clipboard
              </button>
              <button @click="downloadAsFile" class="action-btn download-btn">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                </svg>
                Download File
              </button>
            </div>
          </div>
        </transition>
      </div>
    </transition>

    <!-- Import keys dialog -->
    <transition name="modal-overlay">
      <div v-if="showImportDialog" class="modal-overlay" @click="closeImportDialog">
        <transition name="modal-content">
          <div v-if="showImportDialog" class="modal-container" @click.stop>
            <div class="modal-header">
              <div class="header-icon import-icon">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
                </svg>
              </div>
              <div class="header-text">
                <h3 class="modal-title">Import Private Keys</h3>
                <p class="modal-subtitle">Paste your private key JSON content</p>
              </div>
              <button @click="closeImportDialog" class="close-btn">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>

            <div class="modal-body">
              <div class="input-group">
                <label class="content-label">Private Key Content</label>
                <textarea 
                  v-model="importKeyStr" 
                  placeholder="Paste your private key JSON content here..."
                  class="import-textarea"
                  rows="8"
                ></textarea>
                <p class="input-hint">Make sure to paste the complete JSON format private key content</p>
              </div>
            </div>

            <div class="modal-footer">
              <button 
                @click="closeImportDialog" 
                class="cancel-btn action-btn"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
                <span>Cancel</span>
              </button>
              <button 
                @click="handleImport"
                :disabled="!importKeyStr.trim()"
                class="submit-btn action-btn"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
                </svg>
                <span>Import Keys</span>
              </button>
            </div>
          </div>
        </transition>
      </div>
    </transition>
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

// Open export dialog
function openExportDialog() {
  exportPrivateKeys();
  showExportDialog.value = true;
}

// Close export dialog
function closeExportDialog() {
  showExportDialog.value = false;
}

// Open import dialog
function openImportDialog() {
  showImportDialog.value = true;
}

// Close import dialog
function closeImportDialog() {
  showImportDialog.value = false;
  importKeyStr.value = '';
}

// Function to be called by parent to get latest keys
function exportPrivateKeys() {
  const x25519 = localStorage.getItem('x25519PrivateKey');
  const ed25519 = localStorage.getItem('ed25519PrivateKey');
  const exportObj = { x25519PrivateKey: x25519, ed25519PrivateKey: ed25519 };
  exportedKeys.value = JSON.stringify(exportObj, null, 2);
  return exportedKeys.value;
}

function copyToClipboard() {
  if (!exportedKeys.value) return;
  try {
    copyPrivateKeysToClipboard(JSON.parse(exportedKeys.value));
    ElMessage.success('Keys copied to clipboard!');
  } catch (error) {
    ElMessage.error('Failed to copy keys');
  }
}

function downloadAsFile() {
  if (!exportedKeys.value) return;
  try {
    const keys = JSON.parse(exportedKeys.value);
    exportPrivateKeysToFile(keys, authStore.user?.username);
    ElMessage.success('Keys downloaded successfully!');
  } catch (error) {
    ElMessage.error('Failed to download keys');
  }
}

function handleImport() {
  if (!importKeyStr.value.trim()) return;
  
  try {
    const keys = JSON.parse(importKeyStr.value);
    emit('import-keys', keys);
    showImportDialog.value = false;
    importKeyStr.value = '';
    // Don't show success message here - parent component will handle it
  } catch (error) {
    ElMessage.error('Invalid key format. Please ensure the JSON format is correct.');
  }
}

// Expose the function to the parent component
defineExpose({
    exportPrivateKeys
});
</script>

<style scoped>
/* Key management container */
.key-management {
  @apply w-full;
}

/* Key actions */
.key-actions {
  @apply flex items-center justify-center space-x-3 p-4;
}

.key-action-btn {
  @apply flex items-center space-x-2 px-4 py-2.5 bg-white/60 dark:bg-dark-100/60 border border-gray-200/50 dark:border-dark-200/50 rounded-xl text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-white/80 dark:hover:bg-dark-100/80 hover:border-blue-300 dark:hover:border-blue-600 transition-all duration-200 backdrop-blur-sm;
}

.key-action-btn:hover {
  @apply transform scale-105;
  box-shadow: var(--shadow-soft);
}

.export-btn {
  @apply text-blue-600 dark:text-blue-400;
}

.import-btn {
  @apply text-green-600 dark:text-green-400;
}

/* Modal overlay */
.modal-overlay {
  @apply fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4;
}

/* Modal container */
.modal-container {
  @apply w-full max-w-lg bg-white dark:bg-dark-100 rounded-3xl overflow-hidden;
  box-shadow: var(--shadow-float);
}

/* Modal header */
.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-100/50 dark:border-dark-200/50;
}

.header-icon {
  @apply p-3 rounded-2xl;
}

.export-icon {
  @apply bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400;
}

.import-icon {
  @apply bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400;
}

.header-text {
  @apply flex-1 ml-4;
}

.modal-title {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100;
}

.modal-subtitle {
  @apply text-sm text-gray-500 dark:text-gray-400 mt-1;
}

.close-btn {
  @apply p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-200 rounded-xl transition-colors duration-200;
}

/* Modal body */
.modal-body {
  @apply p-6;
}

.warning-banner {
  @apply flex items-center space-x-3 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200/50 dark:border-yellow-800/50 rounded-2xl text-yellow-800 dark:text-yellow-200 mb-6;
}

.key-content {
  @apply space-y-3;
}

.content-label {
  @apply block text-sm font-medium text-gray-700 dark:text-gray-300;
}

.key-display {
  @apply relative;
}

.key-textarea {
  @apply w-full p-4 bg-gray-50/80 dark:bg-dark-200/80 border border-gray-200/50 dark:border-dark-300/50 rounded-2xl text-sm text-gray-900 dark:text-gray-100 font-mono resize-none focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500/50 transition-all duration-200 backdrop-blur-sm;
}

.import-textarea {
  @apply w-full p-4 bg-gray-50/80 dark:bg-dark-200/80 border border-gray-200/50 dark:border-dark-300/50 rounded-2xl text-sm text-gray-900 dark:text-gray-100 font-mono resize-none placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500/50 transition-all duration-200 backdrop-blur-sm;
}

.input-hint {
  @apply text-xs text-gray-500 dark:text-gray-400 mt-2;
}

.input-group {
  @apply space-y-3;
}

/* Modal footer */
.modal-footer {
  @apply flex items-center justify-end space-x-3 p-6 border-t border-gray-100/50 dark:border-dark-200/50;
}

.action-btn {
  @apply flex items-center space-x-2 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200;
}

.copy-btn {
  @apply bg-blue-500 hover:bg-blue-600 text-white;
  box-shadow: var(--shadow-soft);
}

.copy-btn:hover {
  box-shadow: var(--shadow-float);
}

.download-btn {
  @apply bg-green-500 hover:bg-green-600 text-white;
  box-shadow: var(--shadow-soft);
}

.download-btn:hover {
  box-shadow: var(--shadow-float);
}

.cancel-btn {
  @apply bg-gray-100 dark:bg-dark-200 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-dark-300 border border-gray-200 dark:border-dark-300;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.cancel-btn:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.submit-btn {
  @apply bg-blue-500 hover:bg-blue-600 text-white disabled:bg-gray-300 dark:disabled:bg-dark-300 disabled:text-gray-500 disabled:cursor-not-allowed border border-blue-600 dark:border-blue-400;
  box-shadow: var(--shadow-soft);
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.submit-btn:hover:not(:disabled) {
  box-shadow: var(--shadow-float);
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background: linear-gradient(135deg, #d1d5db, #9ca3af);
  border-color: #d1d5db;
  transform: none;
}

/* Transitions */
.modal-overlay-enter-active,
.modal-overlay-leave-active {
  @apply transition-opacity duration-300;
}

.modal-overlay-enter-from,
.modal-overlay-leave-to {
  @apply opacity-0;
}

.modal-content-enter-active,
.modal-content-leave-active {
  @apply transition-all duration-300;
}

.modal-content-enter-from,
.modal-content-leave-to {
  @apply opacity-0 transform scale-95;
}
</style>
// src/utils/keyExport.js - Private key export utilities
import { ElMessage } from 'element-plus'

/**
 * Export private keys to file
 * @param {Object} keys - Object containing private keys
 * @param {string} username - Username
 * @returns {boolean} - Whether export was successful
 */
export function exportPrivateKeysToFile(keys, username) {
  try {
    const jsonStr = JSON.stringify(keys, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${username || 'unknown'}-securechat-private-keys.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    ElMessage.success('Private keys have been saved to file. Please keep them secure!');
    return true;
  } catch (e) {
    ElMessage.error('Failed to save private keys to file: ' + e.message);
    return false;
  }
}

/**
 * Copy private keys to clipboard
 * @param {Object} keys - Object containing private keys
 * @returns {boolean} - Whether copy was successful
 */
export function copyPrivateKeysToClipboard(keys) {
  try {
    const jsonStr = JSON.stringify(keys, null, 2);
    navigator.clipboard.writeText(jsonStr).then(() => {
      ElMessage.success('Private keys have been copied to clipboard. Please save them securely!');
    });
    return true;
  } catch (e) {
    ElMessage.warning('Failed to copy to clipboard. Please copy manually: ' + JSON.stringify(keys, null, 2));
    return false;
  }
} 
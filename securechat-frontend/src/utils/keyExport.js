// src/utils/keyExport.js
import { ElMessage } from 'element-plus'

/**
 * 导出私钥到文件
 * @param {Object} keys - 包含私钥的对象
 * @param {string} username - 用户名
 * @returns {boolean} - 是否成功导出
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
 * 复制私钥到剪贴板
 * @param {Object} keys - 包含私钥的对象
 * @returns {boolean} - 是否成功复制
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
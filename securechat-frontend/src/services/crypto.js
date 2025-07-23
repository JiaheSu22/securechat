// src/services/crypto.js
import { JSEncrypt } from 'jsencrypt';

// 生成新的RSA密钥对
export function generateKeyPair() {
  const crypt = new JSEncrypt({ default_key_size: 2048 });
  const privateKey = crypt.getPrivateKey();
  const publicKey = crypt.getPublicKey();
  return { privateKey, publicKey };
}

// 用对方公钥加密消息
export function encryptMessage(publicKey, message) {
  const encrypt = new JSEncrypt();
  encrypt.setPublicKey(publicKey);
  return encrypt.encrypt(message);
}

// 用本地私钥解密消息
export function decryptMessage(privateKey, encryptedMessage) {
  const decrypt = new JSEncrypt();
  decrypt.setPrivateKey(privateKey);
  return decrypt.decrypt(encryptedMessage);
} 
# SecureChat

A simple, full-stack secure messaging application featuring **end-to-end encryption (E2EE)**. SecureChat ensures that only you and your friends can read your messages—no one else, not even the server.

## Project Overview

SecureChat is designed for privacy-first, real-time communication. It leverages strong cryptography in the browser and on the backend, with all encryption and decryption performed on the client side. The server never has access to your private keys or plaintext messages.

- **Frontend:** Vue 3 + Vite + Element Plus UI, with cryptography powered by [libsodium-wrappers](https://github.com/jedisct1/libsodium).
- **Backend:** Spring Boot 3 (Java 17), RESTful API, JWT authentication, and WebSocket for real-time messaging.
- **Database:** MySQL (for user and message metadata).

---

## End-to-End Encryption (E2EE) Design

### Key Architecture

- **Key Generation:**  
  - On registration, each user generates two key pairs in the browser:
    - **Ed25519**: For digital signatures (reserved for future use).
    - **X25519**: For ECDH key agreement (session key negotiation).
  - **Public keys** are uploaded to the backend.  
  - **Private keys** are stored only in the browser (localStorage) and never leave the client.

- **Key Exchange:**  
  - When starting a chat, the frontend fetches the peer's X25519 public key.
  - Both sides use their X25519 private key and the peer's public key to derive a **shared session key** using ECDH:
    ```js
    sessionKey = sodium.crypto_scalarmult(myPrivateKey, theirPublicKey)
    ```
  - All keys are encoded in base64url.

### Message Encryption

- **Algorithm:**  
  - Uses **ChaCha20-Poly1305** (via libsodium's `crypto_aead_chacha20poly1305_ietf`).
- **Process:**  
  1. For each message, generate a random 12-byte nonce:
     ```js
     const nonce = sodium.randombytes_buf(sodium.crypto_aead_chacha20poly1305_ietf_NPUBBYTES)
     ```
  2. Encrypt the plaintext with the session key and nonce:
     ```js
     const cipher = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(
       sodium.from_string(plainText),
       null, null, nonce, sessionKey
     )
     ```
  3. Send the ciphertext and nonce (both base64url) to the backend.
- **Decryption:**  
  - The recipient uses the same session key and nonce to decrypt:
    ```js
    const plain = sodium.crypto_aead_chacha20poly1305_ietf_decrypt(
      null, cipher, null, nonce, sessionKey
    )
    ```

### Security Model

- **Private keys** never leave the client; the backend cannot decrypt any message.
- **Session keys** are derived per friend, allowing both parties to decrypt all historical messages.
- **Nonces** ensure each message is uniquely encrypted, preventing replay attacks.
- **Ed25519** keys are reserved for future message signing and verification.

### API & Data Flow

- **Send Message:**  
  - `POST /api/messages` with fields: `receiverUsername`, `encryptedContent`, `nonce`, `messageType`, and (for files) `fileUrl`, `originalFilename`.
- **Fetch Messages:**  
  - `GET /api/messages/{otherUsername}` returns encrypted messages and metadata.
- **WebSocket:**  
  - Real-time message delivery using Spring WebSocket, with all payloads encrypted.

---

## Frontend Cryptography Dependency

SecureChat uses the following cryptography library in the frontend:

- [`libsodium-wrappers`](https://github.com/jedisct1/libsodium)  
  - Version: ^0.7.15  
  - Provides robust, audited cryptographic primitives for key generation, ECDH, and authenticated encryption.

**Other major frontend dependencies:**
- Vue 3, Element Plus, Pinia, Axios, @stomp/stompjs

---

## Deployment Guide

### Prerequisites

- **Backend:** JDK 17+, Maven, MySQL
- **Frontend:** Node.js 18+, npm

### 1. Clone the Repository

```bash
git clone https://github.com/your-repository/securechat.git
cd securechat
```

### 2. Backend Setup

```bash
# Build the backend
mvn clean package

# Start the backend (default port 8080)
mvn spring-boot:run
```

- Configure your MySQL database and application properties as needed.

### 3. Frontend Setup

```bash
cd securechat-frontend
npm install
npm run build   # For production
npm run dev     # For development (default port 5173)
```

### 4. Access the Application

- Open your browser at [http://localhost:5173](http://localhost:5173) (frontend)
- The backend API runs at [http://localhost:8080](http://localhost:8080)

---

## License

This project is licensed under the MIT License.  
Cryptography powered by [libsodium](https://github.com/jedisct1/libsodium).

# Diffie-Hellman Key Exchange (ECDH) Project

## Project Overview

This project implements a secure communication system based on **Diffie-Hellman Key Exchange** (using **ECDH** algorithm). The system includes a backend service (using **Spring Boot 3**) and a frontend client (using **Vite** and **Vue 3**). The core functionality of the system is to generate a shared key via key exchange and use this shared key to encrypt and decrypt text messages and files (images/videos), ensuring the security of information during transmission.

## Tech Stack

### Backend
- **Spring Boot 3**: For building and running the backend service.
- **Spring Security**: For authentication and authorization.
- **Spring WebSocket**: For real-time bidirectional communication over WebSocket.
- **JDK 17**: Java 17 as the development language and runtime environment.
- **Maven**: For project building and dependency management.

### Frontend
- **Vite**: Fast build tool for frontend development and bundling.
- **Vue 3**: For building the frontend UI and components.
- **Web Crypto API**: Native browser cryptographic API for implementing ECDH key exchange and encryption/decryption operations.

### Database
- **MySQL** (for local development, optional in production): For storing user information and message records.

### Key Exchange Algorithm
- **ECDH** (Elliptic Curve Diffie-Hellman): Using elliptic curve Diffie-Hellman algorithm for key exchange, offering a more efficient and secure key negotiation.

## TODO List

1. **User Authentication**
    - User registration and login (based on password hashing with bcrypt)
    - JWT authentication (to secure API access)

2. **Key Exchange Module**
    - Implement ECDH key exchange algorithm
    - Support for generating a shared key and secure transmission between client and server

3. **Message Encryption**
    - Text message encryption and decryption (using AES-GCM)
    - Real-time encrypted message transmission over WebSocket
    - File encryption and upload (images, videos)

4. **Frontend Implementation**
    - User interface: User registration, login, message display, file upload
    - Web Crypto API implementation for ECDH key exchange and AES encryption/decryption
    - WebSocket communication for real-time chat functionality

5. **Security Hardening**
    - MitM (Man-in-the-Middle) attack prevention
    - Message authentication tag (using AES-GCM authenticated ciphertext)
    - Implement message integrity checks

6. **Database Design**
    - User table (for storing user data)
    - Message table (for storing encrypted message records)
    - File table (for storing file metadata)

7. **Testing**
    - Unit tests and integration tests
    - Performance testing and encryption speed benchmarks

8. **Deployment and Containerization**
    - Configure Docker for development environment
    - Integrate backend deployment with frontend build
    - Optional: Deploy on cloud service or self-hosted server

## Backend Functionality and Implementation

### 1. **User Authentication**
The backend provides JWT-based authentication. Users must register and log in; upon successful login, a JWT token is returned for subsequent authorized requests.

- **Registration**: Users submit their username and password (which is hashed using BCrypt). The system checks for uniqueness of the username and saves the user information.
- **Login**: Users submit their username and password. The backend validates the password and generates a JWT token to be returned to the client. The JWT token is used for authentication in future requests.

### 2. **Key Exchange Module**
The key exchange is implemented using **ECDH**. This process allows both the client and server to exchange public keys and compute a shared symmetric key, which is then used for encrypting and decrypting data in subsequent communication.

- **Key Generation**: Both the client and server generate ECDH key pairs (private and public keys).
- **Key Exchange**: The client generates and sends its public key, while the server uses its private key and the client's public key to compute the shared key, and vice versa.
- **Session Key Generation**: The shared key derived from the ECDH exchange is used to generate a symmetric encryption key for encrypting further communication.

### 3. **Message Encryption and Decryption**
Messages are encrypted during transmission using **AES-GCM**, ensuring confidentiality and integrity. Each message is encrypted using the shared session key and transmitted via WebSocket in real-time.

- **Encryption**: Before sending a message, the frontend encrypts the message using AES-GCM. Each encrypted message includes an IV (Initialization Vector) and an authentication tag (authTag).
- **Decryption**: The backend decrypts the encrypted message using the shared session key and the IV, extracting the original message.

### 4. **File Encryption and Upload**
Files (such as images and videos) are uploaded in chunks, with each chunk being encrypted individually. This is done to optimize large file transfers and prevent interception of data during transmission.

- **File Encryption**: Files are split into smaller chunks, and each chunk is encrypted independently using AES-GCM.
- **File Upload**: The encrypted file chunks are transmitted via WebSocket or HTTP. The server receives and stores the encrypted file chunks, reassembling them once the entire file has been uploaded.

### 5. **WebSocket Real-Time Communication**
The backend uses **Spring WebSocket** to provide bidirectional communication. The client and server maintain a real-time WebSocket connection for message push notifications.

- **Message Push**: When a user sends an encrypted message, the message is pushed to the receiving user in real-time through the WebSocket connection.
- **Message Format**: Each message includes the encrypted text, IV, authentication tag, and other relevant data to ensure secure transmission.

### 6. **Database Design**
The backend uses **MySQL** to store user information and encrypted message records. Each message contains the encrypted text and the authentication tag, while user information is stored with hashed passwords.

---

## Installation and Running

1. **Clone the Project**
   ```bash
   git clone https://github.com/your-repository/diffie-hellman-key-exchange.git
   cd diffie-hellman-key-exchange
2. **Backend Build**

- Ensure JDK 17 is installed.

- Build the project using Maven:

   ```bash
   mvn clean package
   ```

3. **Frontend Build**
- Install dependencies and start the development server:
- ```bash
   cd frontend
   npm install
   npm run dev
   ```
4. **Start the Backend Service**
- Start the Spring Boot backend service:
- ```bash
   mvn spring-boot:run
   ```

5. **Access the Application**
- Open your web browser and navigate to the application URL (e.g., http://localhost:8080).
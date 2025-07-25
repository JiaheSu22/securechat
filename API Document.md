# SecureChat API Documentation 

This document provides a detailed specification of the SecureChat backend API. It is intended for frontend developers integrating with the service and for future maintenance.

## 1. General Information

*   **Base URL**: `http://localhost:8080` (or your deployment URL)
*   **API Root Path**: All REST API endpoints are prefixed with `/api`.
*   **Content-Type**: All request and response bodies are in `application/json` format.

## 2. Authentication

The application uses **JSON Web Tokens (JWT)** for authenticating API requests.

1.  A user first registers via `POST /api/auth/register`.
2.  After successful registration, the user authenticates via the `POST /api/auth/login` endpoint to receive a JWT.
3.  For all protected endpoints, the client must include this token in the `Authorization` header with the `Bearer` scheme.

**Example Header:**
```
Authorization: Bearer <your_jwt_token_here>
```

Requests to protected endpoints without a valid token will result in a `401 Unauthorized` error.

## 3. Error Responses

The API uses a standardized format for error responses.

| Status Code | Reason | When It Occurs |
| :--- | :--- | :--- |
| `400 Bad Request` | Validation Failed | Request body fails validation (e.g., password too short, username blank). |
| `401 Unauthorized`| Unauthorized | Authentication fails (e.g., missing, invalid, or expired JWT). |
| `404 Not Found` | Not Found | A requested resource does not exist (e.g., getting a key for a non-existent user). |
| `409 Conflict` | Resource Conflict | An attempt to create a resource that already exists (e.g., registering a user with a username that is already taken). |
| `500 Internal Server Error` | Server Error | An unexpected error occurred on the server. |

#### **Example: Validation Error (400)**
```json
{
  "timestamp": 1678886600000,
  "status": 400,
  "error": "Bad Request",
  "message": "password: Password must be at least 6 characters long",
  "path": "/api/auth/register"
}
```

#### **Example: Conflict Error (409)**
```json
{
  "timestamp": 1678886700000,
  "status": 409,
  "error": "Conflict",
  "message": "Error: Username is already taken!",
  "path": "/api/auth/register"
}
```

## 4. Data Models (DTOs)

These are the primary data transfer objects used in the API.

<details>
<summary><strong>RegisterRequest</strong></summary>

| Field | Type | Description | Constraints                 |
| :--- | :--- | :--- |:----------------------------|
| `username` | String | The user's login name. | Required.                   |
| `password` | String | The user's password. | Required, min 6 characters. |
| `nickname` | String | The user's display name. | Required.                   |

**Example:**
```json
{
  "username": "newuser",
  "password": "password123",
  "nickname": "Newbie"
}
```
</details>

<details>
<summary><strong>LoginRequest</strong></summary>

| Field | Type | Description |
| :--- | :--- | :--- |
| `username` | String | The user's login name. |
| `password` | String | The user's password. |

**Example:**
```json
{
  "username": "newuser",
  "password": "password123"
}
```
</details>

<details>
<summary><strong>AuthResponse</strong></summary>

| Field | Type | Description |
| :--- | :--- | :--- |
| `token`| String | JWT for authenticating subsequent requests. |

**Example:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ..."
}
```
</details>

<details>
<summary><strong>PublicKeyUploadRequest</strong></summary>

| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `publicKey`| String | The user's public key in PEM format. | Required, not blank. |

**Example:**
```json
{
  "publicKey": "-----BEGIN PUBLIC KEY-----\nMIIB...IDAQAB\n-----END PUBLIC KEY-----"
}
```
</details>

<details>
<summary><strong>UserPublicKeyResponse</strong></summary>

| Field | Type | Description |
| :--- | :--- | :--- |
| `username` | String | The user's login name. |
| `nickname` | String | The user's display name. |
| `publicKey`| String | The user's public key in PEM format. Can be null if the user hasn't uploaded it yet. |

**Example:**
```json
{
  "username": "otheruser",
  "nickname": "John Doe",
  "publicKey": "-----BEGIN PUBLIC KEY-----\nMIIB...IDAQAB\n-----END PUBLIC KEY-----"
}
```
</details>

<details>
<summary><strong>SendMessageRequest</strong></summary>

| Field | Type | Description |
| :--- | :--- | :--- |
| `receiverUsername` | String | The username of the message recipient. |
| `encryptedContent` | String | The end-to-end encrypted message content. |

**Example:**
```json
{
  "receiverUsername": "otheruser",
  "encryptedContent": "U2FsdGVkX1+..."
}
```
</details>

<details>
<summary><strong>MessageResponse</strong></summary>

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID | The unique identifier for the message. |
| `senderUsername` | String | The username of the sender. |
| `receiverUsername`| String | The username of the receiver. |
| `encryptedContent`| String | The encrypted message content. |
| `timestamp` | Instant | The UTC timestamp of when the message was sent (ISO-8601).|

**Example:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "senderUsername": "currentuser",
  "receiverUsername": "otheruser",
  "encryptedContent": "U2FsdGVkX1+...",
  "timestamp": "2025-07-21T15:02:36.123Z"
}
```
</details>

---

## 5. REST API Endpoints

### 5.1. Authentication

> All endpoints in this section are public.

#### **Register a New User**
*   **Endpoint:** `POST /api/auth/register`
*   **Description:** Creates a new user account. Registration and login are separate steps.
*   **Request Body:** `RegisterRequest`
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** A plain string: `"User registered successfully. Please proceed to login."`
*   **Error Responses:**
    *   `400 Bad Request`: If request validation fails (e.g., password too short).
    *   `409 Conflict`: If the username is already taken.

---
#### **Log In a User**
*   **Endpoint:** `POST /api/auth/login`
*   **Description:** Authenticates a user and returns a JWT.
*   **Request Body:** `LoginRequest`
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** `AuthResponse`
*   **Error Responses:**
    *   `401 Unauthorized`: If credentials are incorrect.

### 5.2. User Management

> All endpoints in this section require authentication.

#### **Upload/Update Current User's X25519 Public Key**
*   **Endpoint:** `PUT /api/users/me/x25519-key`
*   **Description:** Adds or updates the X25519 public key for the currently authenticated user.
*   **Request Body:**
    ```json
    { "x25519PublicKey": "base64string" }
    ```
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** Empty
*   **Error Responses:**
    *   `400 Bad Request`: If `x25519PublicKey` field is blank.
    *   `401 Unauthorized`: If not authenticated.

---
#### **Upload/Update Current User's Ed25519 Public Key**
*   **Endpoint:** `PUT /api/users/me/ed25519-key`
*   **Description:** Adds or updates the Ed25519 public key for the currently authenticated user.
*   **Request Body:**
    ```json
    { "ed25519PublicKey": "base64string" }
    ```
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** Empty
*   **Error Responses:**
    *   `400 Bad Request`: If `ed25519PublicKey` field is blank.
    *   `401 Unauthorized`: If not authenticated.

---
#### **Get a User's X25519 Public Key**
*   **Endpoint:** `GET /api/users/{username}/x25519-key`
*   **Description:** Retrieves the X25519 public key of a specified user.
*   **URL Parameters:**
    *   `username` (string): The username of the target user.
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:**
    ```json
    { "x25519PublicKey": "base64string" }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.
    *   `404 Not Found`: If the specified user does not exist.

---
#### **Get a User's Ed25519 Public Key**
*   **Endpoint:** `GET /api/users/{username}/ed25519-key`
*   **Description:** Retrieves the Ed25519 public key of a specified user.
*   **URL Parameters:**
    *   `username` (string): The username of the target user.
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:**
    ```json
    { "ed25519PublicKey": "base64string" }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.
    *   `404 Not Found`: If the specified user does not exist.

### 5.3. Messaging

> All endpoints in this section require authentication.

#### **Send a Message**
*   **Endpoint:** `POST /api/messages`
*   **Description:** Sends an end-to-end encrypted message to another user via REST API. The sender is identified from the authentication context.
*   **Request Body:** `SendMessageRequest`
*   **Success Response:**
    *   **Code:** `201 Created`
    *   **Body:** `MessageResponse`
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.
    *   `404 Not Found`: If the receiver user does not exist.

---
#### **Get Conversation History**
*   **Endpoint:** `GET /api/messages/{otherUsername}`
*   **Description:** Retrieves the full message history between the authenticated user and another specified user.
*   **URL Parameters:**
    *   `otherUsername` (string): The username of the other participant in the conversation.
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** `List<MessageResponse>` (An array of message objects)
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

### 5.4. Friendships

> All endpoints in this section require authentication.

#### **Send Friend Request**
*   **Endpoint:** `POST /api/friendships/requests`
*   **Description:** Sends a friend request to another user.
*   **Request Body:** 
    ```json
    {
      "addresseeUsername": "string"
    }
    ```
*   **Success Response:**
    *   **Code:** `201 Created`
    *   **Body:** 
    ```json
    {
      "message": "Friend request sent successfully."
    }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

---
#### **Accept Friend Request**
*   **Endpoint:** `PUT /api/friendships/requests/accept`
*   **Description:** Accepts a friend request from another user.
*   **Request Body:** 
    ```json
    {
      "username": "string (requester's username)"
    }
    ```
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** 
    ```json
    {
      "message": "Friend request from {username} accepted.",
      "status": "ACCEPTED"
    }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

---
#### **Decline Friend Request**
*   **Endpoint:** `PUT /api/friendships/requests/decline`
*   **Description:** Declines a friend request from another user.
*   **Request Body:** 
    ```json
    {
      "username": "string (requester's username)"
    }
    ```
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** 
    ```json
    {
      "message": "Friend request from {username} declined.",
      "status": "DECLINED"
    }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

---
#### **Get Friend List**
*   **Endpoint:** `GET /api/friendships/my-friends`
*   **Description:** Retrieves the list of friends for the authenticated user.
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** 
    ```json
    [
      {
        "id": "integer",
        "username": "string",
        "nickname": "string"
      }
    ]
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

---
#### **Unfriend a User**
*   **Endpoint:** `DELETE /api/friendships/unfriend`
*   **Description:** Removes a user from the authenticated user's friend list.
*   **Request Body:** 
    ```json
    {
      "username": "string (friend's username)"
    }
    ```
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** 
    ```json
    {
      "message": "Friend '{username}' removed successfully."
    }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

---
#### **Block a User**
*   **Endpoint:** `POST /api/friendships/block`
*   **Description:** Blocks a user from interacting with the authenticated user.
*   **Request Body:** 
    ```json
    {
      "username": "string (user to block)"
    }
    ```
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** 
    ```json
    {
      "message": "User '{username}' has been blocked."
    }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

---
#### **Unblock a User**
*   **Endpoint:** `POST /api/friendships/unblock`
*   **Description:** Unblocks a previously blocked user.
*   **Request Body:** 
    ```json
    {
      "username": "string (user to unblock)"
    }
    ```
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** `"Successfully unblocked user: {username}"`
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

### 5.5. File Handling

> All endpoints in this section require authentication.

#### **Upload a File**
*   **Endpoint:** `POST /api/files/upload`
*   **Description:** Uploads a file, which can then be sent as a message.
*   **Request:** `multipart/form-data` with a `file` part.
*   **Success Response:**
    *   **Code:** `200 OK`
    *   **Body:** 
    ```json
    {
      "fileName": "string",
      "fileUrl": "string",
      "originalFilename": "string",
      "fileType": "string",
      "size": "long"
    }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If not authenticated.

## 6. WebSocket API (Real-time Messaging)

The application uses **STOMP over WebSocket** for real-time communication.

### 6.1. Connection
Clients must first establish a WebSocket connection. The handshake must include the JWT for authentication.

*   **WebSocket Endpoint:** `ws://localhost:8080/ws`
*   **Authentication:** The JWT should be passed during the STOMP connection phase, typically in a header like `Authorization`. The exact method depends on the client library.

### 6.2. Subscribing (Receiving Messages)
Once connected, the client subscribes to a private queue to receive direct messages.

*   **Subscription Destination:** `/user/queue/messages`
*   **Description:** Messages sent to the authenticated user will be pushed to this destination. The framework automatically resolves this to a user-specific queue.
*   **Received Message Body:** `MessageResponse` (same as the REST API response)

### 6.3. Sending Messages
The WebSocket API does not currently have a public endpoint for sending messages in this design. Sending is handled via the `POST /api/messages` REST endpoint, and the server then pushes the message to the recipient's WebSocket queue.

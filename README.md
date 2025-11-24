# Real-Time Chat App (Spring Boot + Next.js + Keycloak)

A modern, full-stack real-time chat application built with Spring Boot, Next.js, and Keycloak.
The entire system is fully containerized with Docker Compose.

## ✅ Features

Real-time messaging using WebSockets (STOMP)
Secure login via Keycloak + next-auth (OAuth2/OIDC)
Protected frontend pages (/chat)
Backend API & WebSocket secured with JWT
Automatic token refresh
Fully containerized (Frontend, Backend, Keycloak)

## 🧰 Tech Stack

Backend: Spring Boot, Spring Security, WebSocket (STOMP), Java 17
Frontend: Next.js, React, Tailwind CSS, next-auth, Redux Toolkit
Auth: Keycloak
DevOps: Docker & Docker Compose

🚀 Getting Started
1. Clone the repo
git clone https://github.com/Admiral-Simo/opm-socket/
cd opm-socket

2. Run this command
```
docker-compose up --build keycloak
```

3. Set up Keycloak
- Open Keycloak admin console: http://localhost:8180
- Log in with admin/admin
- Create a new realm: chat-app
- Create a new client: chat-app-client
  - Client ID: chat-app-client
  - Client Protocol: openid-connect
  - Root URL: http://localhost:3000
  - Valid Redirect URIs: http://localhost:3000/api/auth/callback/keycloak
  - Web Origins: *

4. Generate NEXTAUTH_SECRET

```bash
openssl rand -base64 32
```

5. Create a .env file in the root directory of the frontend with the following content:

```env

#### - place the output in the NEXTAUTH_SECRET
NEXTAUTH_SECRET=YOUR_GENERATED_32_BYTE_SECRET_GOES_HERE

KEYCLOAK_CLIENT_ID=chat-app-client
#### - copy and paste your client secret from Keycloak
KEYCLOAK_CLIENT_SECRET=**********
#### - keep those as they are
KEYCLOAK_ISSUER=http://localhost:8180/realms/chat-app

NEXTAUTH_URL=http://localhost:3000
```

# access the chat app at http://localhost:3000

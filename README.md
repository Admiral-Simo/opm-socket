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

Frontend: Next.js 14, React 18, Tailwind CSS, next-auth, Redux Toolkit

Auth: Keycloak

DevOps: Docker & Docker Compose

🚀 Getting Started
1. Clone the repo
git clone https://github.com/Admiral-Simo/opm-socket/
cd opm-socket

2. Generate NEXTAUTH_SECRET

openssl rand -base64 32


Place it in docker-compose.yml.

3. Run the entire stack
docker-compose up --build

🌐 Access
Service	URL
Frontend	http://localhost:3000

Backend	http://localhost:8080

Keycloak	http://localhost:8180
 (admin/admin)
✅ Usage

Visit the frontend

Click Sign In

Log in with a test user

Start chatting in real time (open two tabs to test)

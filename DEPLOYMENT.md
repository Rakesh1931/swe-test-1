# EcoCode Cloud Deployment Guide

This guide explains how to deploy the full EcoCode stack (PostgreSQL + Spring Boot Backend + React Frontend) to **Render** or **Railway**.

---

## Architecture Overview

```
[User Browser]
      │
      ▼
[React Frontend] (Render Static Site or Vercel)
      │
      ▼ (HTTPS REST API)
[Spring Boot 3 Backend] (Render Docker Web Service)
      │
      ▼
[PostgreSQL 15 Database] (Render Managed PostgreSQL)
```

---

## Method 1: Deploy on Render (Recommended Free Tier)

Render supports deploying the entire stack in one go using the included `render.yaml` blueprint.

### Step 1: Push your latest code to GitHub
Make sure your latest commits are pushed to your repository:
```bash
git add .
git commit -m "feat: complete ecocode optimizations and deployment config"
git push origin main
```

### Step 2: Connect to Render
1. Go to [render.com](https://render.com) and sign in with GitHub.
2. Click **New +** in the top navigation bar.
3. Select **Blueprint**.
4. Choose your repository: `swe-test-1`.
5. Render will automatically detect `render.yaml` and configure:
   - **Database**: `ecocode-db` (PostgreSQL 15)
   - **Backend**: `ecocode-backend` (Spring Boot 3 / Docker)
   - **Frontend**: `ecocode-frontend` (React Static Site)

### Step 3: Set Frontend API URL
1. Once the blueprint finishes creating services, copy your backend URL (e.g., `https://ecocode-backend-xxxx.onrender.com`).
2. Go to the **ecocode-frontend** service settings on Render.
3. In **Environment Variables**, verify:
   ```
   VITE_API_URL = https://your-backend-name.onrender.com/api
   ```
4. Click **Manual Deploy** -> **Deploy latest commit** on the frontend to rebuild with the live backend URL.

---

## Method 2: Deploy on Railway

1. Go to [railway.app](https://railway.app) and sign in with GitHub.
2. Click **New Project** -> **Provision PostgreSQL**.
3. Click **Add Service** -> **GitHub Repo** -> select `swe-test-1`.
   - Set Root Directory: `/backend`
   - In Variables, add:
     - `SPRING_DATASOURCE_URL` = `${{Postgres.DATABASE_URL}}`
     - `PORT` = `8080`
4. Click **Add Service** -> **GitHub Repo** -> select `swe-test-1`.
   - Set Root Directory: `/frontend`
   - In Variables, add:
     - `VITE_API_URL` = `https://${{backend.RAILWAY_PUBLIC_DOMAIN}}/api`
   - In Settings, click **Generate Domain** under Networking.

---

## Default Production Credentials

When Flyway runs against the empty production database on first startup, it will seed the initial tables and users:
- **Admin**: `admin@ecocode.io`
- **Developer**: `dev@ecocode.io`
- Users can also create their own accounts via the **Register** page.

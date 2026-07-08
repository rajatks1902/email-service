# Email Service & Email Client — Setup Guide

## Architecture Overview

| Component | Tech | Port |
|-----------|------|------|
| `email-service` | Quarkus (Java 17) + Maven | 8080 |
| `email-client` | Angular 17 | 4200 |

The Angular client calls `http://localhost:8080/api/email/send` by default.
The backend reads SMTP credentials from environment variables — **never from code**.

---

## Prerequisites

| Tool | Minimum Version | Check |
|------|----------------|-------|
| Java JDK | 17 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| Angular CLI | 17+ | `ng version` |

Install Angular CLI if missing:
```bash
npm install -g @angular/cli@17
```

---

## Step 1 — Configure the Email Service credentials

The service reads four environment variables at startup. You supply them via a `.env` file (local only — **never committed to Git**).

### 1a. Create the `.env` file

Inside `email-service/`, copy the example and fill in your values:

```bash
# Windows PowerShell
Copy-Item .env.example .env
notepad .env
```

```bash
# Linux / macOS / Git Bash
cp .env.example .env
nano .env        # or code .env / vim .env
```

### 1b. Fill in `.env`

```dotenv
# Your Gmail sender address
MAIL_FROM=yourname@gmail.com

# SMTP host — leave as-is for Gmail
MAIL_HOST=smtp.gmail.com

# SMTP port — 587 for TLS (recommended)
MAIL_PORT=587

# Gmail address (same as MAIL_FROM for personal accounts)
MAIL_USERNAME=yourname@gmail.com

# Gmail App Password — NOT your normal password
# Generate at: https://myaccount.google.com/apppasswords
# Requires 2-Step Verification to be enabled.
# It looks like: abcd efgh ijkl mnop  (16 chars, spaces optional)
MAIL_PASSWORD=your-app-password-here
```

> **How to get a Gmail App Password:**
> 1. Go to [myaccount.google.com/security](https://myaccount.google.com/security)
> 2. Enable **2-Step Verification** if not already done
> 3. Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
> 4. Create a new app password — name it "email-service"
> 5. Copy the 16-character code into `MAIL_PASSWORD`

---

## Step 2 — Run the Email Service locally

### Windows PowerShell

```powershell
cd C:\1OTA\email-service

# Load .env into the current shell session
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}

# Start in dev mode (hot reload)
mvn quarkus:dev
```

### Linux / macOS / Git Bash

```bash
cd /c/1OTA/email-service   # or wherever the project lives

# Load .env and start in dev mode
export $(tr -d '\r' < .env | grep -v '^#' | xargs)
mvn quarkus:dev
```

### Verify it's running

Open [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui) — you should see the Swagger UI with the `/api/email/send` and `/api/email/health` endpoints.

---

## Step 3 — Run the Angular Client

```bash
cd C:\1OTA\email-client

# Install dependencies (first time only)
npm install

# Start dev server
npm start
```

Open [http://localhost:4200](http://localhost:4200).

The client is pre-configured to talk to `http://localhost:8080`.
You can also change the API URL at runtime from the Settings panel in the UI (stored in `localStorage`).

---

## Step 4 — Upload to Git

### email-service repo

```bash
cd C:\1OTA\email-service

git init
git remote add origin https://github.com/YOUR_USERNAME/email-service.git

# Verify .env is in .gitignore (it already is — never remove this line)
git status   # .env must NOT appear here

git add .
git commit -m "Initial commit: Quarkus email microservice"
git push -u origin main
```

### email-client repo

```bash
cd C:\1OTA\email-client

git init
git remote add origin https://github.com/YOUR_USERNAME/email-client.git
git add .
git commit -m "Initial commit: Angular email client"
git push -u origin main
```

---

## Step 5 — Host the Backend (email-service)

Choose one of the free hosting options below.

---

### Option A — Railway (recommended, easiest)

1. Go to [railway.app](https://railway.app) and sign in with GitHub
2. Click **New Project → Deploy from GitHub repo** → select `email-service`
3. Railway auto-detects Maven and builds the Quarkus app
4. Go to **Variables** tab and add:

   | Key | Value |
   |-----|-------|
   | `MAIL_FROM` | yourname@gmail.com |
   | `MAIL_HOST` | smtp.gmail.com |
   | `MAIL_PORT` | 587 |
   | `MAIL_USERNAME` | yourname@gmail.com |
   | `MAIL_PASSWORD` | your-app-password |
   | `PORT` | 8080 |

5. Railway gives you a public URL like `https://email-service-xyz.railway.app`

---

### Option B — Render

1. Go to [render.com](https://render.com) → **New Web Service** → connect GitHub
2. Select `email-service` repo
3. Set:
   - **Build Command:** `mvn package -DskipTests`
   - **Start Command:** `java -jar target/quarkus-app/quarkus-run.jar`
   - **Environment:** Java 17
4. Add the same 5 environment variables under **Environment**
5. Render gives a URL like `https://email-service.onrender.com`

---

### Option C — Docker (self-hosted / any VPS)

The project has a `Dockerfile`. Build and run:

```bash
# Build image
docker build -t email-service .

# Run with env vars
docker run -d -p 8080:8080 \
  -e MAIL_FROM=yourname@gmail.com \
  -e MAIL_HOST=smtp.gmail.com \
  -e MAIL_PORT=587 \
  -e MAIL_USERNAME=yourname@gmail.com \
  -e MAIL_PASSWORD=your-app-password \
  email-service
```

---

## Step 6 — Host the Frontend (email-client)

### Option A — Netlify (free, recommended)

1. Go to [netlify.com](https://netlify.com) → **Add new site → Import from Git**
2. Select `email-client` repo
3. Set:
   - **Build command:** `npm run build:prod`
   - **Publish directory:** `dist/email-client`
4. After deploy, go to **Site Settings → Environment Variables** and add:
   - `NG_APP_API_URL` = `https://your-backend-url` *(optional — users can also set it in the UI)*

### Option B — Vercel

```bash
npm install -g vercel
cd C:\1OTA\email-client
vercel
# Follow prompts — it auto-detects Angular
```

### Point the client at the hosted backend

After hosting the backend, update `src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  defaultApiUrl: 'https://your-email-service-url.railway.app'
};
```

Then rebuild and redeploy.

---

## Quick Reference — Local Run Checklist

```
[ ] Java 17 installed          java -version
[ ] Maven installed            mvn -version
[ ] Node 18+ installed         node -v
[ ] Angular CLI installed      ng version
[ ] .env created               cat email-service/.env
[ ] .env has real values       (not placeholder text)
[ ] email-service running      http://localhost:8080/q/swagger-ui
[ ] email-client running       http://localhost:4200
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `535 Authentication Failed` | Wrong App Password | Re-generate at myaccount.google.com/apppasswords |
| `534 Application-specific password required` | Using regular password | Use App Password, not your Gmail login password |
| `Connection refused :8080` | Service not started | Run `mvn quarkus:dev` in email-service |
| CORS error in browser | Frontend origin blocked | `application.properties` allows `*` by default; check it wasn't changed |
| Angular blank page | `npm install` not run | Run `npm install` in email-client first |

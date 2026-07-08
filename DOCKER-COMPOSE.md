# Docker Compose

Run the backend and frontend together:

```bash
docker compose up --build
```

Open the UI at:

```text
http://localhost:4200
```

The frontend proxies `/api/*` to the backend container. The backend is also
published directly at `http://localhost:8082` by default for Swagger and direct
API checks. Set `HOST_BACKEND_PORT=8080` if local port `8080` is available.

SMTP variables are read from the shell or `.env` file:

```dotenv
MAIL_FROM=yourname@gmail.com
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=yourname@gmail.com
MAIL_PASSWORD=your-app-password
```

If SMTP credentials are not set, the backend queues requests in the
`pending-emails` Docker volume.

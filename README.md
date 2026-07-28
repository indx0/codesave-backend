<div align="center">
  <h1>CodeSave</h1>
  <p>Backend</p>
</div>

---

> 🚧 **Work in Progress**  
> CodeSave is currently under active development. Breaking changes can be expected

## Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Reverse proxy (Caddy)](#reverse-proxy-caddy)
- [Starting the application](#starting-the-application)

## Prerequisites

- Docker and Docker Compose
  If Docker isn't installed yet run:

```shell script
sudo curl -fsSL https://get.docker.com | sh
```
## Installation

### 1. Create required directories

```shell script
sudo mkdir -p /opt/codesave/backend /opt/codesave/db
sudo chown -R 999:999 /opt/codesave/db
```

### 2. Download config files

```shell script
cd /opt/codesave/backend
curl https://raw.githubusercontent.com/indx0/codesave-backend/refs/heads/master/compose-prod.yml -o docker-compose.yml
curl https://raw.githubusercontent.com/indx0/codesave-backend/refs/heads/master/.env.example -o .env
```

### 3. Configure environment

Edit `/opt/codesave/backend/.env`:

```
# Database
DB_HOST=postgres                              # Postgres host
DB_PORT=5432                                  # Postgres port
DB_NAME=backend                               # Postgres database name
DB_USERNAME=app                               # Postgres user
DB_PASSWORD=CHANGE-THE-PASSWORD-PLEASE        # Postgres password

# JWT
JWT_SECRET=CHANGE-THE-SECRET-PLEASE                           # HMAC-SHA key (base64)
JWT_ISSUER=http://localhost:8080                              # Token issuer claim (Backend address)
JWT_ACCESS_TOKEN_EXPIRATION=3600000                           # Access token TTL (ms, default 1h)
JWT_REFRESH_TOKEN_EXPIRATION=2592000000                       # Refresh token TTL (ms, default 30d)

# CORS
FRONTEND_ADDRESS=http://localhost:5173     # Frontend origin allowed by CORS

# Server
LISTEN_ADDRESS=127.0.0.1                  # Bind address (use 127.0.0.1 for local-only)
```


At minimum, change the following before deploying:

| Variable          | What it does                                                                              |
|-------------------|-------------------------------------------------------------------------------------------|
| `DB_PASSWORD`     | Password the backend uses to authenticate with Postgres                                   |
| `JWT_SECRET`      | Signing key used to issue and verify JWT access/refresh tokens                            |
| `JWT_ISSUER`      | URL embedded in tokens identifying which server issued them. Must be backend's public URL |
| `BACKEND_ADDRESS` | Frontend origin the backend allows via CORS                                               |


Generate a secure `JWT_SECRET`:

```shell script
openssl rand -base64 32
```

Paste the output into `JWT_SECRET` in your `.env` file.

### 4. Update the database password in `docker-compose.yml`

Open `/opt/codesave/backend/docker-compose.yml` and set `POSTGRES_PASSWORD` to the **same value** as `DB_PASSWORD` in your `.env` file — these two must match or the backend won't be able to authenticate against Postgres:

## Reverse proxy (Caddy)

I recommend using Caddy as a reverse proxy.

Install it for your distro:

```shell script
# Ubuntu/Debian
sudo apt install -y caddy
 
# Fedora
sudo dnf install -y caddy
 
# Arch Linux
sudo pacman -S caddy
```

Create `/etc/caddy/Caddyfile`:

```
your-domain.com {
    reverse_proxy localhost:8080
}
```

Restart Caddy to apply the config:

```shell script
sudo systemctl restart caddy
```

## Starting the application

```shell script
cd /opt/codesave/backend
docker compose up -d && docker compose logs -f
```

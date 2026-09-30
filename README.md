# Support Ticket Management System

A system for creating, tracking, and resolving customer support tickets.

## Workflow

Requirement → Specification → Plan/Tasks → Implementation → Testing → Review → Fix

## Local database (Docker Compose)

1. From the **repo root** (not `backend/`): `cp .env.example .env` and adjust placeholders if needed.
2. Still at repo root: `docker compose up -d` (data persists in the named volume `support_ticket_pg_data`).
3. Run the API from `backend/` with env vars loaded (e.g. `set -a && source ../.env && set +a` then `mvn spring-boot:run`). URL/username defaults in `application.yml` match `.env.example`; **password must come from `.env`** (empty default in YAML).

Compose is for day-to-day dev. Integration tests use a separate Postgres container (see below).

## Backend tests (`mvn verify`)

`SupportTicketApplicationTest` starts **Postgres 16 via Testcontainers** and loads the full Spring context (DataSource, JPA, Flyway). Compose can be stopped; tests do not use the `support_ticket_pg_data` volume.

**Requires Docker Engine** and access to `/var/run/docker.sock`.

| Check | What it means |
|-------|----------------|
| `getent group docker` lists your username | `usermod` worked |
| `groups` **does not** include `docker` | Normal until you **log out of the OS** or **fully quit and restart Cursor** (terminals inherit groups from when Cursor started) |
| `docker ps` works without `sudo` | Ready for `mvn verify` |

If you are in `docker` per `getent` but `groups` is stale, either restart Cursor / re-login, or use the helper script (runs Maven under `sg docker`):

```bash
cd backend && ./verify.sh
```

Docker **29+** needs API **1.44**; tests pin Testcontainers **1.21.4** and `docker.api.version=1.44` in `src/test/resources/testcontainers.properties`.

```bash
docker compose stop   # optional; tests use their own container
cd backend && mvn verify   # or ./verify.sh
```

Successful runs log Flyway against the ephemeral container (e.g. schema validated / up to date with zero migrations).

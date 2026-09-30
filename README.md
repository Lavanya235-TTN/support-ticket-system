# Support Ticket Management System

A system for creating, tracking, and resolving customer support tickets.

## Workflow

Requirement → Specification → Plan/Tasks → Implementation → Testing → Review → Fix

## Local database (Docker Compose)

1. From the **repo root** (not `backend/`): `cp .env.example .env` and adjust placeholders if needed.
2. Still at repo root: `docker compose up -d` (data persists in the named volume `support_ticket_pg_data`).
3. Run the API from `backend/` with the same env vars (or export `SPRING_DATASOURCE_*` to match `.env`). Defaults in `application.yml` match `.env.example` for localhost.

JPA and Flyway are wired in later tasks; until then the datasource settings are in place for T-03.

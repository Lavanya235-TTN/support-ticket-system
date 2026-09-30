# Codebase map

Short index for agents: **module → purpose → key files**. Update this when structure or major components change.

| Module | Purpose | Key paths |
|--------|---------|-----------|
| `plan/` | Implementation milestones and tasks | `plan/tasks.md` |
| *(root)* | Local PostgreSQL via Compose | `docker-compose.yml`, `.env.example` (copy to git-ignored `.env`) |
| `backend/` | Spring Boot API (Java 21, Maven) | `backend/pom.xml`, `backend/src/main/java/com/supportticket/SupportTicketApplication.java`, layer packages under `com.supportticket.*`, `backend/src/main/resources/application.yml` |
| `frontend/` | Web UI (placeholder layout) | `frontend/` (app code TBD) |
| `spec/` | Requirements and design specs | `spec/requirements.md`, `spec/api-contract.md`, `spec/ui-flow.md`, `spec/test-strategy.md`, `spec/architecture.md`, `spec/data-model.md`, `spec/state-machine.md` |
| `docs/` | Project docs, prompt history, token/context notes | `docs/prompt-history.md`, `docs/token-optimisation.md`, this file |
| `.cursor/` | Cursor rules, commands, skills | `.cursor/rules/`, `.cursor/commands/`, `.cursor/skills/` |

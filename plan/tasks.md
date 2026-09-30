# Implementation Tasks — Support Ticket Management System

**Status:** Planning (derived from `spec/` baselined v1.0).

## How to execute

- **One task per chat.** Run `.cursor/commands/implement-task.md` with a task ID (e.g. `T-04`).
- **One git commit per completed task** (conventional commit message suggested by the command).
- **Tests must pass** for that task’s scope before marking `[x] done`.
- Do not start a task until all **Depends on** tasks are `[x] done`.
- Spec is authoritative; report ambiguities—do not silently change behaviour.

---

## Milestone M0 — Backend skeleton

### T-01 — Maven Spring Boot project layout

- Status: [ ] todo
- Spec refs: `spec/architecture.md` (stack, backend layering); NFR-05
- Depends on: —
- Scope: Create `backend/` Maven project (Java 21, Spring Boot 3.x): `pom.xml`, main application class, package skeleton (`controller`, `service`, `repository`, `domain`, `dto`, `mapper`, `exception`, `config`). **Not yet:** endpoints, JPA entities, Flyway SQL.
- Files: `backend/pom.xml`, `backend/src/main/java/.../Application.java`, `backend/src/main/resources/application.yml` (minimal), `.gitignore` updates if needed
- Done when: `cd backend && mvn -q verify` succeeds (unit tests optional empty); app class loads

### T-02 — Docker Compose, env config, datasource

- Status: [ ] todo
- Spec refs: `spec/architecture.md` (configuration, local topology); NFR-01, NFR-04-AC2; `spec/requirements.md` decision 9
- Depends on: T-01
- Scope: Root `docker-compose.yml` (PostgreSQL 16, **named volume**, reads `.env`); `.env.example` placeholders; `application.yml` uses env vars for datasource. **Not yet:** business logic.
- Files: `docker-compose.yml`, `.env.example`, `backend/src/main/resources/application.yml`, root `.gitignore` (`.env`)
- Done when: Compose starts DB; backend config documented; no secrets in repo

### T-03 — Flyway wired; app starts against Compose DB

- Status: [ ] todo
- Spec refs: `spec/architecture.md` ADR-002; `spec/data-model.md` (migration plan overview); NFR-01
- Depends on: T-02
- Scope: Add Flyway + JPA + PostgreSQL driver; enable Flyway; `ddl-auto=none`; empty or placeholder migration acceptable only if app starts—**prefer** holding V1/V2 SQL for T-06 (document `spring.flyway.enabled` with optional baseline). Minimum: Flyway autoconfig + health; app connects to Compose PostgreSQL. **Not yet:** domain tables (unless minimal smoke migration agreed in task).
- Files: `backend/pom.xml`, `backend/src/main/resources/application.yml`, optional `backend/src/main/resources/db/migration/.gitkeep` or README note
- Done when: With Compose up, `mvn spring-boot:run` in `backend/` starts without error against DB; Flyway runs

---

## Milestone M1 — Domain

### T-04 — TicketStatus state machine (test-first)

- Status: [ ] todo
- Spec refs: `spec/state-machine.md` (full matrix); FR-09, FR-10, NFR-07; `spec/test-strategy.md` (unit matrix)
- Depends on: T-01
- Scope: `TicketStatus` enum with `allowedTargets()` / `canTransitionTo(TicketStatus)` encoding the 5×5 matrix. **Test-first:** parameterized JUnit test for all **25** from→to pairs (5 allowed, 20 rejected). **Not yet:** HTTP, JPA.
- Files: `backend/src/main/java/.../domain/TicketStatus.java`, `backend/src/test/java/.../domain/TicketStatusTest.java`
- Done when: `mvn test` — `TicketStatusTest` passes all 25 cells

### T-05 — TicketPriority enum

- Status: [ ] todo
- Spec refs: `spec/requirements.md` FR-01 (priority); `spec/data-model.md`
- Depends on: T-01
- Scope: `TicketPriority` enum (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`). Small unit test for values. **Not yet:** validation annotations on DTOs.
- Files: `backend/src/main/java/.../domain/TicketPriority.java`, optional test
- Done when: `mvn test` passes

---

## Milestone M2 — Persistence

### T-06 — Flyway V1/V2 migrations

- Status: [ ] todo
- Spec refs: `spec/data-model.md` (tables, indexes, CHECK constraints)
- Depends on: T-03
- Scope: `V1__create_ticket.sql`, `V2__create_ticket_comment.sql` per data model. **Not yet:** Java entities.
- Files: `backend/src/main/resources/db/migration/V1__*.sql`, `V2__*.sql`
- Done when: Flyway applies on app start / test profile; schema matches spec

### T-07 — JPA entities and repositories

- Status: [ ] todo
- Spec refs: `spec/data-model.md` (entity mapping); FR-11 (`@Version` on ticket)
- Depends on: T-06, T-04, T-05
- Scope: `Ticket`, `TicketComment` entities; `TicketRepository`, `TicketCommentRepository`; `@Version` on ticket. **Not yet:** search Specification, services.
- Files: entity + repository classes under `domain`/`repository`
- Done when: `@DataJpaTest` with Testcontainers (or T-08) can persist and load a ticket

### T-08 — Search/filter Specification + repository tests

- Status: [ ] todo
- Spec refs: FR-06, FR-07, FR-08; `spec/api-contract.md` (q OR, status AND); `spec/test-strategy.md`
- Depends on: T-07
- Scope: `TicketSpecification` (or equivalent): keyword trim, title **OR** description case-insensitive literal substring; escape `%`/`_`; status filter; combined AND. `@DataJpaTest` + Testcontainers: FR-06-AC7, FR-07, comment order FR-03-AC2.
- Files: specification class, repository test class
- Done when: `mvn test` — repository tests pass

---

## Milestone M3 — Error handling

### T-09 — Domain exceptions

- Status: [ ] todo
- Spec refs: `spec/architecture.md` (error mapping); FR-04, FR-09, FR-11; decision 11
- Depends on: T-04
- Scope: Exceptions: not found, stale version, invalid transition, terminal ticket edit. **Not yet:** HTTP mapping.
- Files: `backend/src/main/java/.../exception/*.java`
- Done when: Unit tests or compile; used in next task

### T-10 — RFC 7807 Problem Details advice

- Status: [ ] todo
- Spec refs: `spec/api-contract.md` (Problem Details types); NFR-02-AC2; CRR-04; decisions 10–11
- Depends on: T-09
- Scope: `@RestControllerAdvice`, Problem Detail response records, `/problems/*` types; validation `errors[]`; 500 internal without stack trace. `@WebMvcTest` on a stub controller or standalone advice tests.
- Files: exception handler, problem DTOs, tests
- Done when: Tests assert `type`, `status`, `errors` for sample 400/404/409 cases

### T-11 — Unknown JSON properties + path id validation

- Status: [ ] todo
- Spec refs: CRR-01, CRR-03; `spec/api-contract.md` (DTO allow-lists, check order); decision 13
- Depends on: T-10
- Scope: Jackson `FAIL_ON_UNKNOWN_PROPERTIES` on request DTOs; positive-long path `{id}` parsing → 400 before service. Tests for unknown field on create, invalid id `0`/abc.
- Files: `config/Jackson` or `application.yml`, path validator/helper, tests
- Done when: `@WebMvcTest` proves unknown property → 400; invalid path id → 400

---

## Milestone M4 — API endpoints

### T-12 — POST create ticket + GET detail

- Status: [ ] todo
- Spec refs: FR-01, FR-03 (incl. AC3 `allowedTransitions`), CRR-01–CRR-04; `spec/api-contract.md` POST/GET `{id}`
- Depends on: T-08, T-11
- Scope: DTOs (create, detail), mapper, `TicketService` create/get, controller POST `/api/v1/tickets` (201 + Location), GET `/api/v1/tickets/{id}`. `@WebMvcTest`: happy path, validation, 404, detail includes `comments[]`, `allowedTransitions`. **Not yet:** list, PATCH, comments POST.
- Files: controller, service, dto, mapper, web tests
- Done when: `mvn test` — create + get WebMvcTests pass

### T-13 — GET list / search / filter / sort / paging

- Status: [ ] todo
- Spec refs: FR-02, FR-06, FR-07, FR-08, NFR-06; `spec/api-contract.md` GET `/tickets`
- Depends on: T-12
- Scope: Paginated custom page DTO; query params `q`, `status`, `page`, `size`, `sort`; FR-08-AC4 empty over-range page. `@WebMvcTest` for defaults, invalid sort/status/pagination, combined q+status.
- Files: list endpoint, page DTO, tests
- Done when: `mvn test` — list WebMvcTests pass

### T-14 — PATCH ticket fields

- Status: [ ] todo
- Spec refs: FR-04, FR-10, FR-11, CRR; decision 13; `spec/api-contract.md` PATCH `/tickets/{id}`
- Depends on: T-12
- Scope: Partial update with `version`; terminal → 409 terminal-ticket; stale → 409 stale-version; FR-04-AC9. `@WebMvcTest` coverage.
- Files: update DTO, service method, controller, tests
- Done when: `mvn test` — PATCH WebMvcTests pass

### T-15 — PATCH status transition

- Status: [ ] todo
- Spec refs: FR-09, FR-09-AC11, FR-10, `spec/state-machine.md`; `spec/api-contract.md` PATCH `/tickets/{id}/status`
- Depends on: T-12, T-04
- Scope: Transition in **service** using `TicketStatus.canTransitionTo`; 200 full TicketDetail; invalid/same-status → 409 invalid-transition. `@WebMvcTest` for allowed OPEN→IN_PROGRESS and rejected case.
- Files: transition DTO, service, controller, tests
- Done when: `mvn test` — status WebMvcTests pass

### T-16 — POST comment

- Status: [ ] todo
- Spec refs: FR-05, FR-10, CRR; `spec/api-contract.md` (201 body only, no Location)
- Depends on: T-12
- Scope: POST `/api/v1/tickets/{id}/comments`; ticket version unchanged; whitespace validation FR-05. `@WebMvcTest`: 201, 404, 400 check-order cases.
- Files: comment DTO, service, controller, tests
- Done when: `mvn test` — comment WebMvcTests pass

---

## Milestone M5 — Integration tests

### T-17 — NFR-07 state machine 25-cell integration suite

- Status: [ ] todo
- Spec refs: NFR-07-AC1–AC3; `spec/state-machine.md`; FR-09
- Depends on: T-15
- Scope: `@SpringBootTest` + Testcontainers; parameterized HTTP tests for all 25 matrix cells.
- Files: `backend/src/test/java/.../integration/StatusTransitionMatrixIT.java` (or similar)
- Done when: `mvn verify` — matrix IT passes

### T-18 — Locking + check-order integration tests

- Status: [ ] todo
- Spec refs: FR-11 (incl. AC3), decision 13; `spec/test-strategy.md` check-order table; `spec/api-contract.md` examples
- Depends on: T-14, T-15, T-16
- Scope: Integration tests: stale PATCH/transition; bad body missing ticket → 400; valid body missing ticket → 404; terminal stale vs current version 409 types.
- Files: integration test class(es)
- Done when: `mvn verify` passes

### T-19 — End-to-end happy path integration test

- Status: [ ] todo
- Spec refs: FR-01, FR-03, FR-04, FR-05, FR-09; assignment “core” flows
- Depends on: T-17
- Scope: Single IT: create → list → get → PATCH field → transitions OPEN→…→CLOSED → comment on terminal.
- Files: `TicketHappyPathIT.java`
- Done when: `mvn verify` passes

---

## Milestone M6 — Frontend skeleton

### T-20 — Vite + React + TypeScript scaffold

- Status: [ ] todo
- Spec refs: `spec/architecture.md` ADR-001; NFR-05
- Depends on: T-19 (API stable) — *or parallel after T-16 if UI against mock; prefer T-19 for real API*
- Scope: `frontend/` Vite React TS app; lint/test scripts. **Not yet:** routes, API calls.
- Files: `frontend/package.json`, `vite.config.ts`, `src/main.tsx`, etc.
- Done when: `npm run build` succeeds

### T-21 — Router, TanStack Query, API client, proxy

- Status: [ ] todo
- Spec refs: `spec/ui-flow.md` (routes); `spec/api-contract.md`; NFR-03
- Depends on: T-20
- Scope: React Router routes shell; TanStack Query provider; typed fetch client for `/api/v1`; parse RFC 7807 Problem Details; Vite dev proxy `/api` → backend:8080.
- Files: `frontend/src/api/*`, `frontend/vite.config.ts`, router setup
- Done when: Dev server starts; proxy config present; unit test for problem parser optional

---

## Milestone M7 — Frontend screens

### T-22 — Ticket list screen

- Status: [ ] todo
- Spec refs: FR-12-AC2, AC6, AC7; FR-02, FR-06, FR-07, FR-08; `spec/ui-flow.md` list section
- Depends on: T-21, T-13
- Scope: `/tickets` — search debounce, status filter, pagination, New ticket link. **Not yet:** create/detail.
- Files: list page/components, hooks
- Done when: Manual or component test: list loads from API

### T-23 — Create ticket screen

- Status: [ ] todo
- Spec refs: FR-12-AC1, AC9; FR-01; `spec/ui-flow.md` create section
- Depends on: T-22
- Scope: `/tickets/new` — form, POST, navigate to detail on 201, field errors on 400.
- Files: create page/components
- Done when: Component test: submit → navigate; Vitest passes

### T-24 — Detail: edit, transitions, comments, errors

- Status: [ ] todo
- Spec refs: FR-12-AC3–AC5, AC8, AC10–AC14; FR-03–FR-05, FR-04, FR-09; `spec/ui-flow.md` detail + error mapping
- Depends on: T-23, T-14, T-15, T-16
- Scope: `/tickets/:id` — non-numeric id → not-found without API; PATCH, status buttons from `allowedTransitions`, comments, terminal disabled edit, Problem Details banners.
- Files: detail page/components
- Done when: Component tests for terminal, stale banner, 404; `npm run test` passes

### T-25 — Frontend component test coverage gap fill

- Status: [ ] todo
- Spec refs: `spec/test-strategy.md` frontend table; NFR-03, NFR-05
- Depends on: T-24
- Scope: Vitest + Testing Library: list debounce mock, error mapping cases not covered in T-24.
- Files: `*.test.tsx`
- Done when: `npm run test` passes

---

## Milestone M8 — Hardening

### T-26 — OpenAPI vs api-contract alignment

- Status: [ ] todo
- Spec refs: `spec/api-contract.md` (springdoc note); NFR-02
- Depends on: T-16
- Scope: springdoc annotations; document or script diff checklist against api-contract paths/DTOs/status codes.
- Files: controller annotations, optional `docs/openapi-checklist.md`
- Done when: `/v3/api-docs` reviewed; gaps documented or fixed

### T-27 — README and secrets hygiene

- Status: [ ] todo
- Spec refs: NFR-04; `spec/architecture.md`; README structure in documentation skill
- Depends on: T-02, T-21
- Scope: Root README: run backend/frontend/DB, test commands, structure; confirm `.env` gitignored, `.env.example` only placeholders.
- Files: `README.md`, verify `.gitignore`
- Done when: New developer can follow README; no secrets in tree

### T-28 — Manual acceptance + final review-code

- Status: [ ] todo
- Spec refs: NFR-01-AC2; `spec/test-strategy.md` manual checklist; requirements §6 assignment criteria; `/review-code`
- Depends on: T-25, T-27
- Scope: Execute manual NFR-01 restart checklist; walk all **15** assignment criteria; run `.cursor/commands/review-code` on backend + frontend; fix or log issues.
- Files: optional `docs/acceptance-log.md`
- Done when: Checklist signed off in task notes or acceptance log; review-code findings addressed or waived with reason

---

## Coverage tables

### Task → primary FR/NFR mapping

| Task | FR / NFR / CRR |
|------|----------------|
| T-01 | NFR-05 |
| T-02 | NFR-01, NFR-04 |
| T-03 | NFR-01 |
| T-04 | FR-09, FR-10, NFR-07 |
| T-05 | FR-01 |
| T-06 | — (schema) |
| T-07 | FR-11 |
| T-08 | FR-02, FR-03-AC2, FR-06, FR-07, FR-08 |
| T-09 | FR-04, FR-09, FR-11 |
| T-10 | NFR-02, CRR-04, decisions 10–11 |
| T-11 | CRR-01, CRR-03, decision 13 |
| T-12 | FR-01, FR-03, CRR |
| T-13 | FR-02, FR-06, FR-07, FR-08, NFR-06 |
| T-14 | FR-04, FR-10, FR-11 |
| T-15 | FR-09, FR-09-AC11, FR-10 |
| T-16 | FR-05, FR-10 |
| T-17 | NFR-07 |
| T-18 | FR-11, decision 13 |
| T-19 | FR-01–FR-05, FR-09 |
| T-20–T-21 | FR-12 (infra), NFR-03 |
| T-22 | FR-12-AC2, AC6, AC7, FR-02, FR-06, FR-07, FR-08 |
| T-23 | FR-12-AC1, AC9, FR-01 |
| T-24 | FR-12-AC3–AC5, AC8, AC10–AC14, FR-03–FR-05, FR-04, FR-09, NFR-03 |
| T-25 | NFR-03, NFR-05 |
| T-26 | NFR-02 |
| T-27 | NFR-04 |
| T-28 | NFR-01-AC2, all assignment criteria, NFR-07 |

### FR/NFR → tasks (every ID covered)

| ID | Task(s) |
|----|---------|
| CRR-01–CRR-04 | T-11, T-12–T-16, T-18 |
| FR-01 | T-05, T-12, T-19, T-23 |
| FR-02 | T-08, T-13, T-22 |
| FR-03 | T-08, T-12, T-19, T-24 |
| FR-04 | T-09, T-14, T-18, T-19, T-24 |
| FR-05 | T-16, T-19, T-24 |
| FR-06 | T-08, T-13, T-22 |
| FR-07 | T-08, T-13, T-22 |
| FR-08 | T-08, T-13, T-22 |
| FR-09 | T-04, T-15, T-17, T-19, T-24 |
| FR-10 | T-04, T-14, T-15, T-16, T-24 |
| FR-11 | T-07, T-14, T-15, T-18 |
| FR-12 | T-21–T-25, T-28 |
| NFR-01 | T-02, T-03, T-28 |
| NFR-02 | T-10, T-11, T-12–T-16, T-26 |
| NFR-03 | T-21, T-24, T-25 |
| NFR-04 | T-02, T-27 |
| NFR-05 | T-01, T-04, T-25, all test tasks |
| NFR-06 | T-13 |
| NFR-07 | T-04, T-17, T-28 |

### Assignment core acceptance criteria (15) → tasks

| # | Criterion | Task(s) |
|---|-----------|---------|
| 1 | Ticket can be created from UI | T-23, T-28 |
| 2 | Tickets can be listed | T-13, T-22, T-28 |
| 3 | Ticket details can be viewed | T-12, T-24, T-28 |
| 4 | Ticket fields can be updated | T-14, T-24, T-28 |
| 5 | Assignee can be changed | T-14, T-24, T-28 |
| 6 | Comments can be added | T-16, T-24, T-28 |
| 7 | Search works | T-08, T-13, T-22, T-28 |
| 8 | Status filter works | T-08, T-13, T-22, T-28 |
| 9 | Valid status transitions work | T-15, T-17, T-24, T-28 |
| 10 | Invalid status transitions rejected by backend | T-04, T-15, T-17, T-28 |
| 11 | Data survives application restart | T-02, T-03, T-28 (NFR-01-AC2 manual) |
| 12 | Backend validation works | T-10, T-11, T-12–T-16, T-28 |
| 13 | UI shows meaningful errors | T-24, T-25, T-28 |
| 14 | State-machine integration tests pass | T-17, T-28 |
| 15 | No secrets are committed | T-02, T-27, T-28 |

**Confirmation:** Every FR-01–FR-12, NFR-01–NFR-07, CRR-01–CRR-04, and all **15** assignment core acceptance criteria are covered by at least one task above.

---

## Change Log

| Date | Version | Summary |
|------|---------|---------|
| 2026-09-30 | 1.0.0 | Initial task breakdown M0–M8 (T-01–T-28) from baselined spec v1.0. |

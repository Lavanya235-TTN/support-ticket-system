# API Contract — Support Ticket Management System

**Document status:** Baselined v1.0 — ready for planning.

## Purpose

Define the HTTP API consumed by the web UI and integration tests so implementation, springdoc-openapi output, and requirements stay aligned.

## Scope

- In scope: base path `/api/v1`, six ticket/comment operations, list/search/filter on one endpoint, pagination DTO, sort whitelist, RFC 7807 errors, mutation check order.
- Out of scope: authentication, OpenAPI YAML export policy, production hostnames (use relative paths in examples).

## Content

### Base URL and conventions

| Rule | Value |
|------|--------|
| Base path | `/api/v1` |
| JSON field names | camelCase |
| Enums in JSON | `UPPER_SNAKE_CASE` strings (`OPEN`, `MEDIUM`, …) |
| Timestamps | ISO-8601 UTC with offset `Z` (e.g. `2026-09-30T10:15:30Z`) |
| `Content-Type` | `application/json` for bodies |
| Ticket id in path | Positive **long** (`1`, `42`); `0`, negatives, decimals, non-numeric → **400** (CRR-01); valid id, missing ticket → **404** (CRR-02) |

**Live OpenAPI:** springdoc-openapi serves generated OpenAPI 3 at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`. **This document is normative**; generated annotations and schemas must match it.

### Common request rules

Applies to every endpoint unless overridden below. Traceability: CRR-01–CRR-04, decision 13.

| Rule | Behaviour | HTTP |
|------|-----------|------|
| CRR-01 | Path `{id}` does not parse as a positive long | 400, validation-error |
| CRR-02 | Path `{id}` parses as a positive long but no ticket exists | 404, not-found |
| CRR-03 | JSON body contains unknown or forbidden properties (e.g. `status` on create, `id` / `createdAt` / `updatedAt` / `version` on create; `status` on PATCH ticket) | 400, validation-error; no partial apply |
| CRR-04 | Bean Validation or business field rules fail | 400, validation-error with `errors[]` |

Malformed JSON (unparseable body) → **400** validation-error with a synthetic field (e.g. `_body`) or top-level `detail`.

### Mutation check order (decision 13)

For **PATCH** `/tickets/{id}`, **PATCH** `/tickets/{id}/status`, and **POST** `/tickets/{id}/comments`:

1. **400** — request validation: malformed path `{id}` (CRR-01), malformed JSON, CRR-03 unknown properties, CRR-04 / `@Valid` failures (including missing `version`, version-only PATCH, invalid enums).
2. **404** — ticket not found (CRR-02).
3. **409** — stale `version` (FR-11, FR-09-AC9).
4. **409** — domain conflict: **terminal-ticket** (PATCH fields on `CLOSED`/`CANCELLED`) or **invalid-transition** (PATCH status only).

Rationale: Bean Validation on `@Valid` request DTOs runs at the controller **before** the service loads the ticket (Spring’s natural order).

**Examples:**

| Request | Outcome |
|---------|---------|
| POST `/tickets/999/comments` with invalid body (missing `author`) | **400** (body validation before load) |
| POST `/tickets/999/comments` with valid body, ticket id 999 absent | **404** |
| PATCH `/tickets/{id}` on terminal ticket with **stale** `version` | **409** stale-version |
| PATCH `/tickets/{id}` on terminal ticket with **current** `version` | **409** terminal-ticket |

**POST** `/tickets` has no path id; order is **400** validation only.

**GET** list/detail: invalid query params → **400**; missing ticket on detail → **404** (after CRR-01 on id).

### Data transfer objects

#### TicketSummary

Used in paginated list responses. Never includes `description` or `comments`.

| Field | Type | Notes |
|-------|------|--------|
| `id` | number | System id |
| `title` | string | |
| `status` | string | Ticket status enum |
| `priority` | string | Priority enum |
| `assignee` | string \| null | |
| `createdAt` | string | ISO-8601 UTC |
| `updatedAt` | string | ISO-8601 UTC |
| `version` | number | Optimistic lock (integer ≥ 0) |

#### Comment

| Field | Type | Notes |
|-------|------|--------|
| `id` | number | |
| `author` | string | max 100 |
| `body` | string | |
| `createdAt` | string | ISO-8601 UTC |

#### TicketDetail

Extends summary semantics with detail-only fields.

| Field | Type | Notes |
|-------|------|--------|
| *(all TicketSummary fields)* | | |
| `description` | string | |
| `comments` | Comment[] | **Always present**; empty array when none; **ascending** `createdAt` (FR-03-AC2) |
| `allowedTransitions` | string[] | Target statuses allowed from **current** `status` per `spec/state-machine.md`; empty for terminal states |

#### PaginatedTicketSummaryPage

Custom page DTO — **do not** serialize Spring `Page` directly (NFR-06, FR-08).

| Field | Type | Notes |
|-------|------|--------|
| `content` | TicketSummary[] | May be empty |
| `page` | number | Zero-based index (FR-08) |
| `size` | number | Page size used for this request |
| `totalElements` | number | Total matching rows |
| `totalPages` | number | `ceil(totalElements / size)`; `0` when `totalElements` is `0` |

#### Request bodies

Only properties listed below are accepted per DTO. **Any other JSON property → 400** (CRR-03).

| DTO | Allowed properties | Notes |
|-----|-------------------|--------|
| **CreateTicketRequest** | `title`, `description`, `priority`, `assignee` | `priority` / `assignee` optional; no `status` |
| **UpdateTicketRequest** | `version`, `title`, `description`, `priority`, `assignee` | Required `version`; ≥1 mutable field besides `version` (FR-04-AC9); no `status` |
| **TransitionRequest** | `version`, `status` | Target status only |
| **CreateCommentRequest** | `author`, `body` | No ticket `version` |

### List, search, and filter (single endpoint)

**GET** `/api/v1/tickets` — list, keyword search, and status filter combined (FR-02, FR-06, FR-07, FR-08). **No separate search endpoint.**

| Query param | Required | Default | Rules |
|-------------|----------|---------|--------|
| `q` | No | — | Keyword: omitted, blank, or whitespace-only → **no** keyword filter (FR-06-AC5). Otherwise trim; ticket matches if keyword is a case-insensitive **literal** substring of **title OR description** (FR-06-AC1–AC3); `%` and `_` matched literally (FR-06-AC6, FR-06-AC7). Combined with `status` filter using **AND** (FR-06-AC4). |
| `status` | No | — | Single status enum; omitted, blank, or whitespace-only → no status filter (FR-07-AC4). Invalid value → **400** (FR-07-AC2). |
| `page` | No | `0` | Integer ≥ 0; `< 0` or non-numeric → **400** (FR-08-AC3) |
| `size` | No | `20` | Integer 1–100; otherwise **400** (FR-08-AC3) |
| `sort` | No | `createdAt,desc` | See sort whitelist below |

When `page` is beyond the last page → **200** with `content: []` and consistent pagination metadata (FR-08-AC4).

### Sort whitelist

Parameter `sort` is a single string: `{field},{direction}`.

| Field | Allowed directions |
|-------|-------------------|
| `createdAt` | `asc`, `desc` |
| `updatedAt` | `asc`, `desc` |
| `title` | `asc`, `desc` |

Default when omitted: `createdAt,desc` (FR-08-AC2).

Any other field name, missing direction, invalid direction, or malformed string → **400** validation-error (field `sort`).

### Problem Details (RFC 7807)

All error responses use `application/problem+json`.

**Common properties:** `type`, `title`, `status`, `detail`, `instance` (request path or URI).

**Type URI base:** Relative to the API deployment; normative path suffixes below. Implementations may use absolute URIs (e.g. `https://localhost:8080/problems/validation-error`) with the same path suffix.

| Suffix | HTTP | When |
|--------|------|------|
| `/problems/validation-error` | 400 | CRR-01, CRR-03, CRR-04, invalid query params, invalid transition target enum (FR-09-AC10), PATCH/transition validation |
| `/problems/not-found` | 404 | CRR-02 |
| `/problems/stale-version` | 409 | FR-11, FR-09-AC9 |
| `/problems/invalid-transition` | 409 | FR-09-AC6, FR-09-AC7, FR-10-AC3 |
| `/problems/terminal-ticket` | 409 | FR-04-AC3, FR-04-AC6, FR-10-AC4 |
| `/problems/internal-error` | 500 | Unhandled server error; **no** stack traces or internal details in the body (NFR-02) |

Validation errors (**400**) include:

```json
"errors": [
  { "field": "title", "message": "must not be blank" }
]
```

Field paths use JSON property names (e.g. `assignee`, `status`, `sort`, `page`).

**Example — stale version (409):**

```json
{
  "type": "/problems/stale-version",
  "title": "Conflict",
  "status": 409,
  "detail": "Ticket was modified by another request. Reload and retry.",
  "instance": "/api/v1/tickets/42"
}
```

### Endpoints

#### POST `/api/v1/tickets`

Create ticket (FR-01).

| | |
|--|--|
| **FR / NFR** | FR-01, CRR-03, CRR-04, NFR-02 |
| **Request** | CreateTicketRequest |
| **Success** | **201 Created** |
| **Response body** | TicketDetail with `status`: `OPEN`, `version`: `0`, `comments`: `[]`, `allowedTransitions` computed for `OPEN` |
| **Headers** | `Location: /api/v1/tickets/{id}` |

**Example request:**

```json
{
  "title": "Cannot log in",
  "description": "Password reset email never arrives.",
  "priority": "HIGH",
  "assignee": "Alex"
}
```

**Example response (201):**

```json
{
  "id": 1,
  "title": "Cannot log in",
  "description": "Password reset email never arrives.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "Alex",
  "createdAt": "2026-09-30T06:00:00Z",
  "updatedAt": "2026-09-30T06:00:00Z",
  "version": 0,
  "comments": [],
  "allowedTransitions": ["IN_PROGRESS", "CANCELLED"]
}
```

| Status | Condition |
|--------|-----------|
| 201 | Created |
| 400 | Validation / forbidden properties |

---

#### GET `/api/v1/tickets`

Paginated list with optional search and filter (FR-02, FR-06, FR-07, FR-08, NFR-06).

| | |
|--|--|
| **FR / NFR** | FR-02, FR-06, FR-07, FR-08, NFR-06 |
| **Query** | `q`, `status`, `page`, `size`, `sort` (see above) |
| **Success** | **200 OK**, body PaginatedTicketSummaryPage |

**Example:** `GET /api/v1/tickets?q=login&status=OPEN&page=0&size=20&sort=createdAt,desc`

**Example response (200):**

```json
{
  "content": [
    {
      "id": 1,
      "title": "Cannot log in",
      "status": "OPEN",
      "priority": "HIGH",
      "assignee": "Alex",
      "createdAt": "2026-09-30T06:00:00Z",
      "updatedAt": "2026-09-30T06:00:00Z",
      "version": 0
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

| Status | Condition |
|--------|-----------|
| 200 | Success (including empty list / over-range page) |
| 400 | Invalid `status`, pagination, or `sort` |

---

#### GET `/api/v1/tickets/{id}`

Ticket detail (FR-03).

| | |
|--|--|
| **FR / NFR** | FR-03, CRR-01, CRR-02 |
| **Success** | **200 OK**, TicketDetail |

**Example response (200):**

```json
{
  "id": 1,
  "title": "Cannot log in",
  "description": "Password reset email never arrives.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "Alex",
  "createdAt": "2026-09-30T06:00:00Z",
  "updatedAt": "2026-09-30T06:00:00Z",
  "version": 0,
  "comments": [],
  "allowedTransitions": ["IN_PROGRESS", "CANCELLED"]
}
```

| Status | Condition |
|--------|-----------|
| 200 | Found |
| 400 | Invalid id (CRR-01) |
| 404 | Not found (CRR-02) |

---

#### PATCH `/api/v1/tickets/{id}`

Partial field update — not status (FR-04, FR-10, FR-11).

| | |
|--|--|
| **FR / NFR** | FR-04, FR-10, FR-11, CRR-01–CRR-04, decision 13 |
| **Request** | UpdateTicketRequest |
| **Success** | **200 OK**, TicketDetail (fresh `version`, `updatedAt`, `allowedTransitions`) |

**Example request:**

```json
{
  "version": 0,
  "title": "Cannot log in — SSO",
  "assignee": ""
}
```

| Status | Condition |
|--------|-----------|
| 200 | Updated |
| 400 | CRR-01, CRR-03, CRR-04, missing/extra-only `version` |
| 404 | CRR-02 |
| 409 | stale-version or terminal-ticket |

---

#### PATCH `/api/v1/tickets/{id}/status`

Status transition (FR-09, FR-10, FR-11, `spec/state-machine.md`).

| | |
|--|--|
| **FR / NFR** | FR-09, FR-10, FR-11, NFR-07, decision 13 |
| **Request** | TransitionRequest — `{ "version": <number>, "status": "<TARGET>" }` |
| **Success** | **200 OK**, **full TicketDetail** (FR-09 success; same shape as GET detail) |

**Example request:**

```json
{
  "version": 0,
  "status": "IN_PROGRESS"
}
```

| Status | Condition |
|--------|-----------|
| 200 | Allowed transition |
| 400 | CRR-03, CRR-04, missing `version`, invalid target `status` |
| 404 | CRR-02 |
| 409 | stale-version or invalid-transition |

---

#### POST `/api/v1/tickets/{id}/comments`

Append comment (FR-05, FR-10).

| | |
|--|--|
| **FR / NFR** | FR-05, FR-10, CRR-01–CRR-04 |
| **Request** | CreateCommentRequest |
| **Success** | **201 Created**, Comment body only (**no** `Location` header — documented exception to `.cursor/rules/api-standards.mdc`; no GET-single-comment endpoint) |

Ticket `version` and `updatedAt` **unchanged** (FR-05-AC1).

**Example request:**

```json
{
  "author": "Sam",
  "body": "Checked spam folder — still nothing."
}
```

**Example response (201):**

```json
{
  "id": 10,
  "author": "Sam",
  "body": "Checked spam folder — still nothing.",
  "createdAt": "2026-09-30T07:00:00Z"
}
```

| Status | Condition |
|--------|-----------|
| 201 | Created |
| 400 | CRR-01, CRR-03, CRR-04 |
| 404 | CRR-02 |

### `allowedTransitions` computation

Backend derives `allowedTransitions` from current `status` using the allowed edges in `spec/state-machine.md`:

| Current status | `allowedTransitions` |
|----------------|----------------------|
| `OPEN` | `IN_PROGRESS`, `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED`, `CANCELLED` |
| `RESOLVED` | `CLOSED` |
| `CLOSED` | `[]` |
| `CANCELLED` | `[]` |

Order in the array is not significant; UI may sort for display.

### Traceability — endpoint → requirements

| Endpoint | FR | NFR / other |
|----------|-----|-------------|
| POST `/tickets` | FR-01 | NFR-02, CRR-03, CRR-04 |
| GET `/tickets` | FR-02, FR-06, FR-07, FR-08 | NFR-06 |
| GET `/tickets/{id}` | FR-03 | CRR-01, CRR-02 |
| PATCH `/tickets/{id}` | FR-04, FR-10, FR-11 | CRR-*, decision 13 |
| PATCH `/tickets/{id}/status` | FR-09, FR-10, FR-11 | NFR-07, state-machine.md, decision 13 |
| POST `/tickets/{id}/comments` | FR-05, FR-10 | CRR-* |

## Open Questions

None.

## Change Log

| Date | Version | Author | Summary |
|------|---------|--------|---------|
| 2026-09-30 | 0.1.0 | — | Initial API contract: endpoints, DTOs, pagination, sort, Problem Details, check order, traceability; resolves requirements §7 deferred API items. |
| 2026-09-30 | 1.0.0 | — | Review patch: check order, path id, q OR/AND, DTO allow-lists, comment 201 without Location; baselined v1.0. |

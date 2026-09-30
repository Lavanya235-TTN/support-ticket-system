Implement a single task from the project plan.

**Usage:** Provide a task ID, e.g. `T-04`.

## Steps

### a. Load context (minimal)

1. Read **`plan/tasks.md`** and locate the section `### <TASK_ID> — …` (status, spec refs, depends on, scope, files, done when).
2. Read **only** the spec files/sections listed under **Spec refs** for that task (under `spec/`).
3. Read **`docs/codebase-map.md`** for current layout.

Do not read unrelated spec sections or application code outside the task scope.

### b. Dependency gate

- For each task in **Depends on**, confirm its **Status** is `[x] done` in `plan/tasks.md`.
- If any dependency is not done, **stop** and tell the user which tasks must be completed first.

### c. Plan and wait

- Propose a **short implementation plan**: files to create/change, main classes, tests to add.
- Respect **Scope** and **Not yet** boundaries in the task.
- Follow `.cursor/rules/` (Java/Spring, testing, API standards).
- **Wait for user approval** before writing code.

### d. Implement

- Implement the approved plan in one focused change set (~2–5 files where possible).
- Write or update **tests in the same task** as specified in **Done when**.
- No auth, attachments, delete ticket, or other out-of-scope features.

### e. Verify

- Run the **Done when** commands (e.g. `cd backend && mvn test`, `npm run test`).
- Show command output summary.
- On failure: fix code **without weakening or deleting tests** to green the build.

### f. Spec ambiguity

- If the spec required interpretation, list each point in the final report (**do not** silently change behaviour or spec).

### g. Close out

1. In **`plan/tasks.md`**, set this task’s **Status** to `[x] done`.
2. Update **`docs/codebase-map.md`** if modules, packages, or key paths changed.
3. Append a row to **`docs/prompt-history.md`** (next `#`, today’s date, Phase: Implementation, one-line prompt summary, outcome).

### h. Commit message

Suggest a **conventional commit** message for the user, e.g.:

```text
feat(backend): add TicketStatus 25-cell unit tests (T-04)
```

Do **not** run `git commit` unless the user explicitly asks.

# Token and context optimisation

## Why not the suggested plugins?

We planned to use **codebase-memory-mcp**, **caveman**, and **graphify**, but they could not be installed in this environment (tooling and network restrictions). The setup below uses built-in Cursor features and project files instead.

## Replacements

| Plugin | Role | Built-in / project replacement |
|--------|------|--------------------------------|
| **codebase-memory-mcp** | Persistent memory of where things live | Cursor codebase indexing (respecting `.cursorignore`) + [`docs/codebase-map.md`](codebase-map.md) maintained by the agent |
| **caveman** | Shorter agent replies | [`.cursor/rules/concise-output.mdc`](../.cursor/rules/concise-output.mdc) (`alwaysApply: true`) |
| **graphify** | Structural / relationship views | Mermaid diagrams in `spec/` + the module table in `docs/codebase-map.md` |

## Practices

- **`.cursorignore`** — Keeps build artifacts, dependencies, logs, lockfiles, and `.specstory/` out of AI context and indexing. Keeps `spec/`, `docs/`, `.cursor/`, and application source paths available.
- **`.cursor/rules/context-management.mdc`** — One task at a time, search and map before bulk reads, minimal diffs in chat, update the codebase map after structural changes.
- **New chat per phase or task** — Avoids carrying irrelevant transcript tokens.
- **@-mention specific files** — Pull only what you need instead of whole trees.
- **Reusable commands** — Use `.cursor/commands/` (e.g. `/log-prompt`, `/review-spec`) instead of retyping long prompts.

## Related files

- [`.cursorignore`](../.cursorignore)
- [`.cursor/rules/context-management.mdc`](../.cursor/rules/context-management.mdc)
- [`.cursor/rules/concise-output.mdc`](../.cursor/rules/concise-output.mdc)
- [`docs/codebase-map.md`](codebase-map.md)

---
name: documentation
description: Writes and updates specs, README, ADRs, API docs, and prompt history. Use when creating or changing spec files, README.md, architecture decision records, API documentation, or docs/prompt-history.md.
---

# Documentation

## Spec files

Spec files live in `spec/`. Each spec has:

- Purpose
- Scope
- Content
- Open Questions
- Change Log

## Diagrams

Use Mermaid for diagrams (architecture, ER, state machine, sequence/UI flows).

## Requirements

Requirements get IDs (`FR-01`, `NFR-01`) and acceptance criteria in Given/When/Then.

## Traceability

Every task and test should reference a requirement ID.

## Keep docs in sync

If code changes behaviour, update the matching spec in the same change.

## README structure

- Overview
- Tech stack
- Prerequisites
- How to run (backend, frontend, DB)
- How to test
- Project structure
- AI workflow used

## Style

Plain, concise language. No marketing tone.

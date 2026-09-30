Review the given files or diff against `.cursor/rules` and `spec/`. Check:

- Layering violations
- Business logic in controllers
- State machine enforced only in the backend service/domain
- Validation coverage
- Error handling matches api-standards
- N+1 queries
- Transaction boundaries
- Hardcoded secrets
- Missing tests
- Deviations from `api-contract.md`

Output: Severity | Location | Issue | Fix.

Do not edit unless I confirm.

# Active Tasks

- [x] Project Foundation
  - Build RDS-driven dashboard, shared role selector, DB mappings, and module skeletons.
  - Source of truth: `docs/workflows/RDS main.docx` and `sql/DBFinora.sql`.
  - No schema changes allowed in foundation phase.

- [x] Package Reorganization
  - Java source is organized by feature-owned packages under `src/java`.
  - Shared infrastructure lives under `src/java/common`.
  - Team members should work mainly inside their assigned feature package.

- [ ] Module Implementation
  - Team members can now claim isolated module packages/controllers/services.

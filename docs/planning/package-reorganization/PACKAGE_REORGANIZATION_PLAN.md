# Package Reorganization Implementation Plan

## Scope

Reorganize Java source under `src/java` so each feature owns its controller, DAO, model, service, and DTO code in one package tree.

## Current-State Analysis

Before this change, source was organized by horizontal layers:

```text
controller/
dao/
dto/
model/
service/
util/
```

This caused team members implementing one feature to touch multiple top-level packages.

## Affected Modules

All Java modules were affected because package declarations and imports changed. JSP source and SQL schema were not moved.

## Protected-Area Impact

- `web/WEB-INF/web.xml` servlet/listener class names were updated.
- `DatabaseUtil` moved to `common.util.DatabaseUtil`.
- Role utilities moved to `common.util` and role-selection controller moved to `auth.controller`.
- No SQL behavior, credential behavior, or business workflow behavior was intentionally changed.

## Implementation Steps

1. Move shared infrastructure to `common`.
2. Move dashboard and skeleton routing to `dashboard` and `foundation`.
3. Move auth/role files to `auth` plus shared role utilities to `common`.
4. Move each business feature into its own package.
5. Update Java package declarations and imports.
6. Update `web.xml` listener and servlet-class references.
7. Update governance documentation.
8. Run compile verification.

## Validation Strategy

- Search for old package references in Java source.
- Run Java compile smoke test with Tomcat 10.1 API jars.
- Manually verify dashboard and skeleton routes in Tomcat/NetBeans when available.

## Documentation Updates

Updated architecture, folder structure, dependency flow, module boundaries, coding standards, naming conventions, module index, and status docs.

## Open Questions

- Whether JSPs should be reorganized into feature view folders later, module-by-module.
- Whether future JSON APIs should be centralized under `api` or feature-owned under each module.

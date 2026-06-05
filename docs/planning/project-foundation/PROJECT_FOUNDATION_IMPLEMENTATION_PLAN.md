# Project Foundation Implementation Plan

## Scope

Create the RDS-driven project foundation: shared dashboard, shared Role Selector, DB-mapped models/DAOs, module controllers/services, JSP skeletons, and shared assets.

## Current-State Analysis

The project was nearly empty: NetBeans Ant web project, placeholder index page, DBFinora.sql schema, and RDS main.docx requirements.

## Affected Modules

All RDS modules receive skeleton entry points. Shared infrastructure includes role context, permissions, module registry, and DB connection utility.

## Protected-Area Impact

Authentication, authorization, database utility, and payment/finance areas are foundation skeletons only. Production behavior is left as TODO and no schema is changed.

## Implementation Steps

1. Add DB-mapped models and DAO skeletons.
2. Add shared role context and permission utilities.
3. Add module registry from RDS screen list.
4. Add dashboard, role selector, skeleton JSPs, and assets.
5. Add servlet mappings.
6. Validate compilation/build.

## Validation Strategy

Run Ant compile/dist where local Tomcat/Jakarta libraries are configured. Manually verify dashboard and role switching in Tomcat.

## Documentation Updates

- Update ACTIVE_TASKS.md.
- Add walkthrough after implementation.

## Open Questions

Future implementation should decide whether to migrate to Maven structure or keep NetBeans Ant layout.

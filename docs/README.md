# KiotRetail Engineering Documentation

This directory is the persistent engineering brain for KiotRetail / SWP391_Finora. It helps future AI agents and developers understand the system, preserve boundaries, plan safely, and evolve the codebase without re-discovering architecture context from scratch.

## Source Of Truth

- Java source: `src/java/`
- Web UI source: `web/`
- JSP views: `web/WEB-INF/views`
- Servlet configuration: `web/WEB-INF/web.xml`
- Tomcat context: `web/META-INF/context.xml`
- Database source: `sql/DBFinora.sql`
- Build/dependency source: `build.xml`, `nbproject/project.properties`, and `lib/`
- AI/developer operating rules: `AGENTS.md` and `docs/rules`

Generated artifacts under `build/` and `dist/` are not source of truth.

## Documentation Index

- `architecture/`: current architecture, module boundaries, dependency flow, folder structure
- `rules/`: mandatory coding, naming, AI workflow, refactoring, and protected module rules
- `planning/`: roadmap, active tasks, backlog, and future implementation plans
- `status/`: current implementation status, completed features, and technical debt
- `patterns/`: reusable implementation patterns for controllers, DAOs, repositories, and services
- `security/`: authentication, authorization, session, secret, and data handling rules
- `database/`: database architecture and schema governance
- `api/`: future API routing and response standards
- `features/`: feature index and feature-level documentation
- `modules/`: module ownership and responsibility index
- `decisions/`: architecture decision records
- `workflows/`: repeatable engineering workflows
- `references/`: external references and project context
- `frontend/`: JSP/CSS/JavaScript UI conventions
- `backend/`: servlet/DAO/model backend conventions
- `devops/`: build, deploy, and environment conventions
- `testing/`: verification and test strategy

## Maintenance Rule

When source behavior, package layout, build flow, or deployment configuration changes, update the smallest relevant documentation file in the same change. Documentation drift is treated as technical debt.

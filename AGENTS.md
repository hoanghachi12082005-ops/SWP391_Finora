# AGENTS.md

This file is the operating contract for every AI agent and developer working in this repository. Follow it before changing code, planning features, reviewing pull requests, or modifying architecture.

## Project Identity

- Project: KiotRetail
- Repository/runtime name: SWP391_Finora
- Type: NetBeans Ant Java WAR web application
- Runtime: Apache Tomcat 10.1 with Jakarta Servlet/JSP APIs
- Java level: JDK 17
- Architecture style: Layered MVC with JSP views, servlet controllers, DAO-based JDBC persistence, SQL Server schema scripts, and lightweight development module routing.
- Primary Java packages: `controller`, `dao`, `dto`, `model`, `service`, `util`
- Java source root: `src/java`
- Web source root: `web`
- Generated outputs: `build/`, `dist/`

## Mandatory First Step

Before implementing any non-trivial change, inspect the existing repository state:

1. Read relevant files under `src/java/`.
2. Read relevant JSP views under `web/WEB-INF/views`.
3. Read relevant SQL under `sql` before changing persistence behavior.
4. Read root and deployment configuration, especially `build.xml`, `web/WEB-INF/web.xml`, `web/META-INF/context.xml`, `nbproject/project.properties`, `.gitignore` if present, and Tomcat context files.
5. Check governance docs under `docs` for boundaries, standards, status, and prior decisions.

Generated output under `build/` and `dist/` is build artifact output. Do not treat generated output as source of truth and do not edit it directly.

## Architecture Contract

- Controllers are servlets in `controller` and should coordinate request parsing, validation, redirects, forwards, and response status only.
- Database access belongs in DAO classes under `dao`.
- Models under `model` are data carriers for domain entities used by JSP/server-side flows.
- DTOs under `dto` carry view/module response data where domain models are not appropriate.
- Service classes under `service` are currently lightweight skeletons. Add real service logic only for shared or multi-DAO workflows.
- JSP files under `web/WEB-INF/views` render UI and should not contain database access or heavy business logic.
- Shared cross-cutting behavior belongs in focused utility classes under `util` only when reuse is proven.
- Future JSON API or filter packages may be added only when the implementation requires them and docs are updated in the same change.

## Protected Areas

Treat the following as protected modules:

- Authentication and session flow: future/login-related servlets, `RoleSelectionServlet`, auth/session JSPs, and role-selection routing.
- Authorization: `RolePermissionUtil`, role-based session attributes, protected management/POS URL mappings when implemented.
- Database infrastructure: `DatabaseUtil`, `web.xml` database parameters, SQL schema files under `sql`.
- Payment and finance schema areas: `Payments`, `FinanceTransaction`, order/payment tables, and future payment service code.
- Shared infrastructure: Tomcat/NetBeans Ant build configuration, `build.xml`, `nbproject/project.properties`, `web.xml`, and `context.xml`.

Do not modify protected areas unless the user explicitly asks or the active task cannot be completed safely without doing so. If modification is required, document impact and keep the change minimal.

## Engineering Rules

- Prefer the smallest correct change.
- Reuse existing DAO/model/controller patterns before adding new abstractions.
- Do not create generic utility files unless at least two real call sites need them.
- Do not put SQL in servlets, JSPs, filters, API actions, or service skeletons.
- Do not access request/session objects from DAOs.
- Do not hardcode secrets, passwords, tokens, or production credentials.
- Do not edit `build/`, `dist/`, `.git`, IDE private state, or generated artifacts.
- Keep naming consistent with existing package conventions.
- Keep methods small enough to review and test.
- Update documentation when architecture, boundaries, workflow, status, or patterns change.
- Preserve UTF-8 without BOM for Java, JSP, XML, and Markdown files.

## Planning Workflow

Feature plans must be stored under `docs/planning/<topic>/` using deterministic uppercase names such as `INVOICE_IMPLEMENTATION_PLAN.md` or `AUTH_REFACTOR_PLAN.md`.

Every plan must include:

- Scope
- Current-state analysis
- Affected modules
- Protected-area impact
- Implementation steps
- Validation strategy
- Documentation updates
- Open questions

Update `docs/planning/ACTIVE_TASKS.md`, `docs/planning/BACKLOG.md`, or `docs/planning/ROADMAP.md` when relevant.

## Mem0 Workflow

Before large features, search long-term memory for related rules, architecture decisions, protected modules, security decisions, and reusable patterns.

After major decisions, suggest storing concise memories using categories:

- `[RULE]`
- `[ARCH]`
- `[PATTERN]`
- `[BOUNDARY]`
- `[SECURITY]`
- `[WORKFLOW]`
- `[DECISION]`

Do not store temporary task noise.

## Required Validation

For source changes, run the narrowest useful verification first.

Preferred full project verification when Ant is available:

```powershell
ant clean dist
```

If Ant is not available, use a Java compile smoke test with the local Tomcat jars:

```powershell
javac -encoding UTF-8 -cp "C:/Tomcat 10.1_Tomcat/lib/servlet-api.jar;C:/Tomcat 10.1_Tomcat/lib/jsp-api.jar;C:/Tomcat 10.1_Tomcat/lib/el-api.jar" -d build/check-classes <all java files>
```

Remove temporary compile output such as `build/check-classes` after the smoke test unless it is needed for debugging.

For servlet/JSP/config changes, also manually verify in Tomcat or NetBeans when possible. If verification cannot run locally, state the reason and residual risk.

## Documentation Map

- `docs/architecture`: system shape, dependency flow, folder structure, module boundaries
- `docs/rules`: coding standards, naming, refactor policy, AI workflow, protected modules
- `docs/planning`: roadmap, active tasks, backlog, feature plans
- `docs/status`: current status, implemented features, technical debt
- `docs/patterns`: reusable controller, DAO, service, repository, and API patterns
- `docs/security`: authentication, authorization, secrets, session, and data protection rules
- `docs/database`: schema and persistence architecture
- `docs/api`: future JSON API conventions and standards
- `docs/features`, `docs/modules`, `docs/decisions`, `docs/workflows`, `docs/references`, `docs/frontend`, `docs/backend`, `docs/devops`, `docs/testing`: supporting governance indexes

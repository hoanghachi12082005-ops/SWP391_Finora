# AI Workflow Rules

## Mandatory Repository Analysis

Before implementation, AI agents must inspect relevant existing code and docs. For broad architecture work, inspect:

- `src/java/`
- `web/WEB-INF/views`
- `web/WEB-INF/web.xml`
- `web/META-INF/context.xml`
- `sql/DBFinora.sql`
- `build.xml`
- `nbproject/project.properties`
- `AGENTS.md`
- relevant files under `docs`

Generated `build/` and `dist/` output is not source of truth and must not be edited directly.

## Mem0 Usage

Before large features or protected module work, search Mem0 for:

- architecture decisions
- reusable patterns
- protected module constraints
- security decisions
- prior implementation decisions

After major decisions, suggest adding concise long-term memories using categories such as `[ARCH]`, `[RULE]`, `[PATTERN]`, `[BOUNDARY]`, `[SECURITY]`, `[WORKFLOW]`, or `[DECISION]`.

## Planning Requirements

When asked to create a plan, roadmap, module analysis, implementation steps, or task organization:

1. Create a dedicated markdown file under `docs/planning/<topic>/`.
2. Use deterministic uppercase naming for technical plans.
3. Update `docs/planning/ACTIVE_TASKS.md`, `docs/planning/BACKLOG.md`, or `docs/planning/ROADMAP.md` when relevant.
4. Link related architecture, status, module, or security docs.
5. Preserve planning history rather than overwriting it.

## Implementation Requirements

- Keep changes scoped to the task.
- Reuse current controller/DAO/model/DTO/util patterns before adding new ones.
- Do not edit generated `build/` or `dist/` output.
- Do not modify unrelated modules.
- Preserve UTF-8 without BOM for Java, JSP, XML, and Markdown files.
- Update documentation for architecture, security, database, API, planning, status, build, or deployment changes.
- Explain residual risks if validation cannot run.

## Verification Requirements

- Prefer `ant clean dist` when Ant is available in PATH.
- If Ant is unavailable, run a `javac` smoke compile with Tomcat 10.1 servlet/JSP jars.
- For servlet/JSP changes, manually verify through NetBeans/Tomcat when possible.
- Remove temporary compile output after validation unless needed for debugging.

## Review Requirements

When reviewing code, prioritize:

- security issues
- broken dependency boundaries
- database/schema mismatches
- authentication/authorization regressions
- data loss or transaction risks
- build/deploy compatibility with NetBeans Ant and Tomcat 10.1
- missing validation and tests
- documentation drift

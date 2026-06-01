# Folder Structure

## Repository Structure

```text
SWP391_Finora/
├── AGENTS.md
├── build.xml
├── lib/
├── nbproject/
├── sql/
│   └── DBFinora.sql
├── src/
│   ├── conf/
│   └── java/
│       ├── controller/
│       ├── dao/
│       ├── dto/
│       ├── model/
│       ├── service/
│       └── util/
├── test/
├── web/
│   ├── META-INF/
│   │   └── context.xml
│   ├── WEB-INF/
│   │   ├── web.xml
│   │   └── views/
│   ├── assets/
│   └── index.html
├── docs/
├── build/   # generated
└── dist/    # generated WAR output
```

## Java Source Structure

| Path | Purpose |
| --- | --- |
| `src/java/controller` | Servlet controllers for server-rendered page flows |
| `src/java/dao` | JDBC data access objects |
| `src/java/dto` | View/module DTOs and other non-domain data carriers |
| `src/java/model` | Domain entity/data carrier classes |
| `src/java/service` | Service skeletons and future multi-DAO workflow services |
| `src/java/util` | Focused utility classes used across layers |

No root package such as `com.kiotretail` is used. Java packages are direct module packages such as `controller`, `dao`, and `model`.

## Web Structure

| Path | Purpose |
| --- | --- |
| `web/WEB-INF/web.xml` | Servlet/listener/session/database configuration |
| `web/WEB-INF/views` | JSP views rendered through servlet forwards |
| `web/WEB-INF/views/common` | Shared JSP fragments |
| `web/assets` | CSS, JavaScript, images, and static assets |
| `web/META-INF/context.xml` | Tomcat context path configuration |
| `web/index.html` | Welcome/static entry page |

## Build Structure

| Path | Purpose |
| --- | --- |
| `build.xml` | NetBeans Ant entry point |
| `nbproject/project.properties` | Source roots, WAR name, Java level, Tomcat classpath references |
| `lib/` | Project library metadata and copy-libs support |
| `build/` | Generated build output; do not edit directly |
| `dist/SWP391_Finora.war` | Generated WAR output; do not edit directly |

## Database Structure

| Path | Purpose |
| --- | --- |
| `sql/DBFinora.sql` | Primary SQL Server database creation and seed script |

## Documentation Structure

The governance documentation lives under `docs`:

```text
docs/
├── api/
├── architecture/
├── backend/
├── database/
├── decisions/
├── devops/
├── features/
├── frontend/
├── modules/
├── patterns/
├── planning/
├── references/
├── rules/
├── security/
├── status/
├── testing/
└── workflows/
```

## Generated And Ignored Structure

- `build/` is Ant/NetBeans generated output. Do not edit.
- `dist/` is Ant/NetBeans generated WAR output. Do not edit.
- `.git/` is repository metadata. Do not edit.
- IDE private state should not be changed unless the task explicitly requires local environment configuration.

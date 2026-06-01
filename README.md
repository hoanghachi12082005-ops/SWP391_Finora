# SWP391_Finora

KiotRetail / Finora NetBeans Ant Java WAR web application.

## Tech stack

- Java 17
- Jakarta Servlet/JSP APIs
- Apache Tomcat 10.1
- NetBeans Ant project
- SQL Server

## Project structure

- `src/java/` - Java source code using layered MVC packages.
- `web/` - JSP views, static web resources, and WEB-INF configuration.
- `sql/` - Database schema/scripts when present.
- `docs/` - Architecture, planning, rules, status, and workflow documentation.
- `lib/` - Project library metadata and bundled helper libraries.

## Build

Preferred full build when Apache Ant is available:

```powershell
ant clean dist
```

Generated outputs are written to `build/` and `dist/` and are intentionally ignored by Git.

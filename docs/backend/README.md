# Backend Standards

## Current Backend

The backend uses Jakarta Servlet APIs, JSP forwarding, JDBC DAOs, SQL Server, and NetBeans Ant build/deploy configuration.

Current Java packages live directly under `src/java`:

- `controller`
- `dao`
- `dto`
- `model`
- `service`
- `util`

## Backend Rules

- Controllers handle HTTP request/response flow.
- DAOs handle SQL and result mapping.
- Models carry domain data.
- DTOs carry view/module data that should not be represented as domain entities.
- Utilities must be focused and reused.
- Services are currently skeletons; add real service logic only for complex multi-DAO workflows or shared business logic.
- Do not access SQL directly from JSPs, servlets, or service skeletons.

## Build Baseline

Preferred full verification when Ant is available:

```powershell
ant clean dist
```

If Ant is not available in PATH, run a Java compile smoke test with Tomcat 10.1 jars:

```powershell
javac -encoding UTF-8 -cp "C:/Tomcat 10.1_Tomcat/lib/servlet-api.jar;C:/Tomcat 10.1_Tomcat/lib/jsp-api.jar;C:/Tomcat 10.1_Tomcat/lib/el-api.jar" -d build/check-classes <all java files>
```

Run the narrowest useful command first and remove temporary `build/check-classes` output after smoke testing.

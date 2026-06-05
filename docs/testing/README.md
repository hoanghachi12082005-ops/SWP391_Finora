# Testing Strategy

## Current State

The repository has a NetBeans Ant web-project structure. No complete automated test suite is currently visible.

## Verification Baseline

Preferred full verification when Ant is available:

```powershell
ant clean dist
```

For Java-only smoke verification when Ant is unavailable:

```powershell
javac -encoding UTF-8 -cp "C:/Tomcat 10.1_Tomcat/lib/servlet-api.jar;C:/Tomcat 10.1_Tomcat/lib/jsp-api.jar;C:/Tomcat 10.1_Tomcat/lib/el-api.jar" -d build/check-classes <all java files>
```

After the smoke check, remove temporary output:

```powershell
Remove-Item -Recurse -Force build/check-classes
```

For servlet/JSP/config changes, also run or manually verify the application through NetBeans/Tomcat when possible.

## Testing Priorities

1. Utility tests for deterministic helpers such as role permissions and module registry behavior.
2. Controller validation tests where feasible.
3. DAO integration tests against a controlled SQL Server test database.
4. Authentication and authorization regression tests as those flows are implemented.
5. Future API response shape tests if JSON endpoints are added.

## Protected Regression Areas

- Role selection and permission checks.
- Database connection and schema compatibility.
- Servlet mappings in `web.xml`.
- Tomcat context path in `web/META-INF/context.xml`.
- Payment, finance, order, and inventory workflows once implemented.

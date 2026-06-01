# DevOps Notes

## Current Build

The project is a NetBeans Ant Java WAR project.

Primary command when Ant is available:

```powershell
ant clean dist
```

Configured WAR output:

```text
dist/SWP391_Finora.war
```

Relevant build configuration:

- `build.xml`: Ant entry point generated/managed by NetBeans.
- `nbproject/project.properties`: source roots, Java level, WAR name, Tomcat platform classpath.
- `lib/`: NetBeans library metadata and copy-libs support.

## Runtime

- Expected server: Apache Tomcat 10.1.
- Java level: JDK 17.
- Context path: `/SWP391_Finora` from `web/META-INF/context.xml`.
- Servlet/JSP configuration: `web/WEB-INF/web.xml`.
- Database: SQL Server database `DBFinora` on local development environment.

## Local Verification Fallback

If Ant is unavailable in PATH, use `javac` with Tomcat jars as a smoke test for Java source compilation:

```powershell
javac -encoding UTF-8 -cp "C:/Tomcat 10.1_Tomcat/lib/servlet-api.jar;C:/Tomcat 10.1_Tomcat/lib/jsp-api.jar;C:/Tomcat 10.1_Tomcat/lib/el-api.jar" -d build/check-classes <all java files>
```

Delete `build/check-classes` after the check unless debugging requires it.

## DevOps Debt

- No CI workflow is currently documented.
- No environment-specific configuration strategy is currently implemented.
- Database credentials are configured in source/config and must be externalized before production use.
- Ant is not available in PATH in the observed local shell; NetBeans can still provide Ant build integration.
- JSP compilation is not part of the fallback `javac` smoke test.

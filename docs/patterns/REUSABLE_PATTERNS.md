# Reusable Patterns

## Servlet POST Redirect Pattern

Use POST for mutations, set a session flash message, then redirect to avoid duplicate form submission.

```java
request.getSession().setAttribute("message", "Operation completed.");
request.getSession().setAttribute("messageType", "success");
response.sendRedirect(request.getContextPath() + "/example-management");
```

## Servlet GET Forward Pattern

Use GET methods to load view data and forward to JSPs under `web/WEB-INF/views`.

```java
request.setAttribute("items", items);
request.getRequestDispatcher("/WEB-INF/views/example/page.jsp").forward(request, response);
```

## Module Skeleton Forward Pattern

Module skeleton servlets can extend `SkeletonModuleServlet` and provide the module route key.

```java
package controller;

public class ProductManagementServlet extends SkeletonModuleServlet {
    public ProductManagementServlet() {
        super("/product-management");
    }
}
```

## DAO Query Pattern

Use try-with-resources, `PreparedStatement`, and private mapper methods.

```java
try (Connection conn = DatabaseUtil.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setInt(1, id);
    try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
            return extractEntity(rs);
        }
    }
}
```

## Role Check Pattern

Use `RolePermissionUtil` rather than string comparisons inside JSPs or scattered controllers.

```java
String role = RoleContextUtil.getCurrentRole(request);
boolean allowed = RolePermissionUtil.canAccessRoute(role, route);
```

## Future API Action Pattern

When a real API package is introduced, keep action/router logic small, parse request parameters defensively, delegate to DAO/service code, and return a consistent response envelope.

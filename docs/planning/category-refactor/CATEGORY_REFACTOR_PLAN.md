# Category Refactor Plan

## Scope

Refactor only the category module code from the completed `category/` bundle into the official source tree.

Out of scope:

- `category/ProductServlet.java`
- product module behavior
- database schema changes
- authentication, authorization, payment, and finance modules

## Current-State Analysis

The completed category bundle contained a working model, DAO, and JSP. The official module under `src/java/category` and `web/WEB-INF/views/category-management` contained skeleton files.

The refactor keeps the existing category business behavior while replacing skeleton code with readable implementation in the official source locations.

## Affected Modules

- `src/java/category/model/Category.java`
- `src/java/category/dao/CategoryDAO.java`
- `src/java/category/controller/CategoryManagementServlet.java`
- `src/java/category/service/CategoryManagementService.java`
- `web/WEB-INF/views/category-management/index.jsp`
- `web/WEB-INF/web.xml`
- `docs/refactor-review/`

## Protected-Area Impact

`web/WEB-INF/web.xml` is shared infrastructure. The change is intentionally small: `/category-management` is handled by the concrete category servlet instead of the generic skeleton servlet.

## Implementation Steps

1. Move completed category behavior into official source files.
2. Keep product servlet out of scope.
3. Use `/category-management` as the category route.
4. Improve naming, comments, and method structure without changing category business rules.
5. Add refactor-review documentation.
6. Build or compile-check the project.

## Validation Strategy

- Prefer `ant clean dist`.
- If Ant is unavailable, run a JDK/Tomcat `javac` smoke compile.
- Manually verify `/category-management` in Tomcat when a local database is available.

## Documentation Updates

- `docs/refactor-review/CATEGORY_MODULE_REVIEW.md`
- `docs/refactor-review/REFATOR_SUMMARY.md`

## Open Questions

None. User confirmed:

- Do not refactor `ProductServlet.java`.
- Use existing route `/category-management`.

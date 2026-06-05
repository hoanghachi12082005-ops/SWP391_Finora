# Frontend Standards

## Current Frontend

The frontend is server-rendered JSP with CSS and JavaScript assets under `web/assets`.

## JSP Rules

- Keep pages under `web/WEB-INF/views` so they are rendered through controllers.
- Use `common` fragments for shared header, footer, role selector, and repeated layout markup.
- Keep business rules out of JSP files.
- Do not instantiate DAOs or execute SQL in JSPs.
- Prefer request attributes for page data and session attributes for role/session state.
- Preserve UTF-8 without BOM.

## Asset Rules

- Reuse existing CSS and JavaScript under `web/assets` before adding new files.
- Keep JavaScript in `web/assets/js` when scripts are needed.
- Do not hardcode API base URLs; respect `pageContext.request.contextPath` or equivalent context-path handling.
- Design must remain responsive and accessible as module pages become real features.

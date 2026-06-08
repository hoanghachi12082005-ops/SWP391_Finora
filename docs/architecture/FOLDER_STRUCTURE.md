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
│       ├── common/
│       ├── dashboard/
│       ├── foundation/
│       ├── auth/
│       ├── product/
│       ├── category/
│       ├── customer/
│       ├── supplier/
│       ├── sales/
│       ├── order/
│       ├── payment/
│       └── ... feature packages
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

Java source uses feature-owned packages under `src/java`.

| Path | Purpose |
| --- | --- |
| `src/java/common` | Shared DTOs, utilities, and web startup infrastructure |
| `src/java/dashboard` | Development dashboard controller |
| `src/java/foundation` | Shared skeleton module controller |
| `src/java/auth` | Authentication, authorization, role-selection source |
| `src/java/product` | Product controller, DAO, model, service |
| `src/java/category` | Category controller, DAO, model, service |
| `src/java/customer` | Customer controller, DAO, model, service |
| `src/java/supplier` | Supplier controller, DAO, model, service |
| `src/java/sales` | Sales/POS controller and service skeletons |
| `src/java/order` | Order and order-detail DAO/model/controller/service source |
| `src/java/payment` | Payment controller, DAO, model, service |
| `src/java/finance` | Finance transaction controller, DAO, model, service |
| `src/java/inventory` | Inventory controller and service skeletons |
| `src/java/warehouse` | Warehouse and warehouse transaction source |
| `src/java/stocktransfer` | Stock transfer controller, DAO, model, service |
| Other feature packages | Branch, employee, invoice, report, website, audit, notification, system config |

Within each feature package, use subpackages as needed:

```text
<feature>/controller
<feature>/dao
<feature>/model
<feature>/dto
<feature>/service
```

## Web Structure

| Path | Purpose |
| --- | --- |
| `web/WEB-INF/web.xml` | Servlet/listener/session/database configuration |
| `web/WEB-INF/views` | JSP views rendered through servlet forwards |
| `web/WEB-INF/views/common` | Shared JSP fragments |
| `web/assets` | CSS, JavaScript, images, and static assets |
| `web/META-INF/context.xml` | Tomcat context path configuration |
| `web/index.html` | Welcome/static entry page |

JSPs may be moved into feature view folders gradually when each feature gets a real UI. Do not mass-move JSPs unless the feature plan requires it.

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

## Generated And Ignored Structure

- `build/` is Ant/NetBeans generated output. Do not edit.
- `dist/` is Ant/NetBeans generated WAR output. Do not edit.
- `.git/` is repository metadata. Do not edit.
- IDE private state should not be changed unless the task explicitly requires local environment configuration.

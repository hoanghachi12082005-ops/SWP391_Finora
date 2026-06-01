# Engineering Workflows

## Standard Change Workflow

1. Read `AGENTS.md`.
2. Read relevant architecture/rules/status docs.
3. Inspect existing code before editing.
4. Identify protected modules and dependency boundaries.
5. Make the smallest correct change.
6. Preserve UTF-8 without BOM.
7. Run targeted verification.
8. Update docs if behavior, boundaries, patterns, build flow, or status changed.

## Feature Planning Workflow

1. Create `docs/planning/<topic>/<TOPIC>_IMPLEMENTATION_PLAN.md`.
2. Include current-state analysis, scope, affected modules, protected impact, steps, validation, documentation updates, and open questions.
3. Update planning indexes when relevant.
4. Search Mem0 before implementation if the feature is large or protected.
5. Suggest long-term memories after major decisions.

## Protected Module Workflow

1. Confirm the protected area in `docs/rules/PROTECTED_MODULES.md`.
2. Read all route mappings, session usage, DAOs, JSPs, SQL, and build/deploy files touched by the change.
3. Document risks before or during implementation.
4. Keep the change minimal.
5. Run `ant clean dist` or a documented fallback verification.
6. Document residual risk when full verification is unavailable.

## NetBeans Ant Verification Workflow

Preferred:

```powershell
ant clean dist
```

Fallback when Ant is unavailable in PATH:

```powershell
javac -encoding UTF-8 -cp "C:/Tomcat 10.1_Tomcat/lib/servlet-api.jar;C:/Tomcat 10.1_Tomcat/lib/jsp-api.jar;C:/Tomcat 10.1_Tomcat/lib/el-api.jar" -d build/check-classes <all java files>
Remove-Item -Recurse -Force build/check-classes
```

Use NetBeans Clean and Build for full WAR packaging when local shell Ant is unavailable.

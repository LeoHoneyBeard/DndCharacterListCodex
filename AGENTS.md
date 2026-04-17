# Project AGENTS Overlay

Extends:
- see `AGENTS_PATHS.md` for the extended file list

Project-local rules in this file take priority for `DndCharacterListCodex`.

## Architecture Target

Target module shape:
- `:app`
- `:core:ui`
- `:core:navigation`
- `:core:data`
- `:core:domain`
- `:core:rules`
- `:feature:character-list`
- `:feature:character-detail`
- `:feature:character-editor`
- `:feature:character-creation`
- `:feature:character-level-up`

This is the default refactor direction. Small tasks do not need to create all modules immediately, but changes should move toward this shape, not away from it.

## Structure

- Use feature ownership. Each screen or flow belongs to a specific `:feature:*` module.
- Do not keep unrelated screens in a shared `ui/*` package once a feature module exists for that area.
- `:app` is the application shell and composition root only.
- `:core:navigation` owns shared navigation contracts and helpers.
- `:core:rules` owns reusable rules logic.
- `:core:data` owns infrastructure and persistence implementations.
- `:core:domain` owns shared domain models, contracts, and use cases.

## Feature Layout

Default shape for `:feature:*` modules:
- `presentation` is required.
- `domain` is required when the feature has meaningful business logic, validation, calculations, or orchestration.
- `data` is required only when the feature owns data sources or repository implementations.

Use the lightest structure that keeps boundaries clear. Do not create empty layers just to satisfy a template.

## Navigation

- `:core:navigation` is the shared navigation module.
- Each feature should expose its own route contract and graph entry.
- `:app` assembles the root graph instead of defining all feature routes inline.
- Prefer explicit route contracts over scattered raw route strings.

Hard rules:
- Do not keep all navigation definitions in a single app-level file once a feature graph exists.
- Do not make screens aware of unrelated feature destinations.

## Rules-Driven Flows

These rules apply to character creation, editing, progression, and any other flow that depends on D&D rules content.

- `:core:rules` is the source of truth for progression, class milestones, subclass unlocks, spell progression, and similar system rules.
- Inspect the relevant rules data before coding a rules-driven flow.
- If a transition unlocks a mandatory player choice, that choice must be modeled explicitly before persistence succeeds.
- Do not persist a partially advanced but rules-invalid character state.
- Reminder text is acceptable only for optional follow-up, never for mandatory progression choices.
- If rules support is incomplete, stop at a clear seam: surface the limitation, keep persistence conservative, and report the gap explicitly.

Examples:
- A progression flow must handle mandatory subclass selection when the new state grants a subclass and the character does not already have one.
- A progression flow must handle other required class selections from the active ruleset before applying persistence.
- Character creation and editing must validate class-dependent fields against the active ruleset, not hardcoded assumptions.

## Dependency Injection

Target DI standard is Koin.

- New dependency wiring should move toward Koin modules.
- `:app` initializes Koin and registers the app composition root.
- Do not instantiate repositories, databases, rules engines, or long-lived services inside composables, screens, or `NavHost`.
- Do not create feature dependencies manually inside navigation destinations except as a temporary migration seam that is documented in the final report.

## Presentation

- Presentation should expose `UiState`.
- For simple screens, `UiState + intent methods` on the `ViewModel` is acceptable.
- For complex flows, use `UiAction` / `UiEvent` or an equivalent one-off effect model.

Treat a flow as complex when it includes:
- multiple steps;
- form validation;
- one-off effects such as snackbar, dialog, or navigation events;
- multiple async sources;
- non-trivial orchestration in the `ViewModel`;
- mandatory rules choices such as subclass selection, spell selection, feat/ASI choice, or fighting style choice.

For complex flows:
- model explicit incomplete and complete progression state;
- block save or apply until required choices are valid;
- keep one-off validation and completion effects out of plain persistent state when an effect channel is clearer.

## Data Boundaries

- `Room`, `DAO`, and `Entity` types must not leak into presentation.
- Presentation should consume domain models or explicit UI models only.
- Mapping between persistence models and domain/UI models must happen below presentation.
- Rules-derived progression decisions should be translated into domain models or feature-specific UI models before they reach composables.

## Anti-Pattern Bans

- Creating `Repository`, `RoomDatabase`, DAO, or rules-engine instances directly in screens.
- Creating feature dependencies directly inside `NavHost` as the steady-state architecture.
- Keeping business logic in `App`, `Activity`, composables, or navigation setup files.
- Mixing multiple feature responsibilities into a shared catch-all package.
- Letting a screen own another feature's mapper, repository, or navigation details.
- Persisting a rules-driven transition without resolving mandatory rule consequences first.
- Replacing missing rules implementation with static helper text when the missing piece affects saved character state.

## Naming

Prefer descriptive feature names:
- `:feature:character-list`
- `:feature:character-detail`
- `:feature:character-editor`
- `:feature:character-creation`

Prefer packages that mirror feature ownership:
- `feature.character.list`
- `feature.character.detail`
- `feature.character.editor`
- `feature.character.creation`

## Migration Guidance

- Extract by feature first.
- Move reusable rules logic into `:core:rules`.
- Move shared navigation concerns into `:core:navigation`.
- Introduce Koin instead of expanding manual factories.
- Avoid big-bang rewrites unless the task explicitly requires them.

## Task Workflow

For feature work, bug fixes, and refactors:
- Inspect the touched area and adjacent feature, rules, navigation, and data boundaries before editing.
- Infer the real user-facing workflow, not only the immediate code change requested.
- Identify whether the task is mainly a feature addition, bug fix, architectural refactor, or migration seam.
- Check whether the change implies follow-on work in navigation, DI, persistence, rules, tests, or module wiring.
- Do not stop at the first working implementation if it leaves obvious breakage, invalid state, or architecture drift behind.

Verification is part of the default workflow, not an optional follow-up:
- After implementation and before the final report, decide whether verification should be routed through `$test-verification-orchestrator`.
- Default to `$test-verification-orchestrator` for feature work, bug fixes, rules-driven changes, navigation changes, persistence changes, and refactors that can affect user-visible behavior or safety.
- Do not skip the orchestration step just because one narrow compile or unit command already passed when the touched flow still has regression risk.
- Verification is a hard completion gate for those task types. A task is not complete until the agent either runs `$test-verification-orchestrator` or reports a concrete blocker that prevented it.

## Execution Run Hard Rules

These rules apply when the user asks for a sequential execution run across multiple tasks.

- Process tasks strictly one by one.
- After each completed task, verify it, commit it, and continue immediately to the next task.
- Do not stop or hand back the run early unless a valid blocker category is reached or the backlog is exhausted.
- Reaching a convenient pause point, having several completed commits, or deciding to summarize progress are not valid stop reasons.
- Do not stop on a clean commit boundary unless the user asked to stop or a concrete blocker prevents safe continuation.
- Context size, answer size, prudence, convenience, or perceived task size are not valid stop reasons.
- Do not claim the run is finished or summarize final metrics while there are remaining tasks and no blocker.
- If the run stops, name the exact blocking task and the blocker category.
- Valid blocker categories are only:
  `BLOCKED_BY_REQUIREMENT_AMBIGUITY`
  `BLOCKED_BY_ENVIRONMENT`
  `BLOCKED_BY_BROKEN_BUILD_OR_TESTS`
  `BLOCKED_BY_PERMISSION`
  `BLOCKED_BY_CONFLICTING_USER_CHANGES`
- Waiting for a permission response is not itself a blocker. Keep the run alive, treat the step as pending approval, and continue any non-blocked analysis, coding, verification preparation, or adjacent work that can proceed safely.
- Do not assume the user will deny a permission request. `BLOCKED_BY_PERMISSION` is valid only after an explicit denial or when a required permission is the remaining critical-path dependency and no further safe progress is possible without it.
- Do not stop or slow down based on speculative context-budget concerns unless a concrete tool or model limit has already been hit. If a real limit is encountered, name it explicitly.
- If the backlog is large or the run is expected to be long, prefer narrow subagent delegation for per-task implementation, review, or verification slices so the main thread stays focused on sequencing and integration.
- Subagent use in an execution run must not violate sequential delivery: only one backlog task may be actively implemented at a time, but the current task may be delegated to one or more tightly scoped subagents while the main agent coordinates and integrates the result.
- When using subagents for an execution run, wait for their result and finish the current task before opening the next backlog task. Do not parallelize multiple backlog tasks just to save time.
- Treat excessive main-thread narration or context buildup during a long run as an execution smell. Prefer short coordination updates and offload bulky task-local work to subagents when that reduces thread clutter without breaking task order.

## Git History

- For a single feature branch or task branch, prefer one squashed commit that represents the final coherent change set.
- Do not leave a stack of incremental WIP or fixup commits in the branch unless the user explicitly asks to preserve that history.

## Feature Delivery

- Implement the complete primary user flow for the slice being touched: entry, loading, validation, persistence, result handling, and visible error handling.
- Prefer a vertical slice through the correct feature module over partial wiring across unrelated files.
- Add the smallest domain and data seams needed to keep the feature maintainable.
- If a feature introduces reusable rules or progression concepts, extract them to `:core:rules` or another appropriate core module.
- If a feature is intentionally partial, the limitation must be explicit, user-safe, and covered by tests.

## Refactors

- Preserve behavior unless the task explicitly includes a behavior change.
- Separate pure moves or extractions from logic changes when feasible.
- Leave the touched area more aligned with the target module structure, DI model, and navigation model than before.
- Do not expand old app-level or catch-all packages as a shortcut.
- Remove or reduce migration seams when you touch them unless doing so would materially expand scope.
- If a temporary seam remains, document why it remains and where it should end up.

## Scope Control

- Widen scope when needed to keep the result valid, testable, or architecturally coherent.
- Do not widen scope for speculative cleanup unrelated to the request.
- Required scope expansion includes dependent wiring, blocking validation, essential tests, and architectural extraction needed to avoid reinforcing a bad pattern.
- Omit optional cleanup unless it is very local and clearly improves the touched code without adding review noise.

## Delivery Quality

- For feature work, implement the full happy path, not only the navigation stub or persistence primitive.
- If the request is underspecified but the domain implies mandatory behavior, infer it from existing rules, data, and tests.
- Ask an explicit question only when multiple materially different product decisions are plausible and the codebase does not already imply the intended choice.
- When adding a new feature module, update the architecture target list and keep naming aligned with feature ownership.

## Decision Rules

When several reasonable implementations exist, prefer the one that:
- reinforces feature ownership;
- keeps business rules out of composables and navigation setup;
- improves testability with explicit domain seams;
- reduces manual object construction in favor of Koin;
- avoids coupling unrelated features;
- leaves a clean extraction path toward the target module structure.

Avoid locally convenient but globally sticky choices such as:
- adding another app-level coordinator that knows every feature;
- storing feature-specific rules logic in presentation because it is faster;
- putting repository or mapper responsibilities in the wrong module;
- using reminder text in place of validation or state modeling.

## Good Project Examples

Prefer local patterns over invented style. See `AGENTS_PATHS.md` for concrete reference files.

Style anchors:
- small, explicit constructors;
- clear `UiState` models;
- domain/data boundaries with mapping functions;
- narrow DI modules;
- tests that exercise real behavior transitions.

## General Anti-Patterns

Prohibited unless the user explicitly asks for a temporary spike and the final report calls it out:
- using `Any`, unchecked casts, or untyped maps where a real domain/UI model should exist;
- leaving `println`, `console.log`, ad hoc debug toasts, or similar debug logging in production code;
- writing raw SQL strings outside Room `@Query` declarations or bypassing DAO/repository boundaries for persistence access;
- putting business rules, validation, or progression calculations directly in composables, `Activity`, `App`, or `NavHost`;
- returning persistence-layer types such as Room entities or DAO results directly to presentation;
- adding one-off singleton/service construction inside screens or navigation destinations instead of DI wiring;
- silent catch-all exception handling that hides failure cause and allows the app to continue in an invalid state;
- stringly typed cross-feature contracts when an explicit route contract, model, or enum would make the boundary clear.

Prefer:
- explicit models;
- repository and rules abstractions;
- focused error handling with user-safe messages;
- typed navigation contracts;
- testable domain helpers and use cases.

## File Templates

Use these as default shapes for new files. Adapt to the smallest valid version for the task, but keep ordering predictable.

### `Feature.kt`

Order:
- package
- Android/navigation imports
- presentation imports
- DI imports
- route constants
- destination object
- graph builder
- Koin module

Recommended shape:
- package `com.vinni.dndcharacterlist.feature.character.<feature>`
- imports
- private nav argument constants
- `object <Feature>Destination : NavigationDestination`
- `fun NavGraphBuilder.<feature>Graph(...)`
- `val <feature>Module = module { ... }`

Reference file:
- see `AGENTS_PATHS.md`

### `ViewModel.kt`

Order:
- package
- Compose/lifecycle imports
- domain imports
- coroutine imports
- `UiState` and related UI models
- `ViewModel`
- private mapping/helpers

Recommended shape:
- package `com.vinni.dndcharacterlist.feature.character.<feature>.presentation`
- imports
- `data class <Feature>UiState(...)`
- additional UI-only models if needed
- `class <Feature>ViewModel(...) : ViewModel()`
- public intent methods
- private mapping/extensions/helpers at file bottom

Reference files:
- see `AGENTS_PATHS.md`

### `Repository.kt`

Order:
- package
- data imports
- domain imports
- flow/coroutine imports
- class declaration
- read methods
- write methods
- private mapping helpers if needed

Recommended shape:
- package `com.vinni.dndcharacterlist.core.data.repository`
- imports
- `class <Name>Repository(...) : <DomainRepository>`
- read methods first
- write methods after reads
- mapping through `toDomain()` / `toEntity()` helpers instead of leaking Room models
- `require(...)` or equivalent invariant checks on updates/deletes where needed

Reference file:
- see `AGENTS_PATHS.md`

### Domain helper or use case

Order:
- package
- domain imports
- public data/classes
- public function or operator
- private helpers

Recommended shape:
- package `com.vinni.dndcharacterlist.<module>.domain`
- imports
- result/data classes if needed
- `class <UseCaseOrHelper>(...)`
- one public entry point
- private helper functions below the public API

Reference files:
- see `AGENTS_PATHS.md`

Template rules:
- one primary responsibility per file;
- public declarations before private helpers;
- prefer top-level private mapping functions over burying mapping inside composables;
- do not mix navigation, UI rendering, domain logic, and persistence in one file;
- split files that become hard to scan because they hold multiple responsibilities.

## Verification

For rules-driven feature work:
- add or update tests for the triggering milestone and its blocking edge cases;
- add or update tests for invalid progression attempts, including "cannot persist until required choice is made" scenarios;
- verify touched modules with module-local commands from `LOCAL_COMMANDS.md`;
- call out intentionally deferred rules coverage in the final report.

General verification rules:
- new feature behavior should have targeted tests at the lowest useful layer: domain first, then `ViewModel`, then UI only when interaction risk justifies it;
- refactors should verify both compilation and at least one focused behavior check for the touched area;
- if verification is skipped or limited, say exactly what was not run and what risk remains;
- tests should cover success paths, blocking validation, and the most likely regression path introduced by the change.
- when project rules require `$test-verification-orchestrator`, replacing it with ad hoc compile or unit commands without a concrete blocker is a policy violation.

Use `$test-verification-orchestrator` as the default verification entry point when any of the following is true:
- business logic, rules, validation, calculations, state reduction, or repository orchestration changed;
- a screen, navigation path, form, persistence flow, or loading/result handling changed;
- the task spans both domain and presentation boundaries;
- the request asks to verify, regression-check, sanity-check, smoke-test, or make the change delivery-ready.

`$test-verification-orchestrator` should decide whether to run only `$android-test-gap-closer` or both `$android-test-gap-closer` and `$mobile-smoke-tests`:
- run `$android-test-gap-closer` for business-logic, rules, repository, mapper, validation, and non-trivial `ViewModel` changes;
- also run `$mobile-smoke-tests` when the change touches a user flow, screen entry, navigation, form interaction, save/apply path, loading state, or another critical happy path;
- keep smoke optional for purely internal refactors that do not alter a meaningful user journey;
- if smoke execution is blocked by missing device or setup, report `BLOCKED` explicitly instead of silently downgrading coverage.
- if smoke is not run for a user-facing flow change, the agent must explicitly report either `SMOKE_NOT_REQUIRED` with a narrow reason or `SMOKE_BLOCKED` with the concrete blocker.

The orchestrated verification report should include:
- which skills ran and why;
- which commands and scenarios ran;
- pass, fail, or blocked status for each verification track;
- the first failing step or command when something breaks;
- the shortest credible diagnosis and the next recommended fix.

## Final Reporting

Final task reports should state:
- what was implemented or changed;
- what was verified;
- any remaining seam, limitation, or deferred coverage;
- for refactors, which architectural direction improved.
- for execution runs, final metrics may be reported only when the backlog is exhausted or the run stopped on a concrete blocker.

## Project-Specific Overrides

- Define concrete module commands and command mapping in `LOCAL_COMMANDS.md`.
- Define project-specific secrets handling in `SECRETS.md`.
- Keep review mode strict for code review tasks.
- Keep Fast Mode only for 1-2 file changes.
- Treat architecture drift away from the target module structure as a review concern.
- Until these rules are explicitly changed, persisted character data is disposable during development.
- Do not preserve legacy compatibility for existing saved characters unless the user explicitly updates this rule.
- When a rules-driven change needs to reshape or invalidate saved character records, prefer the cleaner implementation over migration seams for old local data.

## Local References

- see `AGENTS_PATHS.md`

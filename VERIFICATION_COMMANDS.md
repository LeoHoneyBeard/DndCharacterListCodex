# Verification Commands

Tracked verification source for `DndCharacterListCodex`.
Use these commands instead of a repository-tracked `LOCAL_COMMANDS.md`.

## Fast Path

Run the narrowest command that covers the touched area.

- `cmd /c gradlew.bat :core:data:testDebugUnitTest`
- `cmd /c gradlew.bat :core:rules:testDebugUnitTest`
- `cmd /c gradlew.bat :feature:character-list:testDebugUnitTest`
- `cmd /c gradlew.bat :feature:character-detail:testDebugUnitTest`
- `cmd /c gradlew.bat :feature:character-editor:testDebugUnitTest`
- `cmd /c gradlew.bat :feature:character-creation:testDebugUnitTest`
- `cmd /c gradlew.bat :feature:character-level-up:testDebugUnitTest`
- `cmd /c gradlew.bat :app:assembleDebug`

## Strict Path

Use for release-ready verification, cross-module refactors, rules changes, persistence changes, or when the touched surface spans multiple features.

- `cmd /c gradlew.bat :core:data:testDebugUnitTest :core:rules:testDebugUnitTest :feature:character-list:testDebugUnitTest :feature:character-detail:testDebugUnitTest :feature:character-editor:testDebugUnitTest :feature:character-creation:testDebugUnitTest :feature:character-level-up:testDebugUnitTest`
- `cmd /c gradlew.bat :app:assembleDebug`

## Command Mapping

- `:core:data`
  `cmd /c gradlew.bat :core:data:testDebugUnitTest`
- `:core:rules`
  `cmd /c gradlew.bat :core:rules:testDebugUnitTest`
- `:feature:character-list`
  `cmd /c gradlew.bat :feature:character-list:testDebugUnitTest`
- `:feature:character-detail`
  `cmd /c gradlew.bat :feature:character-detail:testDebugUnitTest`
- `:feature:character-editor`
  `cmd /c gradlew.bat :feature:character-editor:testDebugUnitTest`
- `:feature:character-creation`
  `cmd /c gradlew.bat :feature:character-creation:testDebugUnitTest`
- `:feature:character-level-up`
  `cmd /c gradlew.bat :feature:character-level-up:testDebugUnitTest`
- full app compile/integration check
  `cmd /c gradlew.bat :app:assembleDebug`

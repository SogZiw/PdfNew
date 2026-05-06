# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android project. The app module is `app`, with Kotlin source in `app/src/main/java/com/word/file/manager/pdf`. Shared base classes and helpers live under `base/`, reusable view inset helpers under `ext/`, and feature screens under `modules/` (`fragments/`, `permissions/`, `tools/`, `dialogs/`). XML layouts, drawables, colors, themes, and localized strings are in `app/src/main/res`. Bundled Office/PPT/Excel viewer files are in `app/src/main/assets`. Unit tests belong in `app/src/test`, and device/instrumentation tests belong in `app/src/androidTest`.

## Build, Test, and Development Commands

- `./gradlew :app:compileDebugKotlin`: fast Kotlin compilation check for app code.
- `./gradlew :app:assembleDebug`: build a debug APK.
- `./gradlew :app:installDebug`: install the debug APK on a connected device or emulator.
- `./gradlew :app:testDebugUnitTest`: run local JVM unit tests.
- `./gradlew :app:connectedDebugAndroidTest`: run instrumentation tests on a connected device.
- `./gradlew :app:lintDebug`: run Android lint for the debug variant.

Use Android Studio for emulator/device debugging when UI flows, permissions, document scanning, or file picker behavior need manual validation.

## Coding Style & Naming Conventions

Kotlin uses the official style (`kotlin.code.style=official`) with 4-space indentation. Prefer ViewBinding over manual `findViewById`; it is enabled in `app/build.gradle.kts`. Keep shared document logic in `base/data`, file utilities in `base/utils/FileCommonExt.kt`, app helpers in `AppCommonExt.kt`, and permission routing in `modules/permissions`. Use intent-revealing names such as `DocumentRepository`, `LocalPrefs`, `StoragePermissionActivity`, and `SystemVersionExt`. Match existing Android resource prefixes: `activity_`, `fragment_`, `item_`, `dialog_`, `view_`, `ic_`, `shape_`, and `selector_`.

## Testing Guidelines

The project currently uses JUnit 4 for local tests and AndroidX JUnit/Espresso for instrumentation tests. Add focused tests for repository, utility, and permission-flow behavior when changing logic. Name local tests `*Test.kt` and instrumentation tests `*InstrumentedTest.kt`. There is no enforced coverage threshold in the current Gradle setup; prioritize meaningful regression coverage for changed behavior.

## Commit & Pull Request Guidelines

Recent history uses short, imperative messages such as `Fix file tab horizontal scrolling` and `Update README project notes`, with occasional conventional prefixes like `feat:` or `fix:`. Keep commits scoped to one coherent change. PRs should include a clear summary, validation commands run, linked issue/task when available, and screenshots or screen recordings for UI/resource changes.

## Agent-Specific Instructions

Read current files before editing and keep changes tightly scoped. Preserve existing behavior when refactoring unless the request explicitly changes it. For project convention changes, update `README.md` notes when relevant.

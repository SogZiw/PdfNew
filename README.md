# PdfNew Collaboration Notes

This file captures the working conventions established during recent development so future changes can stay aligned.

## Current Base Structure

- `app/src/main/java/com/word/file/manager/pdf/base/data/DocumentActionType.kt`
  - Defines document action types.
  - Current action types:
    - `DocumentOpenType`
    - `PdfMergeType`
    - `PdfSplitType`
    - `PdfPrintType`
  - Use:
    - `getActionName(context)`
    - `getMenuIconRes()`

- `app/src/main/java/com/word/file/manager/pdf/base/helper/LocalPrefs.kt`
  - Central local preference entry object.
  - Backed by `PreferenceStore`.
  - Keep usage declarative, for example:
    - `var isFirstReqStorage by store.boolean(defaultValue = true)`

- `app/src/main/java/com/word/file/manager/pdf/base/helper/PreferenceStore.kt`
  - Shared preference delegate container.
  - Supported delegate types:
    - `boolean`
    - `int`
    - `long`
    - `float`
    - `string`
    - `double`
  - `double` is stored as `String`.

- `app/src/main/java/com/word/file/manager/pdf/base/permission/`
  - `StoragePermissionActivity.kt`
    - Base page for storage permission flow.
    - Public hooks:
      - `checkStoragePermission(type)`
      - `onStorageAccessGranted(type)`
      - `onStorageAccessDenied()`
  - `AllFilesPermissionActivity.kt`
    - Dedicated page for Android 11+ all-files-access flow.
  - `PermissionExt.kt`
    - Holds permission-related helpers, intents, and routing support.

- `app/src/main/java/com/word/file/manager/pdf/base/utils/`
  - `AppCommonExt.kt`
    - Common app helpers only.
    - Current helpers:
      - `showMessageToast(...)`
      - `buildPeriodicSignalFlow(...)`
  - `SystemVersionExt.kt`
    - Central Android version checks.
    - Prefer:
      - `isAtLeastApi(...)`
      - `isAtLeastApi26()`
      - `isAtLeastApi29()`
      - `isAtLeastApi30()`
      - `isAtLeastApi31()`
      - `isAtLeastApi33()`
      - `isAtLeastApi34()`

## Naming Preferences

- Prefer differentiated names instead of directly copying names from the reference project.
- Avoid overly generic names like:
  - `types.kt`
  - `Prefs`
  - `CommonExt`
  - `BaseStorageActivity`
  - `SpecialTempActivity`
- Prefer names that explain intent or scope:
  - `DocumentActionType`
  - `LocalPrefs`
  - `PreferenceStore`
  - `StoragePermissionActivity`
  - `AllFilesPermissionActivity`
  - `PermissionExt`
  - `AppCommonExt`
  - `SystemVersionExt`

## Refactoring Preferences

- Structural changes should be conservative.
- Prefer extracting small methods over changing behavior.
- If refactoring existing logic:
  - keep behavior unchanged
  - improve readability by extracting helper methods
  - avoid changing state flow unless explicitly requested

## Utility Placement Rules

- Permission-specific code should live under `base/permission/`.
- Android version checks should live in a dedicated version utility file.
- Only truly common helpers should stay in `base/utils/`.
- Do not mix permission routing logic into generic utility files.

## Resource Conventions

- General fallback error text currently uses:
  - key: `common_error_message`
  - value: `Something went wrong`

## Notes For Future Changes

- When introducing new preference fields, add them to `LocalPrefs` and back them with `PreferenceStore`.
- When adding new version checks, prefer `isAtLeastApiXX()` naming instead of Android dessert names.
- When adding new permission behaviors, first check whether the logic belongs in:
  - `PermissionExt.kt`
  - `StoragePermissionActivity.kt`
  - `AllFilesPermissionActivity.kt`
- Record each requested change in this `README.md` as part of the implementation workflow.

## Recent Changes

- Added `RouteActivity` as the launcher page with a 3-second delay before routing to `MainActivity`.
- Added `activity_route.xml` splash-style layout and related route logo resources.
- Added `item_file_info_with_menu.xml` item layout for file rows with icon, text, and more-action affordance.
- Consolidated the remaining in-progress launcher asset updates into a follow-up commit when requested.
- Implemented `HomeFragment`, `RecentFragment`, `BookmarkFragment`, and `DocumentFragment` with shared file scanning, category filters, recent/bookmark data, and system file opening.
- Kept the new fragment flow free of ads and left the file `more` action unimplemented as requested.
- Restored `HomeFragment` category switching to use the existing `layout_selector` buttons instead of replacing that area with `TabLayout`.
- Unified `RecentFragment` and `BookmarkFragment` onto the same selector-button pattern after removing the shared `TabLayout` dependency from `fragment_home.xml`.
- Updated the empty document state to show `ic_page_empty` while keeping the current empty-state text unchanged.
- Fixed the post-permission refresh path so empty states in `RecentFragment` and `BookmarkFragment` render correctly after storage access is granted.
- Replaced the app-wide `MainViewModel` approach with a `DocumentRepository` that exposes shared `StateFlow` and `SharedFlow` for file data, permission prompts, and UI state.
- Moved `DocumentRepository` out of `modules/main` into `base/data` so its placement matches its cross-feature data responsibility.
- Added local `assets` viewer resources for `word`, `excel`, and `ppt` document rendering support.

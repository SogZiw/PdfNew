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
- Added shared toolbar resources including `view_toolbar_common.xml` and `ic_go_back.xml`.
- Implemented `PdfViewActivity` and `OfficeViewActivity` using the shared toolbar layout, internal file-detail navigation, PDF rendering, and asset-based Office preview.
- Moved `EXTRA_FILE_ITEM` out of the data model file into the shared global constants area.
- Differentiated `PdfViewActivity` and `OfficeViewActivity` visually and behaviorally with PDF page-state UI and Office-specific preview/loading status UI.
- Renamed the internal viewer classes to `PdfReaderActivity` and `OfficePreviewActivity` and reorganized their method structure to reduce direct similarity with the reference `PDFView` project while preserving behavior.
- Renamed the viewer layout files to `activity_pdf_reader.xml` and `activity_office_preview.xml` to match the updated activity names.
- Updated the bottom navigation `History` label to `Bookmark` to match the actual collection tab meaning.
- Renamed the bottom navigation string key from the old `history` semantics to `bookmark_nav` to match the tab’s actual purpose.
- Added a static file-action dialog layout based on the provided screenshot, with menu rows and icons wired for later click handling.
- Added `FileActionsDialogFragment` to populate the screenshot-style action sheet structure without implementing action click behavior yet.
- Added missing `ic_menu_lock`, `ic_menu_unlock`, and `ic_menu_info` vector resources so the file-action dialog can render the full action list.
- Wired the file item `more` button in `DocumentFragment` to open `FileActionsDialogFragment` without attaching individual action handlers yet.
- Updated `FileActionsDialogFragment` to reconcile the collect icon state from the database so the dialog header stays accurate even when opened from the scanned home list.
- Updated `image_file_cover` in `FileActionsDialogFragment` to reflect the actual file category icon instead of always using the PDF asset.
- Implemented bookmark toggling through `FileActionsDialogFragment` and `DocumentRepository`, with state refresh flowing back to the home list and bookmark list.
- Removed the redundant `isCollected` argument from `FileActionsDialogFragment.newInstance()` and now initialize the collect state directly from database-backed file data.
- Added encrypted-PDF password handling to `PdfReaderActivity`, including password-required detection and a simple password entry dialog.
- Updated the PDF password dialog flow so password input is validated before dismissing the dialog; invalid passwords now show an error toast and clear the input for retry.
- Added `isEncrypt` to `FileItem`, Room persistence, and file scanning, and merged stored Room fields back into scanned items so favorite/recent/encryption state stays consistent after refresh.
- Renamed the Room table and all persisted column names for `FileItem`, and switched the database to destructive migration so the new schema replaces the old one without a manual migration path.
- Renamed the `FileItem` property names themselves away from the original `PDFView` naming, and updated all repository, viewer, dialog, and list bindings to use the new field names consistently.
- Implemented `rename`, `open`, `share`, `print`, and `delete` actions in `FileActionsDialogFragment`, with repository-backed state updates so list and bookmark state stay synchronized after file operations.
- Replaced deprecated `bundleOf(...)` usage in `FileActionsDialogFragment` with an explicit `Bundle` initializer.
- Implemented PDF creation via ML Kit document scanning, local file persistence, repository insertion, and a dedicated create-result page wired from the main add button.
- Fixed the create-PDF permission path so granting storage via the add button also refreshes document state and clears the permission UI before launching the scanner.
- Added `Tools` bottom-navigation resources and updated the main bottom-nav wiring/text to support the tools tab.
- Guarded the file-action dialog’s async collect-state hydration so it no longer races with a user’s immediate bookmark toggle and overwrite the freshly updated icon state.
- Removed the `Merge PDF` row from the file-action dialog menu while keeping the shared merge action definition available for future tool flows.
- Added `ToolsFragment` with a `Format` tools grid for merge, split, lock, and unlock PDF actions, wired with placeholder click handlers.
- Implemented the Tools tab `Merge PDF` and `Split PDF` flows with PDF selection, page selection for splitting, and generated result registration.
- Hardened the PDF merge/split flows by preventing duplicate action clicks during processing and canceling split-page thumbnail render jobs before closing the renderer.
- Added visible selection-order badges to the Merge PDF file picker and Split PDF page picker so selected items show their click order.
- Switched the Merge PDF and Split PDF selection item states from check icons to theme-color stroke backgrounds.
- Aligned `PdfSplitPagesActivity` thumbnail rendering more closely with the reference flow by initializing `PdfRenderer` off the main thread, checking coroutine active state before rendering, and delaying renderer cleanup behind the render mutex.
- Updated `PdfSplitPagesActivity` page thumbnail rendering to keep only one active render job, matching the reference behavior so fast scrolling does not build up a slow render queue.
- Moved the split-page thumbnail render mutex back into `onViewAttachedToWindow()` so the attach/render structure matches the reference implementation more closely.
- Updated the Split PDF flow so finishing a successful split also closes the upstream `PdfSplitActivity` after the result page is launched.
- Added a minimum 3-second processing wait for Merge PDF and Split PDF so fast operations keep the working dialog visible long enough before opening the result page.
- Rechecked the full Merge PDF and Split PDF chains, including tools entry routing, storage permission handoff, selection state, PDF processing, repository insertion, and result navigation.
- Wired the file more-dialog Split action to open the current PDF directly in the split page picker, with non-PDF/encrypted files disabled and single-page PDFs blocked with the existing warning.

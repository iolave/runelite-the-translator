# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v1.3.0] - 2026-01-20

### Added
- Dialogue translation can now be disabled.
- Translation files are now loaded dynamically from [iolave/osrs-translations](https://github.com/iolave/osrs-translations).
- Added real time translation for chat box messages and quest guide journal.

### Changed
- Renamed `SelectLanguage` enum to `Language`.
- TranslatorAPI now uses the `/api/v1/data-collection` endpoint to collect dialogues.
- Translation maps were consolidated into a single map to allow easier implementation of new translation maps.
- Menu entries are now translated when added to the stack rather than when the menu is opened.
- Plugin confiuration now have sections.

### Fixed
- Dialogues after the first one are now collected correctly.
- Collected dialogues with player name are now collected with the name repalced by `[PLAYER_NAME]`.

## [v1.2.0] - 2026-01-14

### Added
- Added Portuguese support.
- Portuguese translation for dialogues, items, NPCs, and objects.

### Changed
- Reduced Finnish translation file sizes.

### Fixed
- Incorrect schedule time on plugin change.

### Removed
- Removed unused resource files.

## [v1.1.0] - 2026-01-07

### Added
- Added an option to enable/disable dialogue options translation.
- Added opt-in data collection via runelite-translator-api.
- Added spanish support.
- Data collection is now opt-in.

### Removed
- Removed sys outs.

## [v1.0.0] - 2025-12-03

### Added

- dialouge translation with support for finnish and spanish.
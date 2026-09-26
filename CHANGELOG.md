# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2025-03-08

### Added
- **Customizable Travel Rates**: Added rate configuration options in Settings for Rate Per Mile and Rate Per Hour, persisting across sessions.
- **Comprehensive Unit Testing**: Added unit tests in `CostCalculatorTest` for address formatting, ViewModel state transitions, expense list manipulation, and number parsing/rounding logic.
- **Documentation**: Created `README.md` project guide and `CHANGELOG.md`.

### Refactored & Improved
- **Modular UI Architecture**: Extracted monolithic UI composables out of `MainActivity.kt` into clean components under `com.example.ui`:
  - `MainScreen`
  - `SettingsScreen`
  - `PolicyScreen`
  - `BreakdownSection`
  - `AutocompleteField`
- **Locale Safety**: Fixed number formatting for network requests (OSRM API) and user string outputs to explicitly force `Locale.US` to prevent coordinate parsing crashes in non-US locales.
- **Settings Screen Search**: Updated coroutine scope usage in `SettingsScreen` for safe cancellation of pending Nominatim location search jobs.

---

## [1.0.0] - Initial Release

- Initial release of Cost Calculator app with route calculation via OpenStreetMap Nominatim and OSRM APIs.

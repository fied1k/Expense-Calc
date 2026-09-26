# Travel Cost Calculator

An Android application built with Jetpack Compose for calculating travel distances, travel times, and total reimbursable costs based on mileage, hourly rate, and additional expenses.

## Features

- **Route Auto-Calculator**: Automatically look up addresses or ZIP codes using OpenStreetMap Nominatim API and fetch driving distance/time via OSRM (Open Source Routing Machine) API.
- **Customizable Travel Rates**: Configure custom Rate Per Mile and Rate Per Hour in the app Settings.
- **Manual Input**: Input one-way miles and hours manually with automatic rounding to the nearest half-unit.
- **Additional Expenses**: Dynamically add and remove itemized extra costs (such as tolls, parking, or per diem).
- **Cost Breakdown & Clipboard Export**: View itemized mileage, time, and total cost breakdown and copy the detailed summary to clipboard with a single tap.
- **Default Starting Address**: Save a default starting address that pre-fills when the app opens.
- **Theme Support**: Select Light, Dark, or System Default theme modes.
- **Travel Rates & Policy Reference**: Built-in policy screen explaining standard travel rate rules.

## Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) with Kotlin Coroutines & StateFlow
- **Networking**: Retrofit2 & Moshi for JSON parsing, OkHttp
- **Data Persistence**: Android `SharedPreferences` managed via `SettingsManager`
- **Testing**: JUnit 4, Robolectric, and Roborazzi

## Code Structure

```
app/src/main/java/com/example/
├── MainActivity.kt               # Main entry point activity
├── CostCalculatorViewModel.kt     # ViewModel managing calculator state and API requests
├── SettingsManager.kt            # Persistence for theme, default address, and custom rates
├── Api.kt                        # Retrofit interfaces & Moshi data models for Nominatim & OSRM
└── ui/
    ├── MainScreen.kt             # Main calculator UI screen
    ├── SettingsScreen.kt         # Settings UI screen
    ├── PolicyScreen.kt           # Travel rate policy reference screen
    ├── BreakdownSection.kt       # Cost summary & copy component
    ├── AutocompleteField.kt      # Reusable address autocomplete input field
    └── theme/                    # Compose Color, Type, and Theme definitions
```

## Getting Started

1. Open the project in Android Studio.
2. Sync Project with Gradle Files.
3. Run the app on an Android Emulator or physical device (Min SDK: 24, Target SDK: 36).

## Testing

Unit tests are located in `app/src/test/java/com/example/CostCalculatorTest.kt`.

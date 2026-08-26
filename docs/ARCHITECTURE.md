# Toolbox Architecture

## Overview

Toolbox is an Android utility app. It provides unit converters and a compass using the device's
magnetic-north sensor.

## Product Features

- Convert length, mass, temperature, speed, volume, and area units.
- Show a magnetic-north compass heading, eight-point direction, level, and magnetic-field
  diagnostics when available.
- Work without network access.
- Keep converter state in memory only.
- Refresh packaged ECB currency reference rates on demand from the Currency toolbar.

## UX

### Converters

- Users select source and target units, enter a source value, and read the converted value.
- The numeric keypad accepts the active locale's decimal separator.
- Swap exchanges the selected units and promotes the converted value to editable input.

### Home

- The home screen presents a fixed local catalog in this order: Compass, Length, Speed,
  Temperature, Mass, Volume, and Area.
- The app is portrait-only.

### Compass

- The compass shows heading in degrees and a localized eight-point direction, including
  intercardinal directions.
- It shows device pitch and roll, plus magnetic-field strength and accuracy when the device has a
  magnetic-field sensor. A missing magnetic-field sensor does not disable the compass.
- The app shows an unavailable state when a rotation-vector sensor is unavailable.
- Sensor listeners are registered only while the Compass screen lifecycle is at least `STARTED`.

## Technical Decisions

### Platform And Stack

- Support Android 15+ / API 35+.
- Kotlin and Jetpack Compose.
- Coroutines and Flow.
- No backend, sync, advertising SDKs, or background work.
- The Currency screen alone has network access, solely to download ECB reference rates through
  Frankfurter when the user requests a refresh.

### Architecture Style

Use pragmatic clean/hexagonal architecture.

Keep the app simple. Avoid heavy enterprise layering.

### Directory Structure

```text
app/src/main/java/com/cosimomatteini/toolbox/
  domain/          immutable models, pure conversion logic, and platform ports
  features/        user-visible actions and business workflows
  infrastructure/  Android implementations of domain ports
  ui/              Compose screens, view models, and navigation
```

`domain/`, `features/`, `infrastructure/`, and `ui/` are flat packages. `domain/` and
`features/` remain free of Android imports.

### Dependency Rules

```text
ui -> features -> domain
infrastructure -> domain
```

Do not depend on `infrastructure` from `domain` or `features`.

### Domain Modelling

- Immutable domain models and pure functions for conversions.
- `BigDecimal` conversion values to avoid floating-point precision errors.

### Dependency Injection

- Manual dependency injection through `ToolboxAppContainer`.
- `ToolboxAppContainer` is the application composition root; it wires feature dependencies and
  Android infrastructure implementations.
- Do not add Hilt unless explicitly requested.

### Offline And State

- All tools work offline with packaged or persisted currency rates. Currency refresh runs only
  after the user presses its toolbar action, never in the background.
- Converters do not persist values or unit selections. Their state is ephemeral and exists only in
  memory while the app process is alive.
- Currency rates are the only persisted data. Runtime rates use the packaged JSON schema and are
  atomically replaced after validation.
- Do not add Room or another persistence layer unless a product requirement needs persistence.

### Compass Platform

- Android `SensorManager` behind a `CompassSensor` port.
- Use the rotation-vector sensor for magnetic-north heading and device level.
- Use the magnetic-field sensor, when available, only for field strength and accuracy diagnostics.
- A missing rotation-vector sensor makes the compass unavailable; a missing magnetic-field sensor
  does not.
- Keep the Android sensor implementation in `infrastructure/`; the port and heading logic stay
  outside Android-specific code.
- Do not request location permission or correct for true-north declination.

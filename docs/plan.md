# Toolbox Implementation Plan

## 1. Bootstrap Project (Done)

- Create native Android single-module `:app` project.
- Set root project name to `Toolbox`.
- Use application ID `com.cosimomatteini.toolbox`.
- Use Kotlin and Jetpack Compose.
- Set minimum Android support to Android 15+ / API 35+.
- Mirror Noted app Gradle wrapper, version catalog, Compose Material 3, lifecycle/ViewModel, Coroutines, ktlint, editor config, and Make targets.
- Add launcher manifest, adaptive icon, app label, and Compose theme resources.
- Add English default resources and Italian `values-it` resources.
- Verify an empty Compose app builds with `make check`.

## 2. Add Architecture Skeleton (Done)

- Create flat `domain/`, `features/`, `infrastructure/`, and `ui/` packages.
- Add `ToolboxAppContainer` for manual dependency wiring.
- Keep dependencies `ui -> features -> domain`.
- Keep `domain/` and `features/` Android-free.
- Do not add Room, a backend, sync, advertising SDKs, or network permissions.
- Add `docs/ARCHITECTURE.md`.
- Document offline-only behavior.
- Document no-persistence decision: converters have ephemeral in-memory state only.
- Document magnetic-north compass sensor integration.
- Verify the app still builds.

## 3. Show Tool Home Screen (Done)

- Add immutable `Tool` domain model.
- Add the initial tool catalog:
  - Length.
  - Mass.
  - Temperature.
  - Speed.
  - Volume.
  - Area.
  - Compass.
- Add a `Tools` feature exposing the static catalog.
- Wire `Tools` in `ToolboxAppContainer`.
- Add `HomeViewModel` and immutable `HomeUiState`.
- Add `HomeScreen` with a responsive grid of tool cards.
- Use outlined Material icons and localized tool labels.
- Add accessible content descriptions.
- Add manual sealed-screen navigation in `MainActivity`.
- Route each tool card to its corresponding screen.
- Add previews for narrow and wide home layouts.
- Add tests for the tool catalog and home UI state.
- Verify every tool opens from the home screen.

## 4. Add Length Converter (Done)

- Add `ConverterCategory.Length`.
- Add immutable `LengthUnit` definitions:
   - Millimeter.
   - Centimeter.
   - Meter.
   - Kilometer.
  - Inch.
  - Foot.
  - Yard.
  - Mile.
- Add canonical-base conversion metadata for length units.
- Add pure conversion functions using `BigDecimal`.
- Add parsing and locale-aware number formatting helpers.
- Accept decimal input using the active locale separator.
- Add `ConvertLength` feature.
- Wire `ConvertLength` in `ToolboxAppContainer`.
- Add reusable `ConverterViewModel` state:
  - Source unit.
  - Target unit.
  - Editable source value.
  - Computed target value.
- Add reusable converter screen:
  - Back action.
  - Source and target unit menus.
  - Source value.
  - Read-only converted value.
  - Conversion equivalence label.
  - Digit, decimal, delete, clear, and swap controls.
- Make swap exchange units and promote the current converted value to editable input.
- Add length-specific route wiring.
- Add tests for all length unit conversions, decimal parsing, formatting, clear, delete, and swap.
- Verify kilometer-to-mile conversion offline.

## 5. Add Mass Converter (Done)

- Add `ConverterCategory.Mass`.
- Add mass units:
  - Milligram.
  - Gram.
  - Kilogram.
  - Ounce.
  - Pound.
  - Stone.
- Add pure mass conversion definitions.
- Add `ConvertMass` feature.
- Reuse the converter state, screen, keypad, selector, and navigation pattern.
- Add mass route wiring and localized strings.
- Add tests for metric and imperial mass conversions.
- Verify mass conversion offline.

## 6. Add Temperature Converter (Done)

- Add `ConverterCategory.Temperature`.
- Add Celsius, Fahrenheit, and Kelvin units.
- Support affine conversions, not only multiplication factors.
- Add pure temperature conversion definitions.
- Add `ConvertTemperature` feature.
- Reuse the converter state and screen.
- Add temperature route wiring and localized strings.
- Add tests for freezing point, boiling point, negative values, and Kelvin boundary behavior.
- Verify temperature conversion offline.

## 7. Add Speed Converter (Done)

- Add `ConverterCategory.Speed`.
- Add speed units:
   - Meters per second.
   - Kilometers per hour.
  - Feet per second.
  - Miles per hour.
- Add pure speed conversion definitions.
- Add `ConvertSpeed` feature.
- Reuse the converter state and screen.
- Add speed route wiring and localized strings.
- Add metric and imperial speed conversion tests.
- Verify speed conversion offline.

## 8. Add Volume Converter

- Add `ConverterCategory.Volume`.
- Add volume units:
  - Millilitre.
  - Litre.
   - Cubic meter.
  - Fluid ounce.
  - Pint.
  - Quart.
  - Imperial gallon.
- Add pure volume conversion definitions.
- Add `ConvertVolume` feature.
- Reuse the converter state and screen.
- Add volume route wiring and localized strings.
- Add metric and imperial volume conversion tests.
- Verify volume conversion offline.

## 9. Add Area Converter

- Add `ConverterCategory.Area`.
- Add area units:
   - Square centimeter.
   - Square meter.
   - Square kilometer.
  - Square inch.
  - Square foot.
  - Square yard.
  - Square mile.
- Add pure area conversion definitions.
- Add `ConvertArea` feature.
- Reuse the converter state and screen.
- Add area route wiring and localized strings.
- Add metric and imperial area conversion tests.
- Verify area conversion offline.

## 10. Add Magnetic-North Compass

- Add immutable compass heading model.
- Add pure functions to:
  - Normalize headings to `0..359`.
  - Convert headings to cardinal directions.
  - Derive dial rotation.
- Add `CompassSensor` port.
- Add Android `SensorManager` implementation in `infrastructure/`.
- Use rotation-vector sensor readings for magnetic-north heading.
- Detect unavailable sensors without crashing.
- Register sensor listeners only while compass is visible.
- Unregister sensor listeners when compass leaves composition.
- Add compass route and screen:
  - Back action.
  - Rotating compass dial.
  - Heading in degrees.
  - Localized cardinal direction.
  - Sensor-unavailable state.
- Do not request location permission.
- Do not correct for true-north declination.
- Add unit tests for heading normalization and cardinal directions.
- Verify live heading manually on a physical device.

## 11. Polish And Verify

- Check all screens in English and Italian.
- Check phone portrait, landscape, and larger-screen layouts.
- Check TalkBack labels and touch target sizes.
- Ensure converters work without network access.
- Ensure compass unavailable state works on emulator.
- Add app-level Compose navigation tests where practical.
- Update `README.md` with features, offline behavior, build steps, and Android version requirement.
- Update `docs/ARCHITECTURE.md` with final models, dependency direction, conversion design, and sensor boundary.
- Run `./gradlew ktlintFormat`.
- Run `make check`.
- Run `make android-test` when an emulator or device is available.

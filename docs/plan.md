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

## 8. Add Volume Converter (Done)

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

## 9. Add Area Converter (Done)

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

## 10. Add Magnetic-North Compass (Done)

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

## 11. Package ECB Currency Rates (Done)

- Add a non-cacheable Gradle task that fetches ECB rates through Frankfurter:
  - `https://api.frankfurter.dev/v2/rates?base=EUR&providers=ECB`
  - Run before generated assets are packaged.
  - Fail the build when the request, parsing, or validation fails.
  - Never package a stale prior task output after a failed fetch.
- Normalize the response into a generated `currency-rates.json` asset:
  - Schema version.
  - Provider ID and name: `ECB` and `European Central Bank`.
  - Source URL, download timestamp, and provider rate date.
  - Base currency `EUR`.
  - `rates` map containing `EUR: "1"` and positive decimal-string rates.
- Validate the generated data has one rate date, valid ISO 4217 codes, positive rates, and EUR at exactly one.
- Add tests for generated-data parsing and validation failures.
- Verify an app build contains a valid generated `currency-rates.json` asset and fails when the fetch fails.

## 12. Add Offline Currency Converter UI (Done)

- Add Currency to the tool catalog, home presentation, navigation, English strings, and Italian strings.
- Load packaged `currency-rates.json` when Currency opens; no network request is needed for this initial UI.
- Add dynamic currency units from the packaged rate codes; do not use an enum or ship manually maintained currency names.
- Use `java.util.Currency` to show localized names in selectors, formatted as `Name (CODE)`, sorted by localized name, and fall back to the code for unknown platform currencies.
- Add pure EUR-pivot conversion using `BigDecimal`: `amount * targetRate / sourceRate`.
- Add a Currency screen reusing converter cards and keypad, defaulting to EUR and USD, with currency codes in the value badges.
- Extend the shared converter top bar with an optional screen-specific action slot for Currency.
- Show the ECB source and provider rate date in the Currency screen; clarify rates are reference rates, not card or bank transaction rates.
- Verify a newly installed app converts all bundled currencies with airplane mode enabled.

## 13. Refresh Currency Rates When Opened

- Add the `INTERNET` manifest permission for Currency rate downloads only.
- Load the latest valid rates in this order: persisted runtime file, packaged generated asset.
- Persist runtime rates atomically in the same JSON schema as the packaged asset.
- When Currency opens, render immediately from available local rates, then attempt one refresh only when the last refresh attempt is at least 24 hours old.
- Seed the first-refresh cooldown from the packaged asset download time, avoiding an unnecessary request immediately after installation.
- Record the refresh attempt before the network request so repeated opens cannot exceed one request per 24 hours, including offline and failed requests.
- On a successful refresh, validate and persist the new file, update the conversion state without losing the entered value or selected currencies, and show the new provider rate date.
- On a failed refresh, retain the persisted or packaged data and show a non-blocking failure message; never make conversion unavailable.
- Do not schedule background work: refresh only when the Currency screen opens.
- Add a Currency app-bar refresh action with loading state.
- During the 24-hour cooldown, present the action as unavailable; interaction explains that the latest exchange rates are already downloaded. Use a distinct message if the most recent check failed.
- Add tests for packaged fallback, persisted-rate precedence, JSON validation, decimal precision, conversion, atomic-update failure retention, cooldown behavior, on-open-only refresh, and refresh-state UI.
- Verify successful and failed refreshes manually with connectivity toggled.

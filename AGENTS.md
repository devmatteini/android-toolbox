# AGENTS.md

Toolbox is an Android utility app for everyday conversions and a magnetic-north compass., built with
Kotlin, Jetpack Compose, and Gradle.

## Essentials

- Package/application id: `com.cosimomatteini.toolbox`.
- Package manager/build runner: Gradle wrapper (`./gradlew`), with Make shortcuts in `Makefile`.
- Manual DI is wired in `app/src/main/java/com/cosimomatteini/toolbox/ToolboxAppContainer.kt`; do
  not add Hilt unless explicitly requested.
- Keep [ARCHITECTURE.md](docs/ARCHITECTURE.md) in sync, but verify roadmap claims against code
  before treating them as implemented.
- When modifying code, run `./gradlew ktlintFormat` before any Kotlin lint command.

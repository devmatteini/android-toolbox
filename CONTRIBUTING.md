# Contributing

Welcome, and thanks for considering a contribution to `Toolbox`.

## Development

1. Fork this repository.
2. Clone your fork.

```shell
git clone https://github.com/<username>/android-toolbox && cd android-toolbox
```

3. Create a branch with `git checkout -b my-branch`.
4. Configure git hooks so pre-commit runs formatting.

```shell
make configure-hooks
```

5. Build the project with `make build`.

Other `make` targets:

- `check`: run ktlint, unit tests, and debug build
- `test`: run unit tests
- `android-test`: run connected Android tests
- `lint`: run ktlint
- `format`: format code with ktlint
- `release`: build the signed release APK; requires `signing.properties`

Useful command when testing with an Android device or emulator:

- `clear-app-data`: clear installed Toolbox app data

## Code Style

- Run `make format` before committing.
- Keep changes small and focused.
- Prefer simple Kotlin and Compose code.
- Split changes into atomic commits where builds and tests pass.
- Use concise commit messages with format `area: summary`.

## Architecture And Technical Choices

Read [ARCHITECTURE.md](docs/ARCHITECTURE.md) for the product and technical overview.

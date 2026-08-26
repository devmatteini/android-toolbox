# Toolbox

[![CI](https://github.com/devmatteini/android-toolbox/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/devmatteini/android-toolbox/actions/workflows/ci.yml)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/devmatteini/android-toolbox)

Toolbox is an Android app with the everyday tools you actually need in one place:

- Unit Converters – length, weight, volume, temperature, speed, and more
- Currency Converter – built-in exchange rates, with the option to download the latest rates on
  demand from the European Central Bank
- Compass – magnetic-north compass using your device's sensors

It works completely offline on your device.

[Languages](#languages) |
[Installation](#installation) |
[Contributing](#contributing)

<p>
  <img src="./assets/demo/home-screen.png" height="460" alt="Toolbox home screen" />
  <img src="./assets/demo/length-screen.png" height="460" alt="Toolbox length converter screen" />
  <img src="./assets/demo/compass-screen.png" height="460" alt="Toolbox compass screen" />
</p>

## Languages

Toolbox is localized in 🇬🇧 English and 🇮🇹 Italian.

## Installation

Your device needs to run Android version 15+.

### Prebuilt APK

Download the prebuilt version of `Toolbox` from the
[latest release](https://github.com/devmatteini/android-toolbox/releases/latest).

### From source

```shell
git clone https://github.com/devmatteini/android-toolbox && cd android-toolbox
make release
file ./app/build/outputs/apk/release/app-release.apk
```

## Contributing

Read the [CONTRIBUTING.md](CONTRIBUTING.md) guidelines.

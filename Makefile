SHELL := bash
.SHELLFLAGS := -euo pipefail -c

build:
	./gradlew :app:assembleDebug

check:
	./gradlew ktlintCheck test :app:assembleDebug

test:
	./gradlew test

android-test:
	./gradlew :app:connectedDebugAndroidTest

lint:
	./gradlew ktlintCheck

format:
	./gradlew ktlintFormat

release:
	./gradlew :app:assembleRelease

clear-app-data:
	adb shell pm clear com.cosimomatteini.toolbox

configure-hooks:
	git config core.hooksPath .githooks

.PHONY: build check test android-test lint format release clear-app-data configure-hooks

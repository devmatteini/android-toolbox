SHELL := bash
.SHELLFLAGS := -euo pipefail -c

build:
	./gradlew :app:assembleDebug

check:
	./gradlew :buildSrc:ktlintCheck ktlintCheck :currency-rates:test :app:testDebugUnitTest :buildSrc:test :app:assembleDebug

test:
	./gradlew :currency-rates:test :app:testDebugUnitTest :buildSrc:test

android-test:
	./gradlew :app:connectedDebugAndroidTest

lint:
	./gradlew :buildSrc:ktlintCheck ktlintCheck

format:
	./gradlew :buildSrc:ktlintFormat ktlintFormat

release:
	./gradlew :app:assembleRelease

clear-app-data:
	adb shell pm clear com.cosimomatteini.toolbox

configure-hooks:
	git config core.hooksPath .githooks

.PHONY: build check test android-test lint format release clear-app-data configure-hooks

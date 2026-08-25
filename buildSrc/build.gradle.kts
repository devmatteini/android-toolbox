plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.2.10"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

sourceSets {
    named("main") {
        java.srcDir("../currency-rates/src/main/kotlin")
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13.2")
}

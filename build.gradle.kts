import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    `java-library`
    application
    id("com.github.ben-manes.versions") version ("0.51.0")
    kotlin("jvm") version("1.9.22")
}

group = "dev.blocky.twitch"
version = "1.0.0"
description = "A useful bot for Twitch with many cool utility features."

repositories {
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))

    api("com.github.twitch4j:twitch4j:1.19.0")

    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-jackson:2.9.0")

    api("se.michaelthelin.spotify:spotify-web-api-java:8.3.5")

    api("org.json:json:20240205")

    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.squareup.okio:okio-jvm:3.8.0")

    api("org.xerial:sqlite-jdbc:3.45.1.0")

    api("io.github.cdimascio:dotenv-java:3.0.0")

    api("org.apache.commons:commons-lang3:3.14.0")
    api("org.apache.commons:commons-collections4:4.4")

    api("org.slf4j:slf4j-api:2.0.12")
    api("ch.qos.logback:logback-classic:1.4.14")

    compileOnly("com.github.spotbugs:spotbugs-annotations:4.8.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainModule.set("dev.blocky.twitch")
    mainClass.set("dev.blocky.twitch.Main")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Module"] = "dev.blocky.twitch"
        attributes["Main-Class"] = "dev.blocky.twitch.Main"
    }
}

tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "current"

    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    outputFormatter = "plain"
    outputDir = "build/dependencyUpdates"
    reportfileName = "update-log"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

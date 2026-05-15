plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("java-library")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.2.1"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
}

group = "io.github.fcat97"
version = "0.9.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    compileOnly("com.android.tools.build:gradle:8.2.2")

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

gradlePlugin {
    website = "https://github.com/fcat97/i18n"
    vcsUrl = "https://github.com/fcat97/i18n.git"
    plugins {
        create("i18n") {
            id = "io.github.fcat97.i18n"
            implementationClass = "io.github.fcat97.i18n.I18nPlugin"
            displayName = "i18n Plugin"
            description = "Generates Android localization from Google Sheets"
            website = "https://github.com/fcat97/i18n"
            vcsUrl = "https://github.com/fcat97/i18n.git"
            tags.addAll(listOf("android", "i18n", "localization", "google-sheets"))
        }
    }
}

tasks.test {
    useJUnit()
    reports {
        junitXml.required.set(true)
    }
}

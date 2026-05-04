plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "io.github.fcat97"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    plugins {
        create("i18n") {
            id = "io.github.fcat97.i18n"
            implementationClass = "com.portonics.i18n.I18nPlugin"
            displayName = "i18n Plugin"
            description = "Generates Android localization from Google Sheets"
            tags.addAll(listOf("android", "i18n", "localization", "google-sheets"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("i18n") {
            from(components["java"])
            pom {
                name.set("i18n")
                description.set("Generates Android localization from Google Sheets")
                url.set("https://github.com/portonics/i18n-gradle")
            }
        }
    }
    repositories {
        maven {
            url = uri("$layout.buildDirectory.get().asFile/repo")
        }
    }
}

tasks.test {
    useJUnit()
    reports {
        junitXml.required.set(true)
    }
}
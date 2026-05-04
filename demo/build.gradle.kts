plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.github.fcat97.i18n")
}

i18n {
    url("https://docs.google.com/spreadsheets/d/1Bi6BHialN1H4ypGMh8xFm8-xGRrUCMnceuH8GdF61QE/edit?gid=0#gid=0")
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
}

android {
    namespace = "com.example.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.demo"
        minSdk = 24
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}

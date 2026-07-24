import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Build script for the Android capture and encrypted-vault evidence application.
// Coordinates use version-catalog aliases only and are governed by the dependency registry.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

android {
    namespace = "com.toolly.spike.capture"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.toolly.spike.capture"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1-spike"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    // Google ML Kit Document Scanner (primary capture path).
    implementation(libs.mlkit.document.scanner)

    // Google AndroidX UI.
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)

    // Kotlin coroutines.
    implementation(libs.coroutines.android)

    // Encrypted metadata candidate. SQLCipher is isolated to the Android vault adapter.
    implementation(libs.room.runtime)
    implementation(libs.androidx.sqlite)
    implementation(libs.sqlcipher.android)
    ksp(libs.room.compiler)

    // Unit tests.
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    // Google AndroidX test-only libraries; not packaged in the production APK.
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

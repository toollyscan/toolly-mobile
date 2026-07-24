// Build script for the TLY-006B capture spike module.
//
// Coordinates use version-catalog aliases only; no direct version literals appear here.
// Every coordinate below has a corresponding approved entry in config/dependencies/registry.json.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
    }

    buildFeatures {
        compose = true
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
    // ML Kit Document Scanner (primary capture path)
    implementation(libs.mlkit.document.scanner)

    // CameraX (fallback path — currently stub, wired for future implementation)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // Compose UI
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)

    // Coil — thumbnail display; disk cache disabled for all vault-origin content (ADR-0011)
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.coroutines.android)

    // Unit tests — JVM only, no Robolectric required for domain and mapper tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Build script for the TLY-006B capture spike module.
//
// Coordinates use version-catalog aliases only; no direct version literals appear here.
// Every external coordinate below has a corresponding approved entry in
// config/dependencies/registry.json.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.toolly.spike.capture"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.toollyscan.mobile"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1-spike"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    // Toolly-owned provider-neutral capture contract.
    implementation(project(":shared-core"))
    implementation(project(":shared-ui"))

    // ML Kit Document Scanner (primary capture path)
    implementation(libs.mlkit.document.scanner)

    // Compose UI
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)

    // Coroutines
    implementation(libs.coroutines.android)

    // Firebase Authentication (ADR-0004 account boundary; `dev` environment only for now —
    // see docs/architecture/FIREBASE_ENVIRONMENTS.md for the per-environment binding plan).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Google ID-token acquisition (Credential Manager) for Google Sign-In. Already present as a
    // transitive dependency of firebase-auth at these exact versions (see spike-capture/gradle.lockfile);
    // declared explicitly here because this module imports them directly, not just Firebase's own
    // internal use of them.
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Unit tests — JVM only, no Robolectric required for domain and mapper tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    // Google AndroidX test-only libraries; not packaged in the production APK.
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

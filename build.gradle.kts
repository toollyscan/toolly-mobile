// Root build script for the TLY-006B capture spike.
// Plugin declarations use version-catalog aliases so no direct version literal appears
// in this file. All dependencies are registered in config/dependencies/registry.json.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

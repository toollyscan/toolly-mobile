package com.toolly.spike.capture

import android.content.pm.PackageManager
import android.test.InstrumentationTestCase

class ManifestPermissionInstrumentedTest : InstrumentationTestCase() {

    fun testSpikeRequestsNoAndroidPermissions() {
        val context = instrumentation.targetContext
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )

        assertTrue(
            "The ML Kit spike must not request app permissions",
            packageInfo.requestedPermissions.isNullOrEmpty(),
        )
    }
}

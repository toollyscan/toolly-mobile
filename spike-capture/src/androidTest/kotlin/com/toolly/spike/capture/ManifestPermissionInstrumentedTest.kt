package com.toolly.spike.capture

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestPermissionInstrumentedTest {

    @Test
    fun spikeRequestsNoAndroidPermissions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
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

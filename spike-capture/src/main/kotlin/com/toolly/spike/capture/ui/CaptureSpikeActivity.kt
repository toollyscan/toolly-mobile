package com.toolly.spike.capture.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.toolly.spike.capture.camerax.CameraXDocumentScannerAdapter
import com.toolly.spike.capture.domain.DocumentScanner
import com.toolly.spike.capture.mlkit.MlKitDocumentScannerAdapter
import kotlinx.coroutines.launch

/**
 * Entry-point Activity for the TLY-006B capture spike.
 *
 * Selects between [MlKitDocumentScannerAdapter] (default) and [CameraXDocumentScannerAdapter]
 * (fallback) based on Play Services availability. Both adapters implement [DocumentScanner],
 * so the Compose UI works identically regardless of which adapter is active.
 *
 * No document pixels, paths, OCR text, filenames or PII are logged at any level.
 */
class CaptureSpikeActivity : ComponentActivity() {

    // mlKitAdapter is non-null only when Play Services are available.
    // The launcher is always registered (lifecycle requirement) but only wired to the
    // adapter when it is selected; the callback is a no-op when mlKitAdapter is null.
    private var mlKitAdapter: MlKitDocumentScannerAdapter? = null

    private val scanLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
        mlKitAdapter?.onActivityResult(result.resultCode, result.data)
    }

    private lateinit var scanner: DocumentScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scanner = if (isPlayServicesAvailable()) {
            MlKitDocumentScannerAdapter(activity = this).also { adapter ->
                adapter.setLauncher(scanLauncher)
                mlKitAdapter = adapter
            }
        } else {
            CameraXDocumentScannerAdapter()
        }

        setContent {
            MaterialTheme {
                CaptureSpikeScreen(
                    onLaunchCapture = { config, onResult ->
                        lifecycleScope.launch {
                            val result = scanner.launch(config)
                            onResult(result)
                        }
                    },
                )
            }
        }
    }

    private fun isPlayServicesAvailable(): Boolean =
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
}

package com.toolly.spike.capture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.toolly.spike.capture.domain.ScanConfig
import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import com.toolly.spike.capture.domain.ScannedPage

/**
 * Minimal adaptive Compose harness for the TLY-006B capture spike.
 *
 * Layout adapts to available window width:
 * - **Compact (phone)**: stacked — capture button above thumbnail row.
 * - **Expanded (tablet, >= 600 dp)**: side-by-side — capture controls on the left,
 *   thumbnail grid on the right.
 *
 * No document pixels, filenames or PII are stored in Compose state or logged.
 */
@Composable
fun CaptureSpikeScreen(
    onLaunchCapture: (ScanConfig, onResult: (ScanResult) -> Unit) -> Unit,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isExpanded = screenWidthDp >= 600

    var pages by remember { mutableStateOf<List<ScannedPage>>(emptyList()) }
    var isCapturing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val onCaptureClick = {
        if (!isCapturing) {
            isCapturing = true
            statusMessage = null
            onLaunchCapture(ScanConfig()) { result ->
                isCapturing = false
                when (result) {
                    is ScanResult.Success -> {
                        pages = result.pages
                        statusMessage = "Captured ${result.pages.size} page(s)"
                    }
                    is ScanResult.Cancelled -> {
                        statusMessage = "Capture cancelled"
                    }
                    is ScanResult.Failure -> {
                        statusMessage = when (result.error) {
                            is ScanError.ServiceUnavailable ->
                                "Scanner unavailable on this device"
                            is ScanError.PermissionDenied ->
                                "Camera permission required"
                            is ScanError.PartialCapture ->
                                "Partial capture: ${(result.error as ScanError.PartialCapture).capturedPages.size} page(s) saved"
                            is ScanError.Unknown ->
                                "Capture failed"
                        }
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isExpanded) {
            TabletLayout(
                pages = pages,
                isCapturing = isCapturing,
                statusMessage = statusMessage,
                onCaptureClick = onCaptureClick,
            )
        } else {
            PhoneLayout(
                pages = pages,
                isCapturing = isCapturing,
                statusMessage = statusMessage,
                onCaptureClick = onCaptureClick,
            )
        }
    }
}

@Composable
private fun PhoneLayout(
    pages: List<ScannedPage>,
    isCapturing: Boolean,
    statusMessage: String?,
    onCaptureClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CaptureControls(
            isCapturing = isCapturing,
            statusMessage = statusMessage,
            onCaptureClick = onCaptureClick,
        )
        if (pages.isNotEmpty()) {
            ThumbnailGrid(pages = pages, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TabletLayout(
    pages: List<ScannedPage>,
    isCapturing: Boolean,
    statusMessage: String?,
    onCaptureClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CaptureControls(
            isCapturing = isCapturing,
            statusMessage = statusMessage,
            onCaptureClick = onCaptureClick,
            modifier = Modifier.width(280.dp),
        )
        if (pages.isNotEmpty()) {
            ThumbnailGrid(pages = pages, modifier = Modifier.weight(1f))
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No pages yet", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun CaptureControls(
    isCapturing: Boolean,
    statusMessage: String?,
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Toolly Capture Spike",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onCaptureClick,
            enabled = !isCapturing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Scan document")
            }
        }
        statusMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

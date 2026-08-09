package com.toolly.spike.capture.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** One-shot Android request that bridges the shared release shell to native capture. */
internal data class CaptureLaunchRequest(
    val requested: Boolean,
    val consume: () -> Unit,
) {
    companion object {
        val None = CaptureLaunchRequest(
            requested = false,
            consume = {},
        )
    }
}

internal val LocalCaptureLaunchRequest = staticCompositionLocalOf {
    CaptureLaunchRequest.None
}

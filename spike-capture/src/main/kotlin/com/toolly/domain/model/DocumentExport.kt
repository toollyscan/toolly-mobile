package com.toolly.domain.model

import com.toolly.foundation.ToollyErrorCode

/** Provider-neutral export choices shared by Android and future Apple composition roots. */
enum class DocumentExportFormat {
    PDF,
    JPEG,
}

/** Delivery mode selected explicitly by the user. */
enum class DocumentExportDelivery {
    SAVE,
    SHARE,
}

/** User-visible completion state without Android URI, filesystem or provider types. */
sealed interface DocumentExportOutcome {
    data object Success : DocumentExportOutcome

    data object Cancelled : DocumentExportOutcome

    data class Failure(
        val code: ToollyErrorCode,
    ) : DocumentExportOutcome
}

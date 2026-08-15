package com.toolly.domain.usecases

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * Runs a suspend block that's expected to complete synchronously -- these use cases never
 * actually suspend (their repository fakes return immediately), so this avoids depending on
 * `kotlinx-coroutines-test` in commonTest. Matches the pattern shared-ui's iosTest suite already
 * uses for the same reason (see `AppleDocumentScannerTest.kt`'s local `runImmediate`).
 */
internal fun runImmediate(block: suspend () -> Unit) {
    var outcome: Result<Unit>? = null
    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<Unit>) {
                outcome = result
            }
        },
    )
    checkNotNull(outcome) { "Test suspension did not complete synchronously" }.getOrThrow()
}

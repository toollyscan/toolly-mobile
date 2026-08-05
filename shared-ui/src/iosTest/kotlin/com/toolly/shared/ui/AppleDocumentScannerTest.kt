package com.toolly.shared.ui

import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AppleDocumentScannerTest {
    @Test
    fun successPreservesPageOrderBehindOpaqueIdentifiers() {
        val first = "0123456789abcdef0123456789abcdef"
        val second = "fedcba9876543210fedcba9876543210"
        val scanner = AppleDocumentScanner(
            FakeAppleCaptureSession { callback ->
                callback.onSuccess(listOf(first, second))
            },
        )

        val result = runImmediate { scanner.launch(ScanConfig(maxPages = 2)) }

        val success = assertIs<ScanResult.Success>(result)
        assertEquals(listOf(0, 1), success.pages.map { it.index })
        assertEquals(listOf(first, second), success.pages.map { it.assetId.value })
    }

    @Test
    fun invalidProviderIdentifiersFailClosedAndAreReleased() {
        val invalidIds = listOf("a-path-must-never-cross-the-boundary")
        val session = FakeAppleCaptureSession { callback ->
            callback.onSuccess(invalidIds)
        }
        val scanner = AppleDocumentScanner(session)

        val result = runImmediate { scanner.launch(ScanConfig()) }

        assertEquals(ScanResult.Failure(ScanError.InvalidResult), result)
        assertEquals(invalidIds, session.releasedIds)
    }

    @Test
    fun cancellationDoesNotPublishPages() {
        val scanner = AppleDocumentScanner(
            FakeAppleCaptureSession { callback -> callback.onCancelled() },
        )

        assertEquals(ScanResult.Cancelled, runImmediate { scanner.launch(ScanConfig()) })
    }

    @Test
    fun overlappingLaunchFailsBusyWithoutReplacingActiveCallback() {
        val session = FakeAppleCaptureSession()
        val scanner = AppleDocumentScanner(session)
        var firstResult: ScanResult? = null

        suspend { scanner.launch(ScanConfig()) }.startCoroutine(
            object : Continuation<ScanResult> {
                override val context = EmptyCoroutineContext
                override fun resumeWith(result: Result<ScanResult>) {
                    firstResult = result.getOrThrow()
                }
            },
        )

        val secondResult = runImmediate { scanner.launch(ScanConfig()) }

        assertEquals(ScanResult.Failure(ScanError.Busy), secondResult)
        assertNull(firstResult)
        session.callback?.onCancelled()
        assertEquals(ScanResult.Cancelled, firstResult)
    }
}

private class FakeAppleCaptureSession(
    private val onLaunch: ((AppleCaptureCallback) -> Unit)? = null,
) : AppleCaptureSession {
    var callback: AppleCaptureCallback? = null
    var releasedIds: List<String> = emptyList()

    override fun launch(maxPages: Int, callback: AppleCaptureCallback) {
        this.callback = callback
        onLaunch?.invoke(callback)
    }

    override fun release(temporaryAssetIds: List<String>) {
        releasedIds = temporaryAssetIds
    }
}

private fun <T> runImmediate(block: suspend () -> T): T {
    var outcome: Result<T>? = null
    block.startCoroutine(
        object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                outcome = result
            }
        },
    )
    return checkNotNull(outcome) { "Test suspension did not complete synchronously" }.getOrThrow()
}

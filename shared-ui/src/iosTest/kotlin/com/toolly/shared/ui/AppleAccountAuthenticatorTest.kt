package com.toolly.shared.ui

import com.toolly.shared.auth.AuthError
import com.toolly.shared.auth.AuthResult
import com.toolly.shared.auth.PhoneVerificationId
import com.toolly.shared.auth.PhoneVerificationResult
import com.toolly.shared.auth.ToollyAccountId
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AppleAccountAuthenticatorTest {
    @Test
    fun phoneVerificationCodeSentCarriesOpaqueVerificationId() {
        val authenticator = AppleAccountAuthenticator(
            FakeSession(onSendPhoneVerificationCode = { _, callback -> callback.onCodeSent("verification-1") }),
        )

        val result = runImmediate { authenticator.sendPhoneVerificationCode("+15551234567") }

        val sent = assertIs<PhoneVerificationResult.CodeSent>(result)
        assertEquals("verification-1", sent.id.value)
    }

    @Test
    fun phoneVerificationFailureMapsAllowlistedErrorCode() {
        val authenticator = AppleAccountAuthenticator(
            FakeSession(onSendPhoneVerificationCode = { _, callback -> callback.onFailure("rate_limited") }),
        )

        val result = runImmediate { authenticator.sendPhoneVerificationCode("+15551234567") }

        val failure = assertIs<PhoneVerificationResult.Failure>(result)
        assertEquals(AuthError.RateLimited, failure.error)
    }

    @Test
    fun unrecognizedErrorCodeDegradesToUnknownRatherThanCrashing() {
        val authenticator = AppleAccountAuthenticator(
            FakeSession(onSignInWithEmail = { _, _, callback -> callback.onFailure("something_new") }),
        )

        val result = runImmediate { authenticator.signInWithEmail("a@example.com", "hunter2") }

        val failure = assertIs<AuthResult.Failure>(result)
        assertEquals(AuthError.Unknown, failure.error)
    }

    @Test
    fun successfulSignInCarriesOpaqueAccountIdAndNewAccountFlag() {
        val authenticator = AppleAccountAuthenticator(
            FakeSession(
                onCreateAccountWithEmail = { _, _, callback ->
                    callback.onSuccess("toolly-account-1", isNewAccount = true)
                },
            ),
        )

        val result = runImmediate { authenticator.createAccountWithEmail("a@example.com", "hunter2") }

        val success = assertIs<AuthResult.Success>(result)
        assertEquals(ToollyAccountId("toolly-account-1"), success.accountId)
        assertEquals(true, success.isNewAccount)
    }

    @Test
    fun currentAccountIdReflectsTheSessionsOpaqueValue() {
        val signedOut = AppleAccountAuthenticator(FakeSession(currentAccountId = null))
        val signedIn = AppleAccountAuthenticator(FakeSession(currentAccountId = "toolly-account-1"))

        assertNull(signedOut.currentAccountId)
        assertEquals(ToollyAccountId("toolly-account-1"), signedIn.currentAccountId)
    }

    @Test
    fun signOutDelegatesToTheSession() {
        var signedOut = false
        val authenticator = AppleAccountAuthenticator(FakeSession(onSignOut = { signedOut = true }))

        runImmediate { authenticator.signOut() }

        assertEquals(true, signedOut)
    }

    @Test
    fun googleAndAppleAndConfirmPhoneRouteThroughTheSameAuthResultCallback() {
        val authenticator = AppleAccountAuthenticator(
            FakeSession(
                onSignInWithGoogle = { _, callback -> callback.onSuccess("toolly-account-1", false) },
                onSignInWithApple = { _, _, callback -> callback.onSuccess("toolly-account-2", false) },
                onConfirmPhoneVerificationCode = { _, _, callback -> callback.onSuccess("toolly-account-3", true) },
            ),
        )

        val google = assertIs<AuthResult.Success>(runImmediate { authenticator.signInWithGoogle("id-token") })
        val apple = assertIs<AuthResult.Success>(
            runImmediate { authenticator.signInWithApple("id-token", "nonce") },
        )
        val phone = assertIs<AuthResult.Success>(
            runImmediate { authenticator.confirmPhoneVerificationCode(PhoneVerificationId("v-1"), "123456") },
        )

        assertEquals("toolly-account-1", google.accountId.value)
        assertEquals("toolly-account-2", apple.accountId.value)
        assertEquals("toolly-account-3", phone.accountId.value)
        assertEquals(true, phone.isNewAccount)
    }
}

private class FakeSession(
    private val currentAccountId: String? = null,
    private val onSendPhoneVerificationCode: ((String, ApplePhoneVerificationCallback) -> Unit)? = null,
    private val onConfirmPhoneVerificationCode: ((String, String, AppleAuthResultCallback) -> Unit)? = null,
    private val onSignInWithEmail: ((String, String, AppleAuthResultCallback) -> Unit)? = null,
    private val onCreateAccountWithEmail: ((String, String, AppleAuthResultCallback) -> Unit)? = null,
    private val onSignInWithGoogle: ((String, AppleAuthResultCallback) -> Unit)? = null,
    private val onSignInWithApple: ((String, String, AppleAuthResultCallback) -> Unit)? = null,
    private val onSignOut: (() -> Unit)? = null,
) : AppleAccountAuthenticatorSession {
    override fun sendPhoneVerificationCode(e164PhoneNumber: String, callback: ApplePhoneVerificationCallback) {
        onSendPhoneVerificationCode?.invoke(e164PhoneNumber, callback)
    }

    override fun confirmPhoneVerificationCode(
        verificationId: String,
        code: String,
        callback: AppleAuthResultCallback,
    ) {
        onConfirmPhoneVerificationCode?.invoke(verificationId, code, callback)
    }

    override fun signInWithEmail(email: String, password: String, callback: AppleAuthResultCallback) {
        onSignInWithEmail?.invoke(email, password, callback)
    }

    override fun createAccountWithEmail(email: String, password: String, callback: AppleAuthResultCallback) {
        onCreateAccountWithEmail?.invoke(email, password, callback)
    }

    override fun signInWithGoogle(googleIdToken: String, callback: AppleAuthResultCallback) {
        onSignInWithGoogle?.invoke(googleIdToken, callback)
    }

    override fun signInWithApple(appleIdToken: String, nonce: String, callback: AppleAuthResultCallback) {
        onSignInWithApple?.invoke(appleIdToken, nonce, callback)
    }

    override fun signOut() {
        onSignOut?.invoke()
    }

    override fun currentAccountId(): String? = currentAccountId
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

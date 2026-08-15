package com.toolly.shared.ui

import com.toolly.shared.auth.AccountAuthenticator
import com.toolly.shared.auth.AuthError
import com.toolly.shared.auth.AuthResult
import com.toolly.shared.auth.PhoneVerificationId
import com.toolly.shared.auth.PhoneVerificationResult
import com.toolly.shared.auth.ToollyAccountId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Objective-C-compatible boundary implemented by the first-party Swift host's Firebase
 * integration (ADR-0004). Mirrors [AppleCaptureSession]'s shape exactly: callback-based rather
 * than suspend (suspend functions don't cross into Swift cleanly), and only opaque strings/
 * primitives cross the boundary -- no Firebase types, no shared-core sealed classes.
 */
interface AppleAccountAuthenticatorSession {
    fun sendPhoneVerificationCode(e164PhoneNumber: String, callback: ApplePhoneVerificationCallback)

    fun confirmPhoneVerificationCode(verificationId: String, code: String, callback: AppleAuthResultCallback)

    fun signInWithEmail(email: String, password: String, callback: AppleAuthResultCallback)

    fun createAccountWithEmail(email: String, password: String, callback: AppleAuthResultCallback)

    fun signInWithGoogle(googleIdToken: String, callback: AppleAuthResultCallback)

    fun signInWithApple(appleIdToken: String, nonce: String, callback: AppleAuthResultCallback)

    fun signOut()

    /** The signed-in account's opaque id, or `null` if no session is active. */
    fun currentAccountId(): String?
}

/** Terminal callback for [AppleAccountAuthenticatorSession.sendPhoneVerificationCode]. */
interface ApplePhoneVerificationCallback {
    fun onCodeSent(verificationId: String)
    fun onFailure(errorCode: String)
}

/** Terminal callback shared by every sign-in/create-account/confirm operation. */
interface AppleAuthResultCallback {
    fun onSuccess(accountId: String, isNewAccount: Boolean)
    fun onFailure(errorCode: String)
}

/**
 * Provider-neutral [AccountAuthenticator] adapter around the Swift-owned Firebase session
 * (ADR-0004). See [FirebaseAccountAuthenticator][com.toolly.spike.capture.firebase.FirebaseAccountAuthenticator]
 * for the Android equivalent and the same interim canonical-ID-minting caveat: the Swift session
 * is expected to mint and persist a device-local id per Firebase UID until a real server-side
 * minting authority exists, not yet the cross-device-portable identity ADR-0004 describes.
 */
internal class AppleAccountAuthenticator(
    private val session: AppleAccountAuthenticatorSession,
) : AccountAuthenticator {

    override val currentAccountId: ToollyAccountId?
        get() = session.currentAccountId()?.let(::ToollyAccountId)

    override suspend fun sendPhoneVerificationCode(
        e164PhoneNumber: String,
    ): PhoneVerificationResult = suspendCoroutine { continuation ->
        session.sendPhoneVerificationCode(
            e164PhoneNumber,
            object : ApplePhoneVerificationCallback {
                override fun onCodeSent(verificationId: String) {
                    continuation.resume(
                        PhoneVerificationResult.CodeSent(PhoneVerificationId(verificationId)),
                    )
                }

                override fun onFailure(errorCode: String) {
                    continuation.resume(PhoneVerificationResult.Failure(errorCode.toAuthError()))
                }
            },
        )
    }

    override suspend fun confirmPhoneVerificationCode(
        id: PhoneVerificationId,
        code: String,
    ): AuthResult = awaitAuthResult { callback ->
        session.confirmPhoneVerificationCode(id.value, code, callback)
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult =
        awaitAuthResult { callback -> session.signInWithEmail(email, password, callback) }

    override suspend fun createAccountWithEmail(email: String, password: String): AuthResult =
        awaitAuthResult { callback -> session.createAccountWithEmail(email, password, callback) }

    override suspend fun signInWithGoogle(googleIdToken: String): AuthResult =
        awaitAuthResult { callback -> session.signInWithGoogle(googleIdToken, callback) }

    override suspend fun signInWithApple(appleIdToken: String, nonce: String): AuthResult =
        awaitAuthResult { callback -> session.signInWithApple(appleIdToken, nonce, callback) }

    override suspend fun signOut() {
        session.signOut()
    }

    private suspend fun awaitAuthResult(
        launch: (AppleAuthResultCallback) -> Unit,
    ): AuthResult = suspendCoroutine { continuation ->
        launch(
            object : AppleAuthResultCallback {
                override fun onSuccess(accountId: String, isNewAccount: Boolean) {
                    continuation.resume(AuthResult.Success(ToollyAccountId(accountId), isNewAccount))
                }

                override fun onFailure(errorCode: String) {
                    continuation.resume(AuthResult.Failure(errorCode.toAuthError()))
                }
            },
        )
    }
}

/**
 * Allowlisted string codes the Swift session reports failures with, mirroring [AuthError]'s
 * cases exactly (ADR-0004 point 8: only one of these known codes crosses the boundary, never a
 * raw provider/NSError message). An unrecognized code maps to [AuthError.Unknown] rather than
 * throwing, so a typo on the Swift side degrades to a generic error instead of crashing.
 */
private fun String.toAuthError(): AuthError = when (this) {
    "network_unavailable" -> AuthError.NetworkUnavailable
    "invalid_credential" -> AuthError.InvalidCredential
    "incorrect_code" -> AuthError.IncorrectCode
    "expired_code" -> AuthError.ExpiredCode
    "account_already_exists" -> AuthError.AccountAlreadyExists
    "account_not_found" -> AuthError.AccountNotFound
    "rate_limited" -> AuthError.RateLimited
    "requires_recent_login" -> AuthError.RequiresRecentLogin
    "not_supported_on_platform" -> AuthError.NotSupportedOnPlatform
    "cancelled" -> AuthError.Cancelled
    else -> AuthError.Unknown
}

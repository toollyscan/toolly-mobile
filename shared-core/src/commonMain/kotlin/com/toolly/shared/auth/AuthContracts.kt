package com.toolly.shared.auth

import kotlin.jvm.JvmInline

/**
 * Provider-neutral port for account authentication (ADR-0004).
 *
 * Implementations own every platform/SDK integration -- Firebase types, provider exceptions,
 * tokens, credentials and raw provider error messages must not cross this boundary. Domain and UI
 * code depend only on this interface and the Toolly-owned types below.
 *
 * Phone verification is two calls because the provider round-trip (send code, then confirm code)
 * cannot be collapsed into one suspend call without losing the "resend" and "wrong code, retry"
 * states the UI already models (see `PhoneEntryScreen` / `OtpVerificationScreen`).
 */
interface AccountAuthenticator {
    /** Starts phone verification. Implementations must not log or persist [e164PhoneNumber]. */
    suspend fun sendPhoneVerificationCode(e164PhoneNumber: String): PhoneVerificationResult

    /** Confirms a code sent by [sendPhoneVerificationCode]. */
    suspend fun confirmPhoneVerificationCode(id: PhoneVerificationId, code: String): AuthResult

    suspend fun signInWithEmail(email: String, password: String): AuthResult

    suspend fun createAccountWithEmail(email: String, password: String): AuthResult

    /**
     * Exchanges an already-obtained Google ID token for a Toolly session.
     *
     * Obtaining [googleIdToken] (the platform sign-in UI/consent flow) is not this port's
     * responsibility -- it is a separate platform concern that hands the token to this method.
     */
    suspend fun signInWithGoogle(googleIdToken: String): AuthResult

    /**
     * Exchanges an already-obtained Apple identity token for a Toolly session.
     *
     * iOS-only per ADR-0004 ("Google and Apple Sign In on iOS"); an adapter with no Apple
     * integration must return [AuthError.NotSupportedOnPlatform], never a silent success.
     */
    suspend fun signInWithApple(appleIdToken: String, nonce: String): AuthResult

    /** Ends the current session. A no-op if nothing is signed in. */
    suspend fun signOut()

    /** The signed-in account, or `null` if no session is active. */
    val currentAccountId: ToollyAccountId?
}

/**
 * Toolly's own canonical account identity (ADR-0004 point 3) -- never a Firebase UID.
 *
 * Interim note: minting a canonical ID that is stable across a user's devices requires a
 * server-side authority (e.g. a Cloud Function mapping provider UID -> `ToollyAccountId`), which
 * does not exist in this repository yet. Until it does, adapters mint and persist a
 * device-local id the first time a given provider credential signs in successfully -- this is
 * NOT yet a cross-device-portable canonical identity, and must not be treated as one. See the
 * adapter's own doc comment for the concrete interim behavior and the follow-up this blocks on.
 */
@JvmInline
value class ToollyAccountId(val value: String) {
    init {
        require(value.isNotBlank()) { "ToollyAccountId must not be blank" }
    }
}

/** Opaque handle for one in-flight phone verification attempt. Not a provider session ID. */
@JvmInline
value class PhoneVerificationId(val value: String) {
    init {
        require(value.isNotBlank()) { "PhoneVerificationId must not be blank" }
    }
}

sealed class PhoneVerificationResult {
    data class CodeSent(val id: PhoneVerificationId) : PhoneVerificationResult()
    data class Failure(val error: AuthError) : PhoneVerificationResult()
}

sealed class AuthResult {
    data class Success(val accountId: ToollyAccountId, val isNewAccount: Boolean) : AuthResult()
    data class Failure(val error: AuthError) : AuthResult()
}

/**
 * Allowlisted, non-sensitive authentication errors (ADR-0004 point 8).
 *
 * These values never contain phone numbers, email addresses, OTPs, passwords, tokens or raw
 * provider exception messages.
 */
sealed class AuthError {
    data object NetworkUnavailable : AuthError()
    data object InvalidCredential : AuthError()
    data object IncorrectCode : AuthError()
    data object ExpiredCode : AuthError()
    data object AccountAlreadyExists : AuthError()
    data object AccountNotFound : AuthError()
    data object RateLimited : AuthError()
    data object RequiresRecentLogin : AuthError()
    data object NotSupportedOnPlatform : AuthError()

    /**
     * The user dismissed a provider's own consent/account-picker UI (e.g. Android's Credential
     * Manager sheet) without completing sign-in. Distinct from [Unknown] so the UI can show a
     * neutral "cancelled" message instead of a scary generic failure -- the user took a deliberate
     * action, not something went wrong.
     */
    data object Cancelled : AuthError()
    data object Unknown : AuthError()
}

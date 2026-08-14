package com.toolly.spike.capture.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.toolly.shared.auth.AccountAuthenticator
import com.toolly.shared.auth.AuthError
import com.toolly.shared.auth.AuthResult
import com.toolly.shared.auth.PhoneVerificationId
import com.toolly.shared.auth.PhoneVerificationResult
import com.toolly.shared.auth.ToollyAccountId
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Real Firebase Authentication adapter for [AccountAuthenticator] (ADR-0004), bound to the
 * `toollyscan-dev` Firebase project (see docs/architecture/FIREBASE_ENVIRONMENTS.md).
 *
 * ## Interim canonical-ID minting
 * ADR-0004 point 3 requires a server-assigned, cross-device-portable `ToollyAccountId`, but no
 * server-side minting authority (e.g. a Cloud Function mapping a Firebase UID to a canonical ID)
 * exists in this repository yet. Until it does, this adapter mints a random device-local ID the
 * first time a given Firebase UID signs in successfully, and persists the UID -> [ToollyAccountId]
 * mapping in plain [android.content.SharedPreferences] -- the ID itself is an opaque identifier,
 * not a secret, unlike the tokens/passwords Firebase's own SDK already keeps out of our code
 * entirely. **This is not yet a cross-device-portable canonical identity**: signing into the same
 * provider account on a second device today mints a second, different local ID. Closing that gap
 * (a real backend component) is separate, unstarted follow-up work -- do not treat the identifier
 * this class returns as satisfying ADR-0004 in full.
 *
 * ## Phone verification and SMS auto-retrieval
 * Android can auto-verify an SMS code before the user ever types one ([onVerificationCompleted]),
 * which this adapter handles by signing in immediately and caching the result keyed by the local
 * [PhoneVerificationId], so a later [confirmPhoneVerificationCode] call (with whatever code, if
 * any, the UI still collected) returns the already-completed result instead of re-deriving a
 * credential from a code that was never actually needed.
 */
class FirebaseAccountAuthenticator(
    private val activity: Activity,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AccountAuthenticator {

    private val preferences = activity.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    // In-flight/completed phone verifications, keyed by the local id returned to the caller.
    // Removed once confirmed or superseded by a fresh sendPhoneVerificationCode call.
    private val pendingVerifications = mutableMapOf<String, PendingVerification>()

    override val currentAccountId: ToollyAccountId?
        get() = firebaseAuth.currentUser?.uid?.let(::localAccountIdFor)

    override suspend fun sendPhoneVerificationCode(
        e164PhoneNumber: String,
    ): PhoneVerificationResult = suspendCancellableCoroutine { cont ->
        val localId = UUID.randomUUID().toString()

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredentialAsync(credential) { result ->
                    pendingVerifications[localId] = PendingVerification.AutoVerified(result)
                }
                if (cont.isActive) {
                    cont.resume(PhoneVerificationResult.CodeSent(PhoneVerificationId(localId)))
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                if (cont.isActive) {
                    cont.resume(PhoneVerificationResult.Failure(mapAuthError(exception)))
                }
            }

            override fun onCodeSent(
                firebaseVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                pendingVerifications[localId] =
                    PendingVerification.AwaitingCode(firebaseVerificationId)
                if (cont.isActive) {
                    cont.resume(PhoneVerificationResult.CodeSent(PhoneVerificationId(localId)))
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(e164PhoneNumber)
            .setTimeout(PHONE_VERIFICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun confirmPhoneVerificationCode(
        id: PhoneVerificationId,
        code: String,
    ): AuthResult {
        val pending = pendingVerifications[id.value]
            ?: return AuthResult.Failure(AuthError.ExpiredCode)

        return when (pending) {
            is PendingVerification.AutoVerified -> {
                pendingVerifications.remove(id.value)
                pending.result
            }

            is PendingVerification.AwaitingCode -> {
                val credential = PhoneAuthProvider.getCredential(pending.firebaseVerificationId, code)
                val result = signInWithCredentialSuspend(credential)
                pendingVerifications.remove(id.value)
                result
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).awaitResult()
        result.toAuthResult(isNewAccount = false)
    } catch (exception: Exception) {
        AuthResult.Failure(mapAuthError(exception))
    }

    override suspend fun createAccountWithEmail(email: String, password: String): AuthResult = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).awaitResult()
        result.toAuthResult(isNewAccount = true)
    } catch (exception: Exception) {
        AuthResult.Failure(mapAuthError(exception))
    }

    override suspend fun signInWithGoogle(googleIdToken: String): AuthResult =
        signInWithCredentialSuspend(GoogleAuthProvider.getCredential(googleIdToken, null))

    // Apple Sign In is iOS-only per ADR-0004 ("Google and Apple Sign In on iOS"); this adapter
    // has no Apple integration and must say so rather than silently failing some other way.
    override suspend fun signInWithApple(appleIdToken: String, nonce: String): AuthResult =
        AuthResult.Failure(AuthError.NotSupportedOnPlatform)

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun signInWithCredentialSuspend(credential: AuthCredential): AuthResult = try {
        firebaseAuth.signInWithCredential(credential).awaitResult().toAuthResult(isNewAccount = null)
    } catch (exception: Exception) {
        AuthResult.Failure(mapAuthError(exception))
    }

    private fun signInWithCredentialAsync(
        credential: AuthCredential,
        onComplete: (AuthResult) -> Unit,
    ) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            val result = if (task.isSuccessful) {
                task.result?.toAuthResult(isNewAccount = null) ?: AuthResult.Failure(AuthError.Unknown)
            } else {
                AuthResult.Failure(mapAuthError(task.exception ?: RuntimeException("unknown failure")))
            }
            onComplete(result)
        }
    }

    /** [isNewAccount] `null` means "ask the provider result", used where Firebase reports it. */
    private fun com.google.firebase.auth.AuthResult.toAuthResult(isNewAccount: Boolean?): AuthResult {
        val uid = user?.uid ?: return AuthResult.Failure(AuthError.Unknown)
        return AuthResult.Success(
            accountId = localAccountIdFor(uid),
            isNewAccount = isNewAccount ?: (additionalUserInfo?.isNewUser ?: false),
        )
    }

    private fun localAccountIdFor(firebaseUid: String): ToollyAccountId {
        val key = ACCOUNT_ID_KEY_PREFIX + firebaseUid
        val existing = preferences.getString(key, null)
        if (existing != null) return ToollyAccountId(existing)
        val minted = UUID.randomUUID().toString()
        preferences.edit().putString(key, minted).apply()
        return ToollyAccountId(minted)
    }

    private sealed class PendingVerification {
        data class AwaitingCode(val firebaseVerificationId: String) : PendingVerification()
        data class AutoVerified(val result: AuthResult) : PendingVerification()
    }

    private companion object {
        const val PREFERENCES_NAME = "toolly_auth_preferences"
        const val ACCOUNT_ID_KEY_PREFIX = "toolly_account_id_for_"
        const val PHONE_VERIFICATION_TIMEOUT_SECONDS = 60L
    }
}

/**
 * Allowlisted, non-sensitive mapping from a Firebase exception to [AuthError] (ADR-0004 point 8).
 * Never reads or forwards [Throwable.message] -- Firebase error messages can include the email,
 * phone number or other identity data that triggered them.
 */
private fun mapAuthError(exception: Throwable): AuthError = when (exception) {
    is FirebaseNetworkException -> AuthError.NetworkUnavailable
    is FirebaseTooManyRequestsException -> AuthError.RateLimited
    is FirebaseAuthUserCollisionException -> AuthError.AccountAlreadyExists
    is FirebaseAuthInvalidUserException -> AuthError.AccountNotFound
    is FirebaseAuthRecentLoginRequiredException -> AuthError.RequiresRecentLogin
    is FirebaseAuthInvalidCredentialsException -> when (exception.errorCode) {
        "ERROR_INVALID_VERIFICATION_CODE" -> AuthError.IncorrectCode
        else -> AuthError.InvalidCredential
    }
    is FirebaseAuthException -> when (exception.errorCode) {
        "ERROR_SESSION_EXPIRED" -> AuthError.ExpiredCode
        "ERROR_TOO_MANY_REQUESTS" -> AuthError.RateLimited
        else -> AuthError.Unknown
    }
    else -> AuthError.Unknown
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            cont.resume(task.result)
        } else {
            cont.resumeWithException(task.exception ?: RuntimeException("Task failed with no exception"))
        }
    }
}

package com.toolly.spike.capture.google

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.toolly.spike.capture.R

/**
 * Obtains a Google ID token via Android's Credential Manager -- a separate platform concern from
 * exchanging that token for a Firebase session (see
 * [AccountAuthenticator.signInWithGoogle][com.toolly.shared.auth.AccountAuthenticator.signInWithGoogle]'s
 * own doc comment for why this port takes an already-obtained token rather than owning the
 * consent UI itself). No Firebase types here -- this is pure Android/Google Identity Services,
 * so it deliberately does NOT live under the `firebase/` source-boundary path.
 *
 * [webClientId] defaults to the OAuth web client id the `google-services` Gradle plugin
 * generates from `google-services.json` (`R.string.default_web_client_id`) -- not a secret, the
 * same non-sensitive client identifier docs/operations/FIREBASE_IAC_AND_ACCESS.md already
 * documents this repo's convention for.
 */
class GoogleIdTokenProvider(
    private val activity: Activity,
    private val webClientId: String = activity.getString(R.string.default_web_client_id),
) {
    private val credentialManager = CredentialManager.create(activity)

    sealed class Result {
        data class Success(val idToken: String) : Result()
        data object Cancelled : Result()
        data object Failure : Result()
    }

    suspend fun requestIdToken(): Result {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.Success(googleIdTokenCredential.idToken)
            } else {
                Result.Failure
            }
        } catch (cancelled: GetCredentialCancellationException) {
            Result.Cancelled
        } catch (failure: GetCredentialException) {
            Result.Failure
        }
    }
}

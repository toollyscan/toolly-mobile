package com.toolly.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.toolly.shared.auth.AuthError
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.apple_sign_in
import com.toolly.shared.resources.auth_error_account_exists
import com.toolly.shared.resources.auth_error_account_not_found
import com.toolly.shared.resources.auth_error_expired_code
import com.toolly.shared.resources.auth_error_incorrect_code
import com.toolly.shared.resources.auth_error_invalid_credential
import com.toolly.shared.resources.auth_error_network
import com.toolly.shared.resources.auth_error_not_supported
import com.toolly.shared.resources.auth_error_rate_limited
import com.toolly.shared.resources.auth_error_requires_recent_login
import com.toolly.shared.resources.auth_error_unknown
import com.toolly.shared.resources.back
import com.toolly.shared.resources.back_to_sign_in
import com.toolly.shared.resources.change_phone_number
import com.toolly.shared.resources.checkmark_glyph
import com.toolly.shared.resources.confirm_password_label
import com.toolly.shared.resources.create_account
import com.toolly.shared.resources.create_account_description
import com.toolly.shared.resources.create_account_title
import com.toolly.shared.resources.create_an_account
import com.toolly.shared.resources.email_hint
import com.toolly.shared.resources.email_label
import com.toolly.shared.resources.email_optional_label
import com.toolly.shared.resources.email_sign_in_description
import com.toolly.shared.resources.email_sign_in_title
import com.toolly.shared.resources.forgot_password
import com.toolly.shared.resources.full_name_label
import com.toolly.shared.resources.go_to_home
import com.toolly.shared.resources.google_sign_in
import com.toolly.shared.resources.otp_field_description
import com.toolly.shared.resources.otp_rate_notice
import com.toolly.shared.resources.otp_sent_to
import com.toolly.shared.resources.otp_title
import com.toolly.shared.resources.password_label
import com.toolly.shared.resources.password_mismatch
import com.toolly.shared.resources.phone_country_label
import com.toolly.shared.resources.phone_country_value
import com.toolly.shared.resources.phone_entry_description
import com.toolly.shared.resources.phone_entry_title
import com.toolly.shared.resources.phone_number_hint
import com.toolly.shared.resources.phone_number_label
import com.toolly.shared.resources.phone_verified_label
import com.toolly.shared.resources.profile_complete
import com.toolly.shared.resources.profile_complete_description
import com.toolly.shared.resources.profile_completion_description
import com.toolly.shared.resources.profile_completion_title
import com.toolly.shared.resources.reset_link_sent
import com.toolly.shared.resources.reset_password_description
import com.toolly.shared.resources.reset_password_title
import com.toolly.shared.resources.resend_code
import com.toolly.shared.resources.resend_code_in
import com.toolly.shared.resources.save_profile
import com.toolly.shared.resources.send_reset_link
import com.toolly.shared.resources.send_verification_code
import com.toolly.shared.resources.session_routing_description
import com.toolly.shared.resources.session_routing_title
import com.toolly.shared.resources.sign_in
import com.toolly.shared.resources.terms_privacy_notice
import com.toolly.shared.resources.use_another_sign_in_method
import com.toolly.shared.resources.verify_and_continue
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/**
 * Auth detail screens (`2.x`/`3.x`/`4.x` wireframes) completing the account journey started by
 * [ToollyApp]'s `SignInScreen`/`CreateProfileScreen`.
 *
 * Email/password and pure-phone sign-in now call a real [ToollyUiActions] implementation, which
 * on Android goes through `FirebaseAccountAuthenticator` behind the `AccountAuthenticator` port
 * (ADR-0004) -- these screens show [state.authBusy]/[state.authError] while that's in flight.
 * Google/Apple sign-in and the phone-verification step that follows creating an email/password
 * account (linking a credential to an already-authenticated user, not a fresh sign-in) remain
 * local-only: Google/Apple need a provider consent-UI adapter that doesn't exist yet, and account
 * linking is exactly the work ADR-0004 point 9 defers pending its own spike.
 */

private const val PHONE_DIGIT_COUNT = 10
private const val OTP_RESEND_SECONDS = 30

@Composable
internal fun PhoneEntryScreen(state: ToollyUiState, actions: ToollyUiActions) {
    var phoneNumber by remember(state.destination) { mutableStateOf(state.pendingPhoneNumber.orEmpty()) }
    ScreenColumn {
        Text(stringResource(Res.string.phone_entry_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.phone_entry_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToollyTextField(
            value = stringResource(Res.string.phone_country_value),
            onValueChange = {},
            label = stringResource(Res.string.phone_country_label),
            readOnly = true,
        )
        ToollyTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it.filter(Char::isDigit).take(PHONE_DIGIT_COUNT) },
            label = stringResource(Res.string.phone_number_label),
            supportingText = if (phoneNumber.isEmpty()) stringResource(Res.string.phone_number_hint) else null,
            keyboardType = KeyboardType.Phone,
        )
        AuthErrorText(state.authError)
        PrimaryButtonText(
            label = stringResource(Res.string.send_verification_code),
            enabled = phoneNumber.length == PHONE_DIGIT_COUNT && !state.authBusy,
            busy = state.authBusy,
            onClick = { actions.submitPhoneNumber(phoneNumber) },
        )
        Text(
            stringResource(Res.string.otp_rate_notice),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = actions::authStepBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            Text(stringResource(Res.string.use_another_sign_in_method))
        }
        TermsNotice()
    }
}

@Composable
internal fun OtpVerificationScreen(state: ToollyUiState, actions: ToollyUiActions) {
    var otp by remember(state.destination) { mutableStateOf("") }
    var secondsRemaining by remember(state.pendingPhoneNumber) { mutableIntStateOf(OTP_RESEND_SECONDS) }
    LaunchedEffect(state.pendingPhoneNumber, secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1_000)
            secondsRemaining -= 1
        }
    }
    val phoneNumber = state.pendingPhoneNumber.orEmpty()
    ScreenColumn {
        Text(stringResource(Res.string.otp_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.otp_sent_to, maskedPhoneDisplay(phoneNumber)),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToollyOtpField(
            value = otp,
            onValueChange = { otp = it },
            length = ToollyUiState.OTP_LENGTH,
            label = stringResource(Res.string.otp_field_description),
        )
        AuthErrorText(state.authError)
        PrimaryButtonText(
            label = stringResource(Res.string.verify_and_continue),
            enabled = otp.length == ToollyUiState.OTP_LENGTH && !state.authBusy,
            busy = state.authBusy,
            onClick = { actions.verifyOtp(otp) },
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (secondsRemaining > 0) {
                Text(
                    stringResource(Res.string.resend_code_in, formatCountdown(secondsRemaining)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                TextButton(onClick = { secondsRemaining = OTP_RESEND_SECONDS }) {
                    Text(stringResource(Res.string.resend_code))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = actions::authStepBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            Text(stringResource(Res.string.change_phone_number))
        }
        TermsNotice()
    }
}

@Composable
internal fun EmailSignInScreen(state: ToollyUiState, actions: ToollyUiActions) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    ScreenColumn {
        Text(stringResource(Res.string.email_sign_in_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.email_sign_in_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToollyTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(Res.string.email_label),
            supportingText = stringResource(Res.string.email_hint),
            keyboardType = KeyboardType.Email,
        )
        ToollyTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(Res.string.password_label),
            isPassword = true,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = actions::selectForgotPassword) {
                Text(stringResource(Res.string.forgot_password))
            }
        }
        AuthErrorText(state.authError)
        PrimaryButtonText(
            label = stringResource(Res.string.sign_in),
            enabled = email.isNotBlank() && password.isNotBlank() && !state.authBusy,
            busy = state.authBusy,
            onClick = { actions.completeAuthentication(email, password) },
        )
        HorizontalDivider()
        SecondaryButton(Res.string.google_sign_in) { actions.authenticate(ToollyAuthenticationMethod.GOOGLE) }
        if (state.appleSignInAvailable) {
            SecondaryButton(Res.string.apple_sign_in) { actions.authenticate(ToollyAuthenticationMethod.APPLE) }
        }
        TextButton(
            onClick = actions::selectCreateAccount,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            Text(stringResource(Res.string.create_an_account))
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = actions::authStepBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            ToollyBackIcon(iconSize = 18.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.back))
        }
        TermsNotice()
    }
}

@Composable
internal fun CreateAccountScreen(state: ToollyUiState, actions: ToollyUiActions) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val mismatch = confirmPassword.isNotEmpty() && confirmPassword != password
    ScreenColumn {
        Text(stringResource(Res.string.create_account_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.create_account_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToollyTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(Res.string.email_label),
            keyboardType = KeyboardType.Email,
        )
        ToollyTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(Res.string.password_label),
            isPassword = true,
        )
        ToollyTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = stringResource(Res.string.confirm_password_label),
            isPassword = true,
            isError = mismatch,
            supportingText = if (mismatch) stringResource(Res.string.password_mismatch) else null,
        )
        Spacer(modifier = Modifier.weight(1f))
        AuthErrorText(state.authError)
        PrimaryButtonText(
            label = stringResource(Res.string.create_account),
            enabled = email.isNotBlank() && password.isNotBlank() && password == confirmPassword && !state.authBusy,
            busy = state.authBusy,
            onClick = { actions.createAccount(email, password) },
        )
        TextButton(
            onClick = actions::authStepBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            ToollyBackIcon(iconSize = 18.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.back))
        }
    }
}

@Composable
internal fun ResetPasswordScreen(actions: ToollyUiActions) {
    var email by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    ScreenColumn {
        Text(stringResource(Res.string.reset_password_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.reset_password_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (submitted) {
            Text(stringResource(Res.string.reset_link_sent))
            Spacer(modifier = Modifier.weight(1f))
            SecondaryButton(Res.string.back_to_sign_in, actions::authStepBack)
        } else {
            ToollyTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(Res.string.email_label),
                keyboardType = KeyboardType.Email,
            )
            PrimaryButtonText(
                label = stringResource(Res.string.send_reset_link),
                enabled = email.contains('@'),
                onClick = { submitted = true },
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = actions::authStepBack,
                modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
            ) {
                ToollyBackIcon(iconSize = 18.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(Res.string.back))
            }
        }
    }
}

@Composable
internal fun ProfileCompletionScreen(state: ToollyUiState, actions: ToollyUiActions) {
    var fullName by remember { mutableStateOf("") }
    val email = state.pendingEmail.orEmpty()
    ScreenColumn {
        Text(stringResource(Res.string.profile_completion_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.profile_completion_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToollyStepProgress(stepCount = 3, currentStep = 1)
        ToollyTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = stringResource(Res.string.full_name_label),
        )
        ToollyTextField(
            value = "+91 ${state.pendingPhoneNumber.orEmpty()}",
            onValueChange = {},
            label = stringResource(Res.string.phone_verified_label),
            readOnly = true,
        )
        ToollyTextField(
            value = email,
            onValueChange = {},
            label = stringResource(Res.string.email_optional_label),
            readOnly = true,
        )
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButtonText(
            label = stringResource(Res.string.save_profile),
            enabled = fullName.isNotBlank(),
            onClick = actions::completeProfile,
        )
    }
}

@Composable
internal fun SessionRoutingScreen(actions: ToollyUiActions) {
    ScreenColumn {
        Text(stringResource(Res.string.session_routing_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.session_routing_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToollyStepProgress(stepCount = 3, currentStep = 2)
        ToollyCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Medium)) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(40.dp)) {
                        drawCircle(color = ToollyColors.Positive, radius = size.minDimension / 2)
                    }
                    Text(
                        stringResource(Res.string.checkmark_glyph),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Column {
                    Text(stringResource(Res.string.profile_complete), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(Res.string.profile_complete_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButtonText(
            label = stringResource(Res.string.go_to_home),
            onClick = actions::finishOnboarding,
        )
    }
}

/** [PrimaryButton] takes a [org.jetbrains.compose.resources.StringResource]; these screens build
 * their label text dynamically (interpolated strings), so this variant takes a resolved [String]. */
@Composable
private fun PrimaryButtonText(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    busy: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.PrimaryActionHeight),
        shape = MaterialTheme.shapes.small,
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(label)
        }
    }
}

/** Shows nothing when [error] is `null` -- callers place this where an error should appear. */
@Composable
private fun AuthErrorText(error: AuthError?) {
    if (error == null) return
    Text(
        authErrorMessage(error),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = ToollySpacing.Small),
    )
}

/**
 * Maps an allowlisted [AuthError] (ADR-0004 point 8 -- never a raw provider message) to a
 * localized, user-facing string.
 */
@Composable
private fun authErrorMessage(error: AuthError): String = stringResource(
    when (error) {
        AuthError.NetworkUnavailable -> Res.string.auth_error_network
        AuthError.InvalidCredential -> Res.string.auth_error_invalid_credential
        AuthError.IncorrectCode -> Res.string.auth_error_incorrect_code
        AuthError.ExpiredCode -> Res.string.auth_error_expired_code
        AuthError.AccountAlreadyExists -> Res.string.auth_error_account_exists
        AuthError.AccountNotFound -> Res.string.auth_error_account_not_found
        AuthError.RateLimited -> Res.string.auth_error_rate_limited
        AuthError.RequiresRecentLogin -> Res.string.auth_error_requires_recent_login
        AuthError.NotSupportedOnPlatform -> Res.string.auth_error_not_supported
        AuthError.Unknown -> Res.string.auth_error_unknown
    },
)

@Composable
private fun TermsNotice() {
    Text(
        stringResource(Res.string.terms_privacy_notice),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun maskedPhoneDisplay(phoneNumber: String): String {
    if (phoneNumber.length < 3) return "+91"
    val lastThree = phoneNumber.takeLast(3)
    return "+91 •••••  ••$lastThree"
}

private fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val secondsText = if (seconds < 10) "0$seconds" else "$seconds"
    return "0$minutes:$secondsText"
}

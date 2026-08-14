package com.toolly.shared.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AuthContractsTest {
    @Test
    fun accountIdentifierRejectsBlankValues() {
        val id = ToollyAccountId("toolly-account-1")

        assertEquals("toolly-account-1", id.value)
        assertFailsWith<IllegalArgumentException> { ToollyAccountId("") }
        assertFailsWith<IllegalArgumentException> { ToollyAccountId("   ") }
    }

    @Test
    fun phoneVerificationIdentifierRejectsBlankValues() {
        val id = PhoneVerificationId("verification-1")

        assertEquals("verification-1", id.value)
        assertFailsWith<IllegalArgumentException> { PhoneVerificationId("") }
    }

    @Test
    fun phoneVerificationResultStaysStructuredAndProviderNeutral() {
        val sent = PhoneVerificationResult.CodeSent(PhoneVerificationId("verification-1"))
        val failed = PhoneVerificationResult.Failure(AuthError.RateLimited)

        val codeSent = assertIs<PhoneVerificationResult.CodeSent>(sent)
        assertEquals("verification-1", codeSent.id.value)

        val failure = assertIs<PhoneVerificationResult.Failure>(failed)
        assertEquals(AuthError.RateLimited, failure.error)
    }

    @Test
    fun authResultStaysStructuredAndProviderNeutral() {
        val success = AuthResult.Success(ToollyAccountId("toolly-account-1"), isNewAccount = true)
        val failure = AuthResult.Failure(AuthError.InvalidCredential)

        val ok = assertIs<AuthResult.Success>(success)
        assertEquals("toolly-account-1", ok.accountId.value)
        assertEquals(true, ok.isNewAccount)

        val err = assertIs<AuthResult.Failure>(failure)
        assertEquals(AuthError.InvalidCredential, err.error)
    }
}

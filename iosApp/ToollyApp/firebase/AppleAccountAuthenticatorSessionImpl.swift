import Foundation
import FirebaseAuth
import ToollySharedUI

/// First-party Firebase implementation of the `AppleAccountAuthenticatorSession` boundary
/// declared in `AppleAuthBridge.kt` (ADR-0004). Mirrors `FirebaseAccountAuthenticator.kt`'s
/// Android counterpart: real `FirebaseAuth` calls for email/password and phone OTP, Google
/// ID-token exchange, and the same interim canonical-ID-minting caveat below. Apple Sign In is
/// intentionally NOT implemented here despite being iOS's own platform capability -- ADR-0004's
/// "Apple Sign In on iOS" scoping still needs the real `ASAuthorizationController` consent-UI
/// flow wired in first (a separate, unstarted piece), so `signInWithApple` reports
/// `not_supported_on_platform` until that lands rather than silently doing the wrong thing.
///
/// ## Interim canonical-ID minting
/// ADR-0004 point 3 requires a server-assigned, cross-device-portable `ToollyAccountId`, but no
/// server-side minting authority exists in this repository yet. Until it does, this class mints
/// and persists a random device-local id per Firebase UID in `UserDefaults` -- functional, but
/// **not** yet the cross-device identity ADR-0004 describes. Signing into the same account on a
/// second device today mints a second, different local id.
///
/// ## Not yet verified against a real Xcode build
/// This file was written without access to Xcode or a macOS toolchain (see this repo's own
/// CI-verification notes for every other iOS-side change this session). The Kotlin side of this
/// boundary (`AppleAuthBridge.kt`) is compiler-verified; this Swift file needs a real Xcode
/// build to confirm it compiles and behaves as intended before it's wired into
/// `ToollyApp.swift` and this repo's `Podfile`/SPM package list gets the Firebase iOS SDK added.
final class AppleAccountAuthenticatorSessionImpl: NSObject, AppleAccountAuthenticatorSession {
    private let accountIdDefaults: UserDefaults

    override init() {
        self.accountIdDefaults = UserDefaults(suiteName: "toolly.auth.account-ids") ?? .standard
        super.init()
    }

    func currentAccountId() -> String? {
        guard let uid = Auth.auth().currentUser?.uid else { return nil }
        return localAccountId(forFirebaseUid: uid)
    }

    func sendPhoneVerificationCode(
        e164PhoneNumber: String,
        callback: ApplePhoneVerificationCallback
    ) {
        PhoneAuthProvider.provider().verifyPhoneNumber(e164PhoneNumber, uiDelegate: nil) { verificationId, error in
            if let error {
                callback.onFailure(errorCode: Self.authErrorCode(for: error))
                return
            }
            guard let verificationId else {
                callback.onFailure(errorCode: "unknown")
                return
            }
            callback.onCodeSent(verificationId: verificationId)
        }
    }

    func confirmPhoneVerificationCode(
        verificationId: String,
        code: String,
        callback: AppleAuthResultCallback
    ) {
        let credential = PhoneAuthProvider.provider().credential(
            withVerificationID: verificationId,
            verificationCode: code
        )
        signIn(with: credential, callback: callback)
    }

    func signInWithEmail(email: String, password: String, callback: AppleAuthResultCallback) {
        Auth.auth().signIn(withEmail: email, password: password) { [weak self] authResult, error in
            self?.deliver(authResult: authResult, error: error, isNewAccount: false, callback: callback)
        }
    }

    func createAccountWithEmail(email: String, password: String, callback: AppleAuthResultCallback) {
        Auth.auth().createUser(withEmail: email, password: password) { [weak self] authResult, error in
            self?.deliver(authResult: authResult, error: error, isNewAccount: true, callback: callback)
        }
    }

    func signInWithGoogle(googleIdToken: String, callback: AppleAuthResultCallback) {
        let credential = GoogleAuthProvider.credential(withIDToken: googleIdToken, accessToken: "")
        signIn(with: credential, callback: callback)
    }

    // Apple Sign In needs a real ASAuthorizationController consent flow (obtaining the identity
    // token this method would exchange) that doesn't exist yet -- see this class's own doc
    // comment. Reporting failure explicitly rather than silently no-op'ing.
    func signInWithApple(appleIdToken: String, nonce: String, callback: AppleAuthResultCallback) {
        callback.onFailure(errorCode: "not_supported_on_platform")
    }

    func signOut() {
        try? Auth.auth().signOut()
    }

    private func signIn(with credential: AuthCredential, callback: AppleAuthResultCallback) {
        Auth.auth().signIn(with: credential) { [weak self] authResult, error in
            self?.deliver(authResult: authResult, error: error, isNewAccount: nil, callback: callback)
        }
    }

    /// `isNewAccount` `nil` means "ask Firebase's own result", used where Firebase reports it
    /// via `additionalUserInfo`; email/password sign-in and creation already know the answer
    /// unconditionally and pass it explicitly instead.
    private func deliver(
        authResult: AuthDataResult?,
        error: Error?,
        isNewAccount: Bool?,
        callback: AppleAuthResultCallback
    ) {
        if let error {
            callback.onFailure(errorCode: Self.authErrorCode(for: error))
            return
        }
        guard let uid = authResult?.user.uid else {
            callback.onFailure(errorCode: "unknown")
            return
        }
        let accountId = localAccountId(forFirebaseUid: uid)
        let resolvedIsNewAccount = isNewAccount ?? (authResult?.additionalUserInfo?.isNewUser ?? false)
        callback.onSuccess(accountId: accountId, isNewAccount: resolvedIsNewAccount)
    }

    private func localAccountId(forFirebaseUid firebaseUid: String) -> String {
        let key = "toolly_account_id_for_\(firebaseUid)"
        if let existing = accountIdDefaults.string(forKey: key) {
            return existing
        }
        let minted = UUID().uuidString.lowercased()
        accountIdDefaults.set(minted, forKey: key)
        return minted
    }

    /// Allowlisted, non-sensitive mapping from a Firebase `NSError` to one of the string codes
    /// `AppleAuthBridge.kt`'s `toAuthError()` recognizes (ADR-0004 point 8) -- never reads or
    /// forwards `error.localizedDescription`, which can include the email/phone that triggered
    /// the error.
    private static func authErrorCode(for error: Error) -> String {
        let nsError = error as NSError
        guard let code = AuthErrorCode(rawValue: nsError.code) else { return "unknown" }
        switch code {
        case .networkError:
            return "network_unavailable"
        case .tooManyRequests:
            return "rate_limited"
        case .emailAlreadyInUse, .credentialAlreadyInUse, .accountExistsWithDifferentCredential:
            return "account_already_exists"
        case .userNotFound:
            return "account_not_found"
        case .requiresRecentLogin:
            return "requires_recent_login"
        case .invalidVerificationCode:
            return "incorrect_code"
        case .sessionExpired:
            return "expired_code"
        case .wrongPassword, .invalidEmail, .invalidCredential, .userDisabled, .invalidPhoneNumber,
             .invalidVerificationID, .missingVerificationCode, .missingVerificationID:
            return "invalid_credential"
        default:
            return "unknown"
        }
    }
}

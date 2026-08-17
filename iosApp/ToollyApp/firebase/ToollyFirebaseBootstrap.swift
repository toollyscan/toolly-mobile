import FirebaseCore

/// The one Firebase call allowed to run outside `AppleAccountAuthenticatorSessionImpl` itself --
/// isolated here so `ToollyApp.swift` (the composition root) never imports Firebase directly,
/// per `config/dependencies/policy.json`'s `firebase-adapter-only` source boundary (Firebase
/// symbols are confined to `/firebase/`-scoped adapter code).
///
/// Must run before anything touches `Auth.auth()` -- `AppleAccountAuthenticatorSessionImpl` does,
/// as soon as it's constructed -- since an unconfigured `FirebaseApp` makes Firebase fail with a
/// fatal error, not a catchable one.
enum ToollyFirebaseBootstrap {
    static func configure() {
        FirebaseApp.configure()
    }
}

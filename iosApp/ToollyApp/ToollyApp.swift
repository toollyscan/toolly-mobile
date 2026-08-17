import SwiftUI
import ToollySharedUI
import UIKit

@main
struct ToollyIOSApplication: App {
    // ToollyFirebaseBootstrap.configure() (not a direct FirebaseCore import/call here -- this
    // file is the composition root, and Firebase symbols are confined to `/firebase/`-scoped
    // adapter code per config/dependencies/policy.json's `firebase-adapter-only` source boundary)
    // must run before anything touches `Auth.auth()`. GoogleService-Info.plist is provisioned by
    // CI from the toollyscan-dev environment secret before this target ever builds (see
    // docs/architecture/FIREBASE_ENVIRONMENTS.md); a real device/local run needs that file present
    // the same way.
    init() {
        ToollyFirebaseBootstrap.configure()
    }

    var body: some Scene {
        WindowGroup {
            ToollyRootView()
                .ignoresSafeArea()
        }
    }
}

private struct ToollyRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // The capture session needs to present on top of the view controller
        // MainViewController(...) is about to create, which doesn't exist until that call
        // returns. This box lets the session resolve it lazily, after assignment below.
        let hostBox = HostViewControllerBox()
        let captureSession = AppleCaptureSessionImpl(
            presentingViewControllerProvider: { hostBox.viewController }
        )
        // Real Firebase auth (TLY-007, #74) and real encrypted local vault (TLY-014 Phase 3) --
        // both previously written but never actually registered in this Xcode project's Sources
        // build phase, so neither was ever part of a real build until now (see this project's own
        // pbxproj history). `resolveTemporaryAsset` mirrors Android's save flow: the vault reads a
        // captured page straight out of capture's own staging directory by temporary asset id.
        let accountAuthenticatorSession = AppleAccountAuthenticatorSessionImpl()
        let documentVaultSession = AppleDocumentVaultSessionImpl(
            resolveTemporaryAsset: { temporaryAssetId in
                captureSession.fileURL(forTemporaryAssetId: temporaryAssetId)
            }
        )
        #if DEBUG
        let viewController = MainViewControllerKt.MainViewController(
            developmentAccessAvailable: true,
            captureSession: captureSession,
            accountAuthenticatorSession: accountAuthenticatorSession,
            documentVaultSession: documentVaultSession
        )
        #else
        let viewController = MainViewControllerKt.MainViewController(
            developmentAccessAvailable: false,
            captureSession: captureSession,
            accountAuthenticatorSession: accountAuthenticatorSession,
            documentVaultSession: documentVaultSession
        )
        #endif
        hostBox.viewController = viewController
        return viewController
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {
    }
}

private final class HostViewControllerBox {
    weak var viewController: UIViewController?
}

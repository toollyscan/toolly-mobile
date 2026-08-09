import SwiftUI
import ToollySharedUI
import UIKit

@main
struct ToollyIOSApplication: App {
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
        #if DEBUG
        let viewController = MainViewControllerKt.MainViewController(
            developmentAccessAvailable: true,
            captureSession: captureSession
        )
        #else
        let viewController = MainViewControllerKt.MainViewController(
            developmentAccessAvailable: false,
            captureSession: captureSession
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

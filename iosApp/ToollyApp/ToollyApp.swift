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
        #if DEBUG
        return MainViewControllerKt.MainViewController(developmentAccessAvailable: true)
        #else
        return MainViewControllerKt.MainViewController()
        #endif
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {
    }
}

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
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {
    }
}

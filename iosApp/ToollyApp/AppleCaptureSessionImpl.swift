import Foundation
import UIKit
import VisionKit
import ToollySharedUI

/// First-party VisionKit implementation of the `AppleCaptureSession` boundary declared in
/// `AppleCaptureBridge.kt`. Owns VisionKit, UIKit and a private, non-backed-up staging directory;
/// only opaque temporary-asset identifiers cross into shared Kotlin.
///
/// `presentingViewControllerProvider` is a closure rather than a stored reference because the
/// host view controller this session presents on top of doesn't exist yet at the point
/// `MainViewController(...)` needs a session to construct -- see `ToollyRootView` in
/// `ToollyApp.swift`, which resolves the closure only after the view controller is created.
final class AppleCaptureSessionImpl: NSObject, AppleCaptureSession {
    private let presentingViewControllerProvider: () -> UIViewController?
    private let storageDirectory: URL
    private var activeCallback: AppleCaptureCallback?

    init(presentingViewControllerProvider: @escaping () -> UIViewController?) {
        self.presentingViewControllerProvider = presentingViewControllerProvider
        let cachesDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
        self.storageDirectory = cachesDirectory.appendingPathComponent(
            "toolly-capture-staging",
            isDirectory: true
        )
        super.init()
        try? FileManager.default.createDirectory(
            at: storageDirectory,
            withIntermediateDirectories: true
        )
    }

    func launch(maxPages: Int32, callback: AppleCaptureCallback) {
        guard activeCallback == nil else {
            callback.onBusy()
            return
        }
        guard VNDocumentCameraViewController.isSupported else {
            callback.onServiceUnavailable()
            return
        }
        guard let host = presentingViewControllerProvider() else {
            callback.onLifecycleEnded()
            return
        }
        activeCallback = callback
        let documentCamera = VNDocumentCameraViewController()
        documentCamera.delegate = self
        host.present(documentCamera, animated: true)
    }

    func release(temporaryAssetIds: [String]) {
        for id in temporaryAssetIds {
            try? FileManager.default.removeItem(at: fileURL(forTemporaryAssetId: id))
        }
    }

    /// Resolves a temporary asset id returned via `onSuccess` back to its staged JPEG file.
    /// Not consumed anywhere yet -- there is no iOS vault to read it into (see issue #48) -- but
    /// this mirrors Android's `TemporaryScanStore.resolve` and is the API whoever builds that
    /// vault next will need.
    func fileURL(forTemporaryAssetId id: String) -> URL {
        storageDirectory.appendingPathComponent("\(id).jpg")
    }

    private func finish(_ callback: AppleCaptureCallback?, _ deliver: (AppleCaptureCallback) -> Void) {
        guard let callback else { return }
        activeCallback = nil
        deliver(callback)
    }
}

extension AppleCaptureSessionImpl: VNDocumentCameraViewControllerDelegate {
    func documentCameraViewController(
        _ controller: VNDocumentCameraViewController,
        didFinishWith scan: VNDocumentCameraScan
    ) {
        let callback = activeCallback
        controller.dismiss(animated: true) { [weak self] in
            guard let self else { return }
            guard scan.pageCount > 0 else {
                self.finish(callback) { $0.onInvalidResult() }
                return
            }
            var stagedIds: [String] = []
            for pageIndex in 0..<scan.pageCount {
                let pageImage = scan.imageOfPage(at: pageIndex)
                guard let jpegData = pageImage.jpegData(compressionQuality: JPEG_QUALITY) else {
                    self.releaseStaged(stagedIds)
                    self.finish(callback) { $0.onStorageFailure() }
                    return
                }
                let id = UUID().uuidString.lowercased()
                do {
                    try jpegData.write(to: self.fileURL(forTemporaryAssetId: id), options: .atomic)
                    stagedIds.append(id)
                } catch {
                    self.releaseStaged(stagedIds)
                    self.finish(callback) { $0.onStorageFailure() }
                    return
                }
            }
            self.finish(callback) { $0.onSuccess(temporaryAssetIds: stagedIds) }
        }
    }

    func documentCameraViewControllerDidCancel(_ controller: VNDocumentCameraViewController) {
        let callback = activeCallback
        controller.dismiss(animated: true) { [weak self] in
            self?.finish(callback) { $0.onCancelled() }
        }
    }

    func documentCameraViewController(
        _ controller: VNDocumentCameraViewController,
        didFailWithError error: Error
    ) {
        let callback = activeCallback
        controller.dismiss(animated: true) { [weak self] in
            self?.finish(callback) { $0.onInvalidResult() }
        }
    }

    private func releaseStaged(_ ids: [String]) {
        release(temporaryAssetIds: ids)
    }
}

private let JPEG_QUALITY: CGFloat = 0.92

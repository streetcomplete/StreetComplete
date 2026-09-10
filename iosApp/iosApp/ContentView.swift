import StreetComplete
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    let incomingUriHandler: IncomingUriHandler

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(incomingUriHandler: incomingUriHandler)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    // SwiftUI recreates View values freely. State keeps the ingress paired with the
    // UIViewController that was created for this scene instead of stranding a URL in
    // a replacement handler.
    @State private var incomingUriHandler = IncomingUriHandler()

    var body: some View {
        ComposeView(incomingUriHandler: incomingUriHandler)
            // Compose handles all insets (and the keyboard) itself
            .ignoresSafeArea()
            .onOpenURL { url in
                incomingUriHandler.submit(uri: url.absoluteString)
            }
    }
}

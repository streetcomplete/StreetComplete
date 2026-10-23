import StreetComplete
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    let pendingUri: Kotlinx_coroutines_coreMutableStateFlow

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(pendingUri: pendingUri)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let pendingUri: Kotlinx_coroutines_coreMutableStateFlow

    var body: some View {
        ComposeView(pendingUri: pendingUri)
            // Compose handles all insets (and the keyboard) itself
            .ignoresSafeArea()
    }
}

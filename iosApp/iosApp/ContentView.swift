import StreetComplete
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    let links: IosAppLinks

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(links: links)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let links: IosAppLinks

    var body: some View {
        ComposeView(links: links)
            // Compose handles all insets (and the keyboard) itself
            .ignoresSafeArea()
    }
}

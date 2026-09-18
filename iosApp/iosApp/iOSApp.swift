import StreetComplete
import SwiftUI

@main
struct iOSApp: App {
    private let links = IosAppLinks()

    init() {
        KoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView(links: links)
                .onOpenURL { links.openUri(uri: $0.absoluteString) }
        }
    }
}

import StreetComplete
import SwiftUI

@main
struct iOSApp: App {
    private let pendingUri = MutableStateFlowKt.MutableStateFlow(value: nil)

    init() {
        KoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView(pendingUri: pendingUri)
                .onOpenURL { pendingUri.value = $0.absoluteString }
        }
    }
}

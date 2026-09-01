import StreetComplete
import BackgroundTasks
import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinKt.doInitKoin()
        BackgroundSyncScheduler.shared.register()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

private final class BackgroundSyncScheduler {
    static let shared = BackgroundSyncScheduler()

    private let identifier = "de.westnordost.streetcomplete.background-sync"

    func register() {
        let registered = BGTaskScheduler.shared.register(
            forTaskWithIdentifier: identifier,
            using: nil
        ) { [weak self] task in
            self?.handle(task)
        }
        precondition(registered, "The iOS background-sync identifier is not permitted")
        schedule()
    }

    private func schedule() {
        let request = BGProcessingTaskRequest(identifier: identifier)
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Unable to schedule StreetComplete background sync: \(error)")
        }
    }

    private func handle(_ task: BGTask) {
        schedule()

        var didComplete = false
        func finish(success: Bool) {
            DispatchQueue.main.async {
                guard !didComplete else { return }
                didComplete = true
                task.setTaskCompleted(success: success)
            }
        }

        let handle = KoinKt.startIosBackgroundSync { success in
            finish(success: success.boolValue)
        }
        task.expirationHandler = {
            handle.cancel()
            finish(success: false)
        }
    }
}

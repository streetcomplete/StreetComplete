package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.sync.IosBackgroundSyncController
import de.westnordost.streetcomplete.data.sync.IosBackgroundSyncHandle
import org.koin.core.Koin
import org.koin.core.context.startKoin

private lateinit var koin: Koin

fun initKoin() {
    val koinApplication = startKoin {
        modules(
            iosModule,
            commonModule,
        )
    }
    koin = koinApplication.koin
    koin.get<ApplicationInitializer>().initialize(schedulePeriodicCleanup = true)
}

fun startIosBackgroundSync(completion: (Boolean) -> Unit): IosBackgroundSyncHandle =
    koin.get<IosBackgroundSyncController>().start(completion)

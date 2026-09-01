package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.sync.IosBackgroundSyncController
import de.westnordost.streetcomplete.data.sync.IosBackgroundSyncHandle
import de.westnordost.streetcomplete.util.error_reporting.IosCrashReportHolder
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
    koin.get<IosCrashReportHolder>().install()
    koin.get<ApplicationInitializer>().initialize(schedulePeriodicCleanup = true)
}

fun startIosBackgroundSync(completion: (Boolean) -> Unit): IosBackgroundSyncHandle =
    koin.get<IosBackgroundSyncController>().start(completion)

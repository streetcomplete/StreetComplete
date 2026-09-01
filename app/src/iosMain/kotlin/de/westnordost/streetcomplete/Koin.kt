package de.westnordost.streetcomplete

import org.koin.core.context.startKoin

fun initKoin() {
    val koinApplication = startKoin {
        modules(
            iosModule,
            commonModule,
        )
    }
    koinApplication.koin.get<ApplicationInitializer>().initialize()
}

package de.westnordost.streetcomplete

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
    koin.get<ApplicationInitializer>().initialize()
}

fun handleIncomingUri(uri: String) {
    koin.get<IncomingUriHandler>().submit(uri)
}

package de.westnordost.streetcomplete

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(
            iosModule,
            commonModule,
        )
    }
}

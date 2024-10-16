package de.westnordost.streetcomplete.data.platform

import org.koin.dsl.module

val platformModule = module {
    factory<InternetConnectionState> { InternetConnectionState(get()) }
}

package de.westnordost.streetcomplete.data

import org.koin.dsl.module

val feedsModule = module {
    factory { FeedsUpdater(get(), get(), get()) }
}

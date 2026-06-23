package de.westnordost.streetcomplete.data.weeklyosm

import org.koin.dsl.module

val weeklyOsmModule = module {
    factory<WeeklyOsmApiClient> { WeeklyOsmApiClientImpl(get(), get()) }
    factory { WeeklyOsmUpdater(get(), get()) }
    factory { WeeklyOsmRssFeedParser() }
}

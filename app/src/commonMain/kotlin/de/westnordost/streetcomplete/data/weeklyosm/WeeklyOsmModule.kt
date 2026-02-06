package de.westnordost.streetcomplete.data.weeklyosm

import org.koin.dsl.module

val weeklyOsmModule = module {
    factory { WeeklyOsmApiClient(get(), get(), get()) }
    factory { WeeklyOsmUpdater(get(), get()) }
}

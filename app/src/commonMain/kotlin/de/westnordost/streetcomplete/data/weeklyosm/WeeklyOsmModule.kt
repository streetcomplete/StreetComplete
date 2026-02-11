package de.westnordost.streetcomplete.data.weeklyosm

import org.koin.dsl.module

private const val WEEKLY_OSM_FEED_URL = "https://weeklyosm.eu/feed"

val weeklyOsmModule = module {
    factory { WeeklyOsmApiClient(get(), WEEKLY_OSM_FEED_URL, get()) }
    factory { WeeklyOsmUpdater(get(), get()) }
    factory { WeeklyOsmRssFeedParser() }
}

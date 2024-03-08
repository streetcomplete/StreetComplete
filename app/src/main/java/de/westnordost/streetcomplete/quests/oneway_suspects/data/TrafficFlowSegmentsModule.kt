package de.westnordost.streetcomplete.quests.oneway_suspects.data

import org.koin.dsl.module

const val ONEWAY_API_URL = "https://www.westnordost.de/streetcomplete/oneway-data-api/"
val trafficFlowSegmentsModule = module {
    factory { TrafficFlowSegmentsApi(get(), ONEWAY_API_URL) }
}

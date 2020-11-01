package de.westnordost.streetcomplete.quests.oneway_suspects.data

import dagger.Module
import dagger.Provides

@Module
object TrafficFlowSegmentsModule {
    const val ONEWAY_API_URL = "https://www.westnordost.de/streetcomplete/oneway-data-api/"

    @Provides
    fun trafficFlowSegmentsApi(): TrafficFlowSegmentsApi = TrafficFlowSegmentsApi(ONEWAY_API_URL)
}
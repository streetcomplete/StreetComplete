package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.quest.QuestType

data class Statistics(
    val questTypes: List<QuestTypeStatistics>,
    val countries: List<CountryStatistics>,
    val rank: Int,
    val daysActive: Int,
    val lastUpdate: Long,
    val isAnalyzing: Boolean
)

data class CountryStatistics(val countryCode: String, val solvedCount: Int, val rank: Int?)

data class QuestTypeStatistics(val questType: QuestType<*>, val solvedCount: Int)

package de.westnordost.streetcomplete.data.user

import java.util.Date

data class Statistics(
    val questTypes: Map<String, Int>,
    val daysActive: Int,
    val lastUpdate: Date,
    val isAnalyzing: Boolean
)

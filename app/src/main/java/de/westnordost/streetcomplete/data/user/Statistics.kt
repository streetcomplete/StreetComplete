package de.westnordost.streetcomplete.data.user

data class Statistics(
    val questTypes: Map<String, Int>,
    val daysActive: Int,
    val lastUpdate: String
)

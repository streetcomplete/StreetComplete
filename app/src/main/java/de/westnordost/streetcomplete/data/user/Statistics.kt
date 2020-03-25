package de.westnordost.streetcomplete.data.user

data class Statistics(
    val amounts: Map<String, Int>,
    val daysActive: Int,
    val lastUpdate: String
)

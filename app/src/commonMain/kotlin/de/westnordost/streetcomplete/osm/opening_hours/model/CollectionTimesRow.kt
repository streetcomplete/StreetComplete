package de.westnordost.streetcomplete.osm.opening_hours.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionTimesRow(var weekdays: Weekdays, var time: Int)

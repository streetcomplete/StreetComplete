package de.westnordost.streetcomplete.data.osm.mapdata

import kotlinx.serialization.Serializable

@Serializable
data class ElementKey(val type: ElementType, val id: Long)

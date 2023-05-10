package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import kotlinx.serialization.Serializable

@Serializable
data class ElementKey(val type: ElementType, val id: Long)

val Element.key get() = ElementKey(type, id)

val ElementGeometryEntry.key get() = ElementKey(elementType, elementId)

val RelationMember.key get() = ElementKey(type, ref)

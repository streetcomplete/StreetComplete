package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.AtpQuestKey
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.quest.Quest

/** Represents one task for the user to contribute by reviewing proposed element creation */
data class CreateElementQuest(
    val id: Long, // may be a stable value associated with a given element, but stability is not promised, should be unique
    val atpEntry: AtpEntry, // At this point it is tightly bound with ATP
    override val type: OsmCreateElementQuestType<*>,
    override val position: LatLon
)  : Quest {
    override val key: AtpQuestKey by lazy { AtpQuestKey(id) }
    override val markerLocations: Collection<LatLon> by lazy { listOf(position) }
    override val geometry: ElementGeometry get() = ElementPointGeometry(position)
}

package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey

/** Represents one task for the user to contribute to a public OSM note */
data class OsmNoteQuest(
    val id: Long,
    override val position: LatLon
) : Quest {
    override val type: QuestType<*> get() = OsmNoteQuestType
    override val key: OsmNoteQuestKey get() = OsmNoteQuestKey(id)
    override val markerLocations: Collection<LatLon> get() = listOf(position)
    override val geometry: ElementGeometry get() = ElementPointGeometry(position)
}

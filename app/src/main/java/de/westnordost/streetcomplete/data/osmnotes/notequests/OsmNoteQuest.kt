package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry

/** Represents one task for the user to contribute to a public OSM note */
data class OsmNoteQuest(
    override var id: Long?,
    override val center: LatLon
) : Quest {
    override val type: QuestType<*> get() = OsmNoteQuestType
    override val markerLocations: Collection<LatLon> get() = listOf(center)
    override val geometry: ElementGeometry get() = ElementPointGeometry(center)
}

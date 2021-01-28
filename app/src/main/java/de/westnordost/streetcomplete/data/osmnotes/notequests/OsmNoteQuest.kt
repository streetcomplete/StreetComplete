package de.westnordost.streetcomplete.data.osmnotes.notequests

import java.util.Date
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry

/** Represents one task for the user to contribute to a public OSM note */
data class OsmNoteQuest(
    override var id: Long?,
    override val center: LatLon,
    override val lastUpdate: Date,
    private val questType: OsmNoteQuestType
) : Quest {

    constructor(note: Note, osmNoteQuestType: OsmNoteQuestType)
        : this(note.id, note.position, Date(), osmNoteQuestType)

    override val type: QuestType<*> get() = questType
    override val markerLocations: Collection<LatLon> get() = listOf(center)
    override val geometry: ElementGeometry get() = ElementPointGeometry(center)
    override var status = QuestStatus.NEW
}

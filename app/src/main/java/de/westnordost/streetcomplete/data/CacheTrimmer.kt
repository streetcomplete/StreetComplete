package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController

class CacheTrimmer(
    private val osmQuestController: OsmQuestController,
    private val noteController: NoteController,
    private val mapDataController: MapDataController,
) {
    fun trimCaches() {
        osmQuestController.trimCache()
        noteController.trimCache()
        mapDataController.trimCache()
    }

    fun clearCaches() {
        mapDataController.clearCache()
        osmQuestController.clearCache()
        noteController.clearCache()
    }
}

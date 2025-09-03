package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.Quest

// TODO multiplatform: this indirection can be removed once OsmNoteQuest is moved to common
interface OsmNoteQuest : Quest {
    val id: Long
}

expect fun createOsmNoteQuest(id: Long, position: LatLon): OsmNoteQuest

package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.saveable.Saver
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.map.MAX_SAVED_TRACKPOINTS
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Which sheet should be shown on the main screen. E.g. a quest form, an overlay form, the form
 *  to create a note or the edit history. */
@Serializable
sealed interface MainSheetSelection {
    @Serializable
    data class Quest(val key: QuestKey) : MainSheetSelection

    @Serializable
    data class Overlay(val name: String, val elementKey: ElementKey? = null) : MainSheetSelection

    @Serializable
    data class CreateNote(val position: LatLon, val trackpoints: List<Trackpoint>? = null) : MainSheetSelection

    @Serializable
    data class EditHistory(val editKey: EditKey) : MainSheetSelection

    companion object {
        val Saver = Saver<MainSheetSelection?, String>(
            save = { selection ->
                selection?.let {
                    // Instance state is limited to about 1 MB per transaction, so only the newest
                    // points of a recorded track are saved (see MainMapTrackState.Saver).
                    val saved = if (it is CreateNote) it.copy(trackpoints = it.trackpoints?.takeLast(MAX_SAVED_TRACKPOINTS)) else it
                    Json.encodeToString<MainSheetSelection>(saved)
                }
            },
            restore = { Json.decodeFromString<MainSheetSelection>(it) },
        )
    }
}

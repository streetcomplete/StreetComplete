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

@Serializable
sealed interface MainBottomSheetSelection {
    @Serializable
    data class Quest(val key: QuestKey) : MainBottomSheetSelection

    @Serializable
    data class Overlay(val name: String, val elementKey: ElementKey? = null) : MainBottomSheetSelection

    @Serializable
    data class CreateNote(val position: LatLon, val trackpoints: List<Trackpoint>? = null) : MainBottomSheetSelection

    /** The edit history sidebar is not a bottom sheet, but never shown together with one */
    @Serializable
    data class EditHistory(val editKey: EditKey) : MainBottomSheetSelection

    companion object {
        val Saver = Saver<MainBottomSheetSelection?, String>(
            save = { selection ->
                selection?.let {
                    // Instance state is limited to about 1 MB per transaction, so only the newest
                    // points of a recorded track are saved (see MainMapTrackState.Saver).
                    val saved = if (it is CreateNote) it.copy(trackpoints = it.trackpoints?.takeLast(MAX_SAVED_TRACKPOINTS)) else it
                    Json.encodeToString<MainBottomSheetSelection>(saved)
                }
            },
            restore = { Json.decodeFromString<MainBottomSheetSelection>(it) },
        )
    }
}

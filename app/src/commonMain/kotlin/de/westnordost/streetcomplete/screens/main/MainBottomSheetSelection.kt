package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.saveable.Saver
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.quest.QuestKey
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

    companion object {
        val Saver = Saver<MainBottomSheetSelection?, String>(
            save = { selection ->
                selection?.let {
                    // Bound instance state only; the live note still contains the full recording.
                    val saved = if (it is CreateNote) it.copy(trackpoints = it.trackpoints?.takeLast(1000)) else it
                    Json.encodeToString<MainBottomSheetSelection>(saved)
                }
            },
            restore = { Json.decodeFromString<MainBottomSheetSelection>(it) },
        )
    }
}

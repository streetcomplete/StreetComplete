package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.saveable.Saver
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.quest.QuestKey
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.path
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
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
        private const val DRAFT_NOTE_TRACK_CACHE_FILE = "draft_note_track_cache.json"

        private fun getDraftNoteTrackCachePath(): Path =
            Path(PlatformFile(FileKit.cacheDir, DRAFT_NOTE_TRACK_CACHE_FILE).path)

        private fun saveDraftNoteTrackToCache(trackpoints: List<Trackpoint>) {
            try {
                val path = getDraftNoteTrackCachePath()
                SystemFileSystem.sink(path).buffered().use {
                    it.writeString(Json.encodeToString(trackpoints))
                }
            } catch (ignored: Exception) {
            }
        }

        private fun loadDraftNoteTrackFromCache(): List<Trackpoint>? {
            return try {
                val path = getDraftNoteTrackCachePath()
                if (SystemFileSystem.exists(path)) {
                    val text = SystemFileSystem.source(path).buffered().use { it.readString() }
                    Json.decodeFromString<List<Trackpoint>>(text)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        internal fun deleteDraftNoteTrackCache() {
            try {
                val path = getDraftNoteTrackCachePath()
                SystemFileSystem.delete(path, mustExist = false)
            } catch (ignored: Exception) {
            }
        }

        val Saver = Saver<MainSheetSelection?, String>(
            save = { selection ->
                selection?.let {
                    val saved = if (it is CreateNote) {
                        if (!it.trackpoints.isNullOrEmpty()) {
                            saveDraftNoteTrackToCache(it.trackpoints)
                            it.copy(trackpoints = null)
                        } else {
                            deleteDraftNoteTrackCache()
                            it
                        }
                    } else {
                        deleteDraftNoteTrackCache()
                        it
                    }
                    Json.encodeToString<MainSheetSelection>(saved)
                }
            },
            restore = { value ->
                val selection = Json.decodeFromString<MainSheetSelection>(value)
                if (selection is CreateNote) {
                    val cachedTrack = loadDraftNoteTrackFromCache()
                    if (cachedTrack != null) {
                        selection.copy(trackpoints = cachedTrack)
                    } else {
                        selection
                    }
                } else {
                    selection
                }
            },
        )
    }
}

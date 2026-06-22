package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.Mockable
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

interface NoteEditsController : NoteEditsSource {
    fun add(
        noteId: Long,
        action: NoteEditAction,
        position: LatLon,
        text: String? = null,
        imagePaths: List<String> = emptyList(),
        track: List<Trackpoint> = emptyList(),
    )

    fun getOldestNeedingImagesActivation(): NoteEdit?

    fun markImagesActivated(id: Long): Boolean

    fun markSynced(edit: NoteEdit, note: Note)

    fun markSyncFailed(edit: NoteEdit): Boolean

    fun undo(edit: NoteEdit): Boolean

    fun deleteSyncedOlderThan(timestamp: Long): Int

    fun updateElementIds(idUpdates: Collection<ElementIdUpdate>)
}

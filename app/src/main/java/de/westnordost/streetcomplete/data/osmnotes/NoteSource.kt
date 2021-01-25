package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note

interface NoteSource {

    /** Interface to be notified of new notes in the given bbox */
    interface NoteUpdatesListener {
        fun onUpdatedForBBox(bbox: BoundingBox, notes: Collection<Note>)
        fun onAdded(note: Note)
        fun onUpdated(note: Note)
        fun onDeleted(id: Long)
    }

    fun getAllPositions(bbox: BoundingBox): List<LatLon>

    fun addNoteUpdatesListener(listener: NoteUpdatesListener)
    fun removeNoteUpdatesListener(listener: NoteUpdatesListener)

}

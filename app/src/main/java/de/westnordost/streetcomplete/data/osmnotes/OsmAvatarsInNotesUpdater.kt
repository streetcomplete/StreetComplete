package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.notes.Note
import javax.inject.Inject

class OsmAvatarsInNotesUpdater @Inject constructor(private val downloader: OsmAvatarsDownloader) :
    NoteSource.NoteUpdatesListener {

    override fun onUpdatedForBBox(bbox: BoundingBox, notes: Collection<Note>) {
        val noteCommentUserIds = notes.flatMap { it.userIds }.toSet()
        downloader.download(noteCommentUserIds)
    }
    override fun onUpdated(note: Note) {
        downloader.download(note.userIds.toSet())
    }
    override fun onDeleted(noteId: Long) {}
}

private val Note.userIds: List<Long> get() = comments.mapNotNull { it.user?.id }

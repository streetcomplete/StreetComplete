package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.notes.Note
import javax.inject.Inject

class AvatarsInNotesUpdater @Inject constructor(private val downloader: AvatarsDownloader) :
    NoteSource.Listener {

    override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
        val noteCommentUserIds = (added + updated).flatMap { it.userIds }.toSet()
        downloader.download(noteCommentUserIds)
    }
}

private val Note.userIds: List<Long> get() = comments.mapNotNull { it.user?.id }

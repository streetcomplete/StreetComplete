package de.westnordost.streetcomplete.data.osmnotes
import kotlinx.coroutines.runBlocking

class AvatarsInNotesUpdater(private val downloader: AvatarsDownloader) :
    NoteController.Listener {

    override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
        if (added.isEmpty() && updated.isEmpty()) return

        val noteCommentUserIds = (added + updated).flatMap { it.userIds }.toSet()
        runBlocking { downloader.download(noteCommentUserIds) }
    }

    override fun onCleared() {}
}

private val Note.userIds: List<Long> get() = comments.mapNotNull { it.user?.id }

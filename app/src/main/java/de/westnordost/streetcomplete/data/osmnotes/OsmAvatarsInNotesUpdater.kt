package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.UserApi
import javax.inject.Inject

class OsmAvatarsInNotesUpdater @Inject constructor(
    private val userApi: UserApi,
    private val downloader: OsmAvatarsDownloader
) : NoteSource.Listener {

    override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
        val noteCommentUserIds = (added + updated).flatMap { it.userIds }.toSet()
        for (userId in noteCommentUserIds) {
            val avatarUrl = userApi.get(userId)?.profileImageUrl
            if (avatarUrl != null) {
                downloader.download(userId, avatarUrl)
            }
        }
    }
}

private val Note.userIds: List<Long> get() = comments.mapNotNull { it.user?.id }

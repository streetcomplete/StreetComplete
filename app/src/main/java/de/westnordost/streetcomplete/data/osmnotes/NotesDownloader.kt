package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** Takes care of downloading notes into persistent storage */
class NotesDownloader(
    private val notesApi: NotesApiClient,
    private val noteController: NoteController
) {
    suspend fun download(bbox: BoundingBox) {
        val time = nowAsEpochMilliseconds()

        val notes = notesApi
            .getAllOpen(bbox, 10000)
            // exclude invalid notes (#1338)
            .filter { it.comments.isNotEmpty() }

        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Downloaded ${notes.size} notes in ${seconds.format(1)}s")

        yield()

        withContext(Dispatchers.IO) { noteController.putAllForBBox(bbox, notes) }
    }

    companion object {
        private const val TAG = "NotesDownload"
    }
}

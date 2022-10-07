package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.util.ktx.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/** Takes care of downloading notes and referenced avatar pictures into persistent storage */
class NotesDownloader(
    private val notesApi: NotesApi,
    private val noteController: NoteController
) {
    @OptIn(ExperimentalTime::class)
    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        val (notes, execDuration) = measureTimedValue {
            notesApi.getAll(bbox, 10000, 0)
                // exclude invalid notes (#1338)
                .filter { it.comments.isNotEmpty() }
        }
        Log.i(TAG, "Downloaded ${notes.size} notes in ${(execDuration.inWholeMilliseconds / 1000.0).format(1)}s")

        yield()

        noteController.putAllForBBox(bbox, notes)
    }

    companion object {
        private const val TAG = "NotesDownload"
    }
}

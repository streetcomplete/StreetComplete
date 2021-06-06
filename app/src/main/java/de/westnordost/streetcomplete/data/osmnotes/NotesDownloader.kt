package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

import javax.inject.Inject

import de.westnordost.streetcomplete.ktx.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.lang.System.currentTimeMillis

/** Takes care of downloading notes and referenced avatar pictures into persistent storage */
class NotesDownloader @Inject constructor(
    private val notesApi: NotesApi,
    private val noteController: NoteController
) {
    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        val time = currentTimeMillis()

        val notes = notesApi
            .getAll(bbox, 10000, 0)
            // exclude invalid notes (#1338)
            .filter { it.comments.isNotEmpty() }

        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Downloaded ${notes.size} notes in ${seconds.format(1)}s")

        yield()

        noteController.putAllForBBox(bbox, notes)
    }

    companion object {
        private const val TAG = "NotesDownload"
    }
}

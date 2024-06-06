package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NotesApi
import de.westnordost.streetcomplete.data.osmnotes.StreetCompleteImageUploader
import de.westnordost.streetcomplete.data.osmnotes.deleteImages
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.COMMENT
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CREATE
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.osmtracks.TracksApi
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.util.ktx.truncate
import de.westnordost.streetcomplete.util.logs.Log
import io.ktor.http.encodeURLPathPart
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NoteEditsUploader(
    private val noteEditsController: NoteEditsController,
    private val noteController: NoteController,
    private val userDataSource: UserDataSource,
    private val notesApi: NotesApi,
    private val tracksApi: TracksApi,
    private val imageUploader: StreetCompleteImageUploader
) {
    var uploadedChangeListener: OnUploadedChangeListener? = null

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("NoteEditsUploader"))

    /** Uploads all edits to notes
     *
     *  Drops any edits where the upload failed because of a conflict but keeps any notes where
     *  the upload failed because attached photos could not be uploaded (so it can try again
     *  later). */
    suspend fun upload() = mutex.withLock { withContext(Dispatchers.IO) {
        // first look if any images have not been activated yet
        uploadMissedImageActivations()
        // then do the usual stuff
        uploadEdits()
    } }

    private suspend fun uploadMissedImageActivations() {
        while (true) {
            val edit = noteEditsController.getOldestNeedingImagesActivation() ?: break
            // see uploadEdits
            withContext(scope.coroutineContext) {
                imageUploader.activate(edit.noteId)
                noteEditsController.markImagesActivated(edit.id)
            }
        }
    }

    private suspend fun uploadEdits() {
        while (true) {
            val edit = noteEditsController.getOldestUnsynced() ?: break
            /* the sync of local change -> API and its response should not be cancellable because
             * otherwise an inconsistency in the data would occur. E.g. a note could be uploaded
             * twice  */
            withContext(scope.coroutineContext) { uploadEdit(edit) }
        }
    }

    private suspend fun uploadEdit(edit: NoteEdit) {
        // try to upload the image and track if we have them
        val imageText = uploadAndGetAttachedPhotosText(edit.imagePaths)
        val trackText = uploadAndGetAttachedTrackText(edit.track, edit.text)
        val text = edit.text.orEmpty() + imageText + trackText

        // done, try to upload the note to OSM
        try {
            val note = when (edit.action) {
                CREATE -> notesApi.create(edit.position, text)
                COMMENT -> notesApi.comment(edit.noteId, text)
            }

            Log.d(TAG,
                "Uploaded a ${edit.action.name} to ${note.id}" +
                " at ${edit.position.latitude}, ${edit.position.longitude}"
            )
            uploadedChangeListener?.onUploaded(NOTE, edit.position)

            noteEditsController.markSynced(edit, note)
            noteController.put(note)

            if (edit.imagePaths.isNotEmpty()) {
                imageUploader.activate(note.id)
                noteEditsController.markImagesActivated(note.id)
            }
            deleteImages(edit.imagePaths)
        } catch (e: ConflictException) {
            Log.d(TAG,
                "Dropped a ${edit.action.name} to ${edit.noteId}" +
                " at ${edit.position.latitude}, ${edit.position.longitude}: ${e.message}"
            )
            uploadedChangeListener?.onDiscarded(NOTE, edit.position)

            noteEditsController.markSyncFailed(edit)

            // should update the note if there was a conflict, so it doesn't happen again
            val updatedNote = notesApi.get(edit.noteId)
            if (updatedNote != null) {
                noteController.put(updatedNote)
            } else {
                noteController.delete(edit.noteId)
            }

            deleteImages(edit.imagePaths)
        }
    }

    private suspend fun uploadAndGetAttachedPhotosText(imagePaths: List<String>): String {
        if (imagePaths.isNotEmpty()) {
            val urls = imageUploader.upload(imagePaths)
            if (urls.isNotEmpty()) {
                return "\n\nAttached photo(s):\n" + urls.joinToString("\n")
            }
        }
        return ""
    }

    private fun uploadAndGetAttachedTrackText(
        trackpoints: List<Trackpoint>,
        noteText: String?
    ): String {
        if (trackpoints.isEmpty()) return ""
        val trackId = tracksApi.create(trackpoints, noteText?.truncate(255))
        val encodedUsername = userDataSource.userName!!.encodeURLPathPart()
        return "\n\nGPS Trace: https://www.openstreetmap.org/user/$encodedUsername/traces/$trackId\n"
    }

    companion object {
        private const val TAG = "NoteEditsUploader"
        private const val NOTE = "NOTE"
    }
}

package de.westnordost.streetcomplete.data.osmnotes.edits

import android.util.Log
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.NotesApi
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.StreetCompleteImageUploader
import de.westnordost.streetcomplete.data.osmnotes.deleteImages
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.*
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class NoteEditsUploader @Inject constructor(
    private val noteEditsController: NoteEditsController,
    private val noteController: NoteController,
    private val notesApi: NotesApi,
    private val imageUploader: StreetCompleteImageUploader
) {
    var uploadedChangeListener: OnUploadedChangeListener? = null

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("ElementEditsUploader"))

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
        while(true) {
            val edit = noteEditsController.getOldestNeedingImagesActivation() ?: break
            /* see uploadEdits */
            withContext(scope.coroutineContext) {
                imageUploader.activate(edit.noteId)
                noteEditsController.imagesActivated(edit.id)
            }
        }
    }

    private suspend fun uploadEdits() {
        while(true) {
            val edit = noteEditsController.getOldestUnsynced() ?: break
            /* the sync of local change -> API and its response should not be cancellable because
             * otherwise an inconsistency in the data would occur. F.e. a note could be uploaded
             * twice  */
            withContext(scope.coroutineContext) { uploadEdit(edit) }
        }
    }

    private fun uploadEdit(edit: NoteEdit) {
        val text = edit.text.orEmpty() + uploadAndGetAttachedPhotosText(edit.imagePaths)

        try {
            val note = when(edit.action) {
                CREATE -> uploadCreateNote(edit.position, text)
                COMMENT -> uploadCommentNote(edit.noteId, text)
            }

            Log.d(TAG,
                "Uploaded a ${edit.action.name} to ${note.id}" +
                " at ${edit.position.latitude}, ${edit.position.longitude}"
            )
            uploadedChangeListener?.onUploaded(NOTE, edit.position)

            noteEditsController.synced(edit, note)
            noteController.put(note)

            if (edit.imagePaths.isNotEmpty()) {
                imageUploader.activate(note.id)
                noteEditsController.imagesActivated(note.id)
            }
            deleteImages(edit.imagePaths)

        } catch (e: ConflictException) {
            Log.d(TAG,
                "Dropped a ${edit.action.name} to ${edit.noteId}" +
                " at ${edit.position.latitude}, ${edit.position.longitude}: ${e.message}"
            )
            uploadedChangeListener?.onDiscarded(NOTE, edit.position)

            noteEditsController.syncFailed(edit)

            // should update the note if there was a conflict, so it doesn't happen again
            val updatedNote = notesApi.get(edit.noteId)
            if (updatedNote != null) noteController.put(updatedNote)
            else noteController.delete(edit.noteId)

            deleteImages(edit.imagePaths)
        }
    }

    private fun uploadCreateNote(pos: LatLon, text: String): Note = notesApi.create(pos, text)

    private fun uploadCommentNote(noteId: Long, text: String): Note = try {
        notesApi.comment(noteId, text)
    } catch (e: OsmNotFoundException) {
        // someone else already closed the note -> our contribution is probably worthless
        throw ConflictException(e.message, e)
    } catch (e: OsmConflictException) {
        // was closed by admin
        throw ConflictException(e.message, e)
    }

    private fun uploadAndGetAttachedPhotosText(imagePaths: List<String>): String {
        if (imagePaths.isNotEmpty()) {
            val urls = imageUploader.upload(imagePaths)
            if (urls.isNotEmpty()) {
                return "\n\nAttached photo(s):\n" + urls.joinToString("\n")
            }
        }
        return ""
    }

    companion object {
        private const val TAG = "NoteEditsUploader"
        private const val NOTE = "NOTE"
    }
}

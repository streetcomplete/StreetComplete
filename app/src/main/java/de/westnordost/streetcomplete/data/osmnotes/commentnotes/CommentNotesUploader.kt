package de.westnordost.streetcomplete.data.osmnotes.commentnotes

import android.util.Log

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.osmnotes.ImageUploadException
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteWithPhotosUploader
import de.westnordost.streetcomplete.data.osmnotes.deleteImages
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all note quests from local DB and uploads them via the OSM API */
class CommentNotesUploader @Inject constructor(
    private val commentNoteDB: CommentNoteDao,
    private val noteController: NoteController,
    private val singleNoteUploader: OsmNoteWithPhotosUploader
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    /** Uploads all comment notes from local DB and deletes them on successful upload.
     *
     *  Drops any notes where the upload failed because of a conflict but keeps any notes where
     *  the upload failed because attached photos could not be uploaded (so it can try again
     *  later). */
    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        var created = 0
        var obsolete = 0
        if (cancelled.get()) return
        for (commentNote in commentNoteDB.getAll()) {
            if (cancelled.get()) break

            try {
                val newNote = singleNoteUploader.comment(commentNote.noteId, commentNote.text, commentNote.imagePaths)
                noteController.put(newNote)
                commentNoteDB.delete(commentNote.noteId)

                Log.d(TAG, "Uploaded note comment ${commentNote.logString}")
                uploadedChangeListener?.onUploaded(NOTE, commentNote.position)
                created++
                deleteImages(commentNote.imagePaths)
            } catch (e: ConflictException) {
                noteController.delete(commentNote.noteId)
                commentNoteDB.delete(commentNote.noteId)

                Log.d(TAG, "Dropped note comment ${commentNote.logString}: ${e.message}")
                uploadedChangeListener?.onDiscarded(NOTE, commentNote.position)
                obsolete++
                deleteImages(commentNote.imagePaths)
            } catch (e: ImageUploadException) {
                Log.e(TAG, "Error uploading image attached to note comment ${commentNote.logString}", e)
            }
        }
        var logMsg = "Commented on $created notes"
        if (obsolete > 0) {
            logMsg += " but dropped $obsolete comments because the notes have already been closed"
        }
        Log.i(TAG, logMsg)
    }

    companion object {
        private const val TAG = "CommentNoteUpload"
        private const val NOTE = "NOTE"
    }
}

private val CommentNote.logString get() = "\"$text\" at ${position.latitude}, ${position.longitude}"

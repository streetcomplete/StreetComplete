package de.westnordost.streetcomplete.data.osmnotes.notequests

import android.util.Log

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.osmnotes.ImageUploadException
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteWithPhotosUploader
import de.westnordost.streetcomplete.data.osmnotes.deleteImages
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all note quests from local DB and uploads them via the OSM API */
class OsmNoteQuestsChangesUploader @Inject constructor(
        private val osmNoteQuestController: OsmNoteQuestController,
        private val singleNoteUploader: OsmNoteWithPhotosUploader
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    /** Uploads all note quests from local DB and closes them on successful upload.
     *
     *  Drops any notes where the upload failed because of a conflict but keeps any notes where
     *  the upload failed because attached photos could not be uploaded (so it can try again
     *  later). */
    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        var created = 0
        var obsolete = 0
        if (cancelled.get()) return
        for (quest in osmNoteQuestController.getAllAnswered()) {
            if (cancelled.get()) break

            try {
                val newNote = singleNoteUploader.comment(quest.note.id, quest.comment ?: "", quest.imagePaths)
                quest.note.comments = newNote.comments
                quest.note.dateClosed = newNote.dateClosed
                quest.note.status = newNote.status
                osmNoteQuestController.success(quest)

                Log.d(TAG, "Uploaded note comment ${quest.logString}")
                uploadedChangeListener?.onUploaded(NOTE, quest.center)
                created++
                deleteImages(quest.imagePaths)
            } catch (e: ConflictException) {
                osmNoteQuestController.fail(quest)

                Log.d(TAG, "Dropped note comment ${quest.logString}: ${e.message}")
                uploadedChangeListener?.onDiscarded(NOTE, quest.center)
                obsolete++
                deleteImages(quest.imagePaths)
            } catch (e: ImageUploadException) {
                Log.e(TAG, "Error uploading image attached to note comment ${quest.logString}", e)
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

private val OsmNoteQuest.logString get() = "\"$comment\" at ${center.latitude}, ${center.longitude}"

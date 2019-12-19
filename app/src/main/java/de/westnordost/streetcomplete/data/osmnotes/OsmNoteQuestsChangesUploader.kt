package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log

import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all note quests from local DB and uploads them via the OSM API */
class OsmNoteQuestsChangesUploader @Inject constructor(
        private val questDB: OsmNoteQuestDao,
        private val statisticsDB: QuestStatisticsDao,
        private val noteDB: NoteDao,
        private val singleNoteUploader: SingleOsmNoteQuestChangesUploader
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
        for (quest in questDB.getAll(listOf(QuestStatus.ANSWERED))) {
            if (cancelled.get()) break

            try {
                val newNote = singleNoteUploader.upload(quest)

                /* Unlike OSM quests, note quests are never deleted when the user contributed to it
                   but must remain in the database with the status CLOSED as long as they are not
                   solved. The reason is because as long as a note is unsolved, the problem at that
                   position persists and thus it should still block other quests to be created.
                   (Reminder: Note quests block other quests)
                  */
                // so, not this: questDB.delete(quest.getId());
                quest.close()
                questDB.update(quest)
                noteDB.put(newNote)
                statisticsDB.addOneNote()

                Log.d(TAG, "Uploaded note comment ${quest.logString}")
                uploadedChangeListener?.onUploaded()
                created++
                deleteImages(quest.imagePaths)
            } catch (e: ConflictException) {
                questDB.delete(quest.id!!)
                noteDB.delete(quest.note.id)

                Log.d(TAG, "Dropped note comment ${quest.logString}: ${e.message}")
                uploadedChangeListener?.onDiscarded()
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
    }
}

private val OsmNoteQuest.logString get() = "\"$comment\" at ${center.latitude}, ${center.longitude}"

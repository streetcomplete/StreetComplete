package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log

import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all note quests from local DB and uploads them via the OSM API */
class OsmNoteQuestsChangesUpload @Inject constructor(
        private val questDB: OsmNoteQuestDao,
        private val statisticsDB: QuestStatisticsDao,
        private val noteDB: NoteDao,
        private val singleNoteUpload: SingleOsmNoteQuestChangesUpload
) {
    private val TAG = "CommentNoteUpload"

    var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized fun upload(cancelled: AtomicBoolean) {
        var created = 0
        var obsolete = 0
        if (cancelled.get()) return
        for (quest in questDB.getAll(null, QuestStatus.ANSWERED)) {
            if (cancelled.get()) break

            try {
                val newNote = singleNoteUpload.upload(quest)

                /* Unlike OSM quests, note quests are never deleted when the user contributed to it
                   but must remain in the database with the status CLOSED as long as they are not
                   solved. The reason is because as long as a note is unsolved, the problem at that
                   position persists and thus it should still block other quests to be created.
                   (Reminder: Note quests block other quests)
                  */
                // so, not this: questDB.delete(quest.getId());
                quest.status = QuestStatus.CLOSED
                quest.note = newNote
                questDB.update(quest)
                noteDB.put(newNote)
                statisticsDB.addOneNote()

                Log.d(TAG, "Uploaded note comment ${quest.logString}")
                uploadedChangeListener?.onUploaded()
                created++
            } catch (e: ConflictException) {
                questDB.delete(quest.id!!)
                noteDB.delete(quest.note.id)

                Log.d(TAG, "Dropped note comment ${quest.logString}: ${e.message}")
                uploadedChangeListener?.onDiscarded()
                obsolete++
            }
            AttachPhotoUtils.deleteImages(quest.imagePaths)
        }
        var logMsg = "Commented on $created notes"
        if (obsolete > 0) {
            logMsg += " but dropped $obsolete comments because the notes have already been closed"
        }
        Log.i(TAG, logMsg)
    }
}

private val OsmNoteQuest.logString get() = "\"$comment\" at ${center.latitude}, ${center.longitude}"

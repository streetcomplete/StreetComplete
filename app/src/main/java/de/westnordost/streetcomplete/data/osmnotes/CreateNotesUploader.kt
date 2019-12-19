package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log

import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all create notes from local DB and uploads them via the OSM API */
class CreateNotesUploader @Inject constructor(
    private val createNoteDB: CreateNoteDao,
    private val noteDB: NoteDao,
    private val noteQuestDB: OsmNoteQuestDao,
    private val mapDataDao: MapDataDao,
    private val questType: OsmNoteQuestType,
    private val statisticsDB: QuestStatisticsDao,
    private val singleCreateNoteUploader: SingleCreateNoteUploader
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    /** Uploads all create notes from local DB and deletes them on successful upload.
     *
     *  Drops any notes where the upload failed because of a conflict but keeps any notes where
     *  the upload failed because attached photos could not be uploaded (so it can try again
     *  later). */
    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        var created = 0
        var obsolete = 0
        if (cancelled.get()) return
        Log.i(TAG, "Uploading create notes")
        for (createNote in createNoteDB.getAll()) {
            if (cancelled.get()) break

            try {
                val newNote = uploadSingle(createNote)

                // add a closed quest as a blocker so that at this location no quests are created.
                // if the note was not added, don't do this (see below) -> probably based on old data
                val noteQuest = OsmNoteQuest(newNote, questType)
                noteQuest.status = QuestStatus.CLOSED
                noteDB.put(newNote)
                noteQuestDB.add(noteQuest)
                statisticsDB.addOneNote()

                Log.d(TAG, "Uploaded note ${createNote.logString}")
                uploadedChangeListener?.onUploaded()
                created++
                createNoteDB.delete(createNote.id!!)
                deleteImages(createNote.imagePaths)
            } catch (e: ConflictException) {
                Log.d(TAG, "Dropped note ${createNote.logString}: ${e.message}")
                uploadedChangeListener?.onDiscarded()
                obsolete++
                createNoteDB.delete(createNote.id!!)
                deleteImages(createNote.imagePaths)
            } catch (e: ImageUploadException) {
                Log.e(TAG, "Error uploading image attached to note ${createNote.logString}", e)
            }
        }
        var logMsg = "Created $created notes"
        if (obsolete > 0) {
            logMsg += " but dropped $obsolete because they were obsolete already"
        }
        Log.i(TAG, logMsg)
    }

    private fun uploadSingle(n: CreateNote): Note {
        if (n.isAssociatedElementDeleted())
            throw ElementDeletedException("Associated element deleted")
        
        return singleCreateNoteUploader.upload(n)
    }

    private fun CreateNote.isAssociatedElementDeleted(): Boolean {
        return elementKey != null && fetchElement() == null
    }

    private fun CreateNote.fetchElement(): Element? {
        val key = elementKey ?: return null
        return when (key.elementType) {
            Element.Type.NODE -> mapDataDao.getNode(key.elementId)
            Element.Type.WAY -> mapDataDao.getWay(key.elementId)
            Element.Type.RELATION -> mapDataDao.getRelation(key.elementId)
        }
    }

    companion object {
        private const val TAG = "CreateNotesUpload"
    }
}

private val CreateNote.logString get() = "\"$text\" at ${position.latitude}, ${position.longitude}"

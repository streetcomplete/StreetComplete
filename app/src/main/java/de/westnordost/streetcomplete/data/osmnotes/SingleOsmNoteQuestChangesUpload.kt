package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.util.ImageUploader
import javax.inject.Inject

/** Uploads a single note quest to OSM */
class SingleOsmNoteQuestChangesUpload @Inject constructor(
        private val osmDao: NotesDao,
        private val imageUploader: ImageUploader
){

    fun upload(quest: OsmNoteQuest): Note {
        try {
            val attachedPhotosText = AttachPhotoUtils.uploadAndGetAttachedPhotosText(imageUploader, quest.imagePaths)
            val newNote = osmDao.comment(quest.note.id, quest.comment + attachedPhotosText)
            if (!quest.imagePaths.isNullOrEmpty()) {
                imageUploader.activate(newNote.id)
            }
            return newNote
        } catch (e: OsmNotFoundException) {
            // someone else already closed the note -> our contribution is probably worthless
            throw ConflictException(e.message, e)
        } catch (e: OsmConflictException) {
            throw ConflictException(e.message, e)
        }
    }
}

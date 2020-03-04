package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import javax.inject.Inject

/** Uploads a single note quest to OSM */
class SingleOsmNoteQuestChangesUploader @Inject constructor(
        private val osmDao: NotesDao,
        private val imageUploader: StreetCompleteImageUploader
){
    /** Comments on an existing note
     *
     * @throws ImageUploadException if any attached photo could not be uploaded
     * @throws ConflictException if the note has already been closed or deleted
     */
    fun upload(quest: OsmNoteQuest): Note {
        try {
            val attachedPhotosText = uploadAndGetAttachedPhotosText(imageUploader, quest.imagePaths)
            val newNote = osmDao.comment(quest.note.id, quest.comment + attachedPhotosText)
            if (!quest.imagePaths.isNullOrEmpty()) {
                activateImages(newNote.id)
            }
            return newNote
        } catch (e: OsmNotFoundException) {
            // someone else already closed the note -> our contribution is probably worthless
            throw ConflictException(e.message, e)
        } catch (e: OsmConflictException) {
            throw ConflictException(e.message, e)
        }
    }

    private fun activateImages(noteId: Long) {
        try {
            imageUploader.activate(noteId)
        } catch (e: ImageActivationException) {
            Log.e("NoteImageUpload", "Image activation failed", e)
        }
    }
}

package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.osmapi.common.SingleElementHandler
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import java.util.*
import javax.inject.Inject

/** Uploads a single note or note comment */
class SingleCreateNoteUploader @Inject constructor(
    private val osmDao: NotesDao,
    private val imageUploader: StreetCompleteImageUploader
) {
    /** Creates a new note or if a note at this exact position and for this element already exists,
     *  instead adds a comment to the existing note
     *
     * @throws ImageUploadException if any attached photo could not be uploaded
     * @throws ConflictException if a note has already been created for this element but that note
     *                           has is now closed
     */
    fun upload(n: CreateNote): Note {
        if (n.elementKey != null) {
            val oldNote = findAlreadyExistingNoteWithSameAssociatedElement(n)
            if (oldNote != null) {
                return commentNote(oldNote, n.text, n.imagePaths)
            }
        }
        return createNote(n)
    }

    private fun createNote(n: CreateNote): Note {
        val attachedPhotosText = uploadAndGetAttachedPhotosText(imageUploader, n.imagePaths)
        val result = osmDao.create(n.position, n.fullNoteText + attachedPhotosText)
        if (!n.imagePaths.isNullOrEmpty()) {
            activateImages(result.id)
        }
        return result
    }

    private fun commentNote(note: Note, text: String, attachedImagePaths: List<String>?): Note {
        return if (note.isOpen) {
            try {
                val attachedPhotosText = uploadAndGetAttachedPhotosText(imageUploader, attachedImagePaths)
                val result = osmDao.comment(note.id, text + attachedPhotosText)
                if (!attachedImagePaths.isNullOrEmpty()) {
                    activateImages(result.id)
                }
                result
            } catch (e: OsmConflictException) {
                throw ConflictException(e.message, e)
            }
        } else throw ConflictException("Note already closed")
    }

    private fun activateImages(noteId: Long) {
        try {
            imageUploader.activate(noteId)
        } catch (e: ImageActivationException) {
            Log.e("NoteImageUpload", "Image activation failed", e)
        }
    }

    private fun findAlreadyExistingNoteWithSameAssociatedElement(newNote: CreateNote): Note? {
        val handler = object : SingleElementHandler<Note>() {
            override fun handle(oldNote: Note) {
                val newNoteRegex = newNote.associatedElementRegex
                if (newNoteRegex != null) {
                    val firstCommentText = oldNote.comments[0].text
                    if (firstCommentText.matches(newNoteRegex.toRegex())) {
                        super.handle(oldNote)
                    }
                }
            }
        }
        val bbox = BoundingBox(
            newNote.position.latitude, newNote.position.longitude,
            newNote.position.latitude, newNote.position.longitude
        )
        val hideClosedNoteAfter = 7
        osmDao.getAll(bbox, handler, 10, hideClosedNoteAfter)
        return handler.get()
    }
}

private val CreateNote.fullNoteText: String get() {
    return if (elementKey != null) {
        val title = questTitle
        if (title != null) {
            "Unable to answer \"$title\" for $associatedElementString via $USER_AGENT:\n\n$text"
        } else {
            "for $associatedElementString via $USER_AGENT:\n\n$text"
        }
    } else "$text\n\nvia $USER_AGENT"
}

private val CreateNote.associatedElementRegex: String? get() {
    val elementKey = elementKey ?: return null
    val elementTypeName = elementKey.elementType.name
    val elementId = elementKey.elementId
    // before 0.11 - i.e. "way #123"
    val oldStyleRegex = "$elementTypeName\\s*#$elementId"
    // i.e. www.openstreetmap.org/way/123
    val newStyleRegex = "(osm|openstreetmap)\\.org\\/$elementTypeName\\/$elementId"
    // i: turns on case insensitive regex, s: newlines are also captured with "."
    return "(?is).*(($oldStyleRegex)|($newStyleRegex)).*"
}

private val CreateNote.associatedElementString: String? get() {
    val elementKey = elementKey ?: return null
    val lowercaseTypeName = elementKey.elementType.name.toLowerCase(Locale.UK)
    val elementId = elementKey.elementId
    return "https://osm.org/$lowercaseTypeName/$elementId"
}

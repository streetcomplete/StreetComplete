package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.common.SingleElementHandler
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.util.ImageUploader
import java.util.*
import javax.inject.Inject

/** Create a note at the given position, or, if there is already a note at the exact same
 * position AND its associated element is the same, adds the user's message as another comment.
 *
 * Throws an ElementConflictException and does not add the note comment if that note has already
 * been closed because the contribution is very likely obsolete (based on old data) */
class SingleCreateNoteUpload @Inject constructor(
    private val osmDao: NotesDao,
    private val imageUploader: ImageUploader
) {
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
        val attachedPhotosText = AttachPhotoUtils.uploadAndGetAttachedPhotosText(imageUploader, n.imagePaths)
        val result = osmDao.create(n.position, n.fullNoteText + attachedPhotosText)
        if (!n.imagePaths.isNullOrEmpty()) {
            imageUploader.activate(result.id)
        }
        return result
    }

    private fun commentNote(note: Note, text: String, attachedImagePaths: List<String>?): Note {
        return if (note.isOpen) {
            try {
                val attachedPhotosText = AttachPhotoUtils.uploadAndGetAttachedPhotosText(imageUploader, attachedImagePaths)
                val result = osmDao.comment(note.id, text + attachedPhotosText)
                if (!attachedImagePaths.isNullOrEmpty()) {
                    imageUploader.activate(result.id)
                }
                result
            } catch (e: OsmConflictException) {
                throw ConflictException(e.message, e)
            }
        } else throw ConflictException("Note already closed")
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

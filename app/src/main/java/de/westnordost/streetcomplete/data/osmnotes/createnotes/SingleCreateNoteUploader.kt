package de.westnordost.streetcomplete.data.osmnotes.createnotes

import de.westnordost.streetcomplete.data.NotesApi
import de.westnordost.osmapi.common.SingleElementHandler
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteWithPhotosUploader
import java.util.*
import javax.inject.Inject

/** Uploads a single note or note comment, depending if a note at the given position, referencing
 *  the same element already exists or not */
class SingleCreateNoteUploader @Inject constructor(
    private val uploader: OsmNoteWithPhotosUploader,
    private val notesApi: NotesApi
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
                return uploader.comment(oldNote.id, n.text, n.imagePaths)
            }
        }
        return uploader.create(n.position, n.fullNoteText, n.imagePaths)
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
        notesApi.getAll(bbox, handler, 10, hideClosedNoteAfter)
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

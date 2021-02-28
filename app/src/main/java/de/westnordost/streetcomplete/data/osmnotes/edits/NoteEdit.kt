package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.osmapi.map.data.LatLon

/** Contains all necessary information to create/comment on an OSM note. */
data class NoteEdit(
    /** (row) id of the edit. Null if not inserted into DB yet */
    var id: Long?,

    /** note id this edit refers to */
    val noteId: Long,

    /** position of the note */
    val position: LatLon,

    /** timestamp when this edit was made. Used to order the (unsynced) edits in a queue */
    val createdTimestamp: Long,

    /** whether this edit has been uploaded already */
    val isSynced: Boolean,

    /** note comment text */
    val text: String?,

    /** attached photos */
    val imagePaths: List<String>,

    /** Whether the images attached still need activation. Already true if imagePaths is empty */
    val imagesNeedActivation: Boolean,

    /** The action to perform */
    val action: NoteEditAction
)

enum class NoteEditAction {
    CREATE, COMMENT
}

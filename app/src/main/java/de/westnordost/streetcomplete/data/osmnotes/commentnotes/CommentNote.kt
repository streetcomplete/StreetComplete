package de.westnordost.streetcomplete.data.osmnotes.commentnotes

import de.westnordost.osmapi.map.data.LatLon

/** Contains all necessary information to comment on an OSM note. */
data class CommentNote(
    var noteId: Long,
    val position: LatLon,
    val text: String,
    val imagePaths: List<String>? = null
)

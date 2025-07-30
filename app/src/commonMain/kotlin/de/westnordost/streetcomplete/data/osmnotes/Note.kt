package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.User
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val position: LatLon,
    val id: Long,
    val timestampCreated: Long,
    val timestampClosed: Long?,
    val status: Status,
    val comments: List<NoteComment>,
) {
    val isOpen get() = status == Status.OPEN
    val isClosed get() = status == Status.CLOSED
    val isHidden get() = status == Status.HIDDEN

    enum class Status {
        OPEN,
        CLOSED,
        HIDDEN
    }
}

@Serializable
data class NoteComment(
    val timestamp: Long,
    val action: Action,
    val text: String?,
    val user: User?,
) {
    val isAnonymous get() = user == null

    enum class Action {
        OPENED,
        COMMENTED,
        CLOSED,
        REOPENED,
        HIDDEN
    }
}

package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.osmapi.map.data.LatLon

// TODO make sealed as soon as kotlin supports this
/* sealed*/ interface Edit {
    val createdTimestamp: Long
    val isUndoable: Boolean
    val position: LatLon
}

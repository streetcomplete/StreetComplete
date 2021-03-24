package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.osmapi.map.data.LatLon

interface Edit {
    val createdTimestamp: Long
    val isUndoable: Boolean
    val position: LatLon
}

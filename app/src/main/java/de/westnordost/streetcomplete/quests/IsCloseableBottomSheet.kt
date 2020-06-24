package de.westnordost.streetcomplete.quests

import de.westnordost.osmapi.map.data.LatLon

interface IsCloseableBottomSheet {
    /** Returns true if the bottom sheet shall consume the event */
    fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean
    fun onClickClose(onConfirmed: () -> Unit)
}

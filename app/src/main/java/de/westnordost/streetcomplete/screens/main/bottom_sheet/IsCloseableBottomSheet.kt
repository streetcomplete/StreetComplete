package de.westnordost.streetcomplete.screens.main.bottom_sheet

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface IsCloseableBottomSheet {
    /** Returns true if the bottom sheet shall consume the event */
    fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean
    fun onClickClose(onConfirmed: () -> Unit)
}

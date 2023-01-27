package de.westnordost.streetcomplete.screens.main.bottom_sheet

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface IsMapPositionAware {
    fun onMapMoved(position: LatLon)
}

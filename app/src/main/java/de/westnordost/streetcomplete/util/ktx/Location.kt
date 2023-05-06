package de.westnordost.streetcomplete.util.ktx

import android.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

fun Location.toLatLon() = LatLon(latitude, longitude)

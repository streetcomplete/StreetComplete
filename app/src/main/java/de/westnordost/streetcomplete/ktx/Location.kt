package de.westnordost.streetcomplete.ktx

import android.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

fun Location.toLatLon() = LatLon(latitude, longitude)

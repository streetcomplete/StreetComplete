package de.westnordost.streetcomplete.util.ktx

import android.location.Location
import androidx.core.location.LocationCompat
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.time.Duration.Companion.milliseconds

fun Location.toLatLon() = LatLon(latitude, longitude)

val Location.elapsedDuration
    get() = LocationCompat.getElapsedRealtimeMillis(this).milliseconds

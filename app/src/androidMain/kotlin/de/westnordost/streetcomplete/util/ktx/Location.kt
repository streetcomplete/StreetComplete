package de.westnordost.streetcomplete.util.ktx

import android.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

fun Location.toLatLon() = LatLon(latitude, longitude)

val Location.elapsedDuration: Duration get() = elapsedRealtimeNanos.nanoseconds

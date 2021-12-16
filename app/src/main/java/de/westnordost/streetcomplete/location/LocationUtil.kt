package de.westnordost.streetcomplete.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager

import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

val Context.isLocationEnabled: Boolean
    get() = hasLocationPermission && LocationManagerCompat.isLocationEnabled(getSystemService()!!)

val Context.hasLocationPermission: Boolean
    get() = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

fun createLocationAvailabilityIntentFilter() = IntentFilter(LocationManager.MODE_CHANGED_ACTION)

fun Location.toLatLon() = LatLon(latitude, longitude)

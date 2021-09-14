package de.westnordost.streetcomplete.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager

import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

fun isLocationEnabled(context: Context): Boolean =
    hasLocationPermission(context) && isLocationEnabled(getLocationManager(context)!!)

private fun getLocationManager(context: Context): LocationManager? =
    ContextCompat.getSystemService(context, LocationManager::class.java)

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

fun createLocationAvailabilityIntentFilter(): IntentFilter =
    IntentFilter(LocationManager.MODE_CHANGED_ACTION)

fun Location.toLatLon(): LatLon = LatLon(latitude, longitude)

package de.westnordost.streetcomplete.location

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager

import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

object LocationUtil {
    fun isLocationEnabled(context: Context): Boolean {
        return hasLocationPermission(context) && LocationManagerCompat
                .isLocationEnabled(ContextCompat.getSystemService(context, LocationManager::class.java)!!)
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun createLocationAvailabilityIntentFilter(): IntentFilter {
        return IntentFilter(LocationManager.MODE_CHANGED_ACTION)
    }
}

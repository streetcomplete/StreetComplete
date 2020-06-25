package de.westnordost.streetcomplete.location

import android.location.LocationListener
import android.os.Bundle

interface LocationUpdateListener : LocationListener {

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String?) {}

    override fun onProviderDisabled(provider: String?) {}
}

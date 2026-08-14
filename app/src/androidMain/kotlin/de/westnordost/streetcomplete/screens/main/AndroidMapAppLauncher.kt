package de.westnordost.streetcomplete.screens.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.buildGeoUri

class AndroidMapAppLauncher(private val context: Context) : MapAppLauncher {
    override fun openAt(position: LatLon, zoom: Double) {
        val uri = buildGeoUri(
            latitude = position.latitude,
            longitude = position.longitude,
            zoom = zoom
        )
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val anyOtherMapAppInstalled = context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            // because we ourselves also can open geo uris
            .any { it.activityInfo.packageName != context.packageName }

        if (anyOtherMapAppInstalled) {
            context.startActivity(intent)
        }
    }

    override fun isAvailable(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, "geo:0.0,0.0".toUri())
        val anyOtherMapAppInstalled = context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            // because we ourselves also can open geo uris
            .any { it.activityInfo.packageName != context.packageName }
        return anyOtherMapAppInstalled
    }
}

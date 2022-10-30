package de.westnordost.streetcomplete.screens.main

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.quests.getQuestTitle
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.util.buildGeoUri
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// will be created every time the app is put to background... actually that's not good
class NearbyQuestMonitor : Service(), LocationListener, KoinComponent {

    private val locationManager: LocationManager by lazy { applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager }

    private fun getQuestFoundNotification(size: Int, closest: Quest): Notification =
        NotificationCompat.Builder(this, FOUND_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle(getString(R.string.quest_monitor_found, size))
            .setContentText(resources.getQuestTitle(closest.type, emptyMap()))
            .setContentIntent(intent(closest.position))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

    private fun intent(position: LatLon): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = buildGeoUri(position.latitude, position.longitude)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(this, 1, intent, 0)
        }
    }

    private val visibleQuestsSource: VisibleQuestsSource by inject()

    override fun onBind(intent: Intent): IBinder {
        running = true
        // create notification channels and service notification
        val manager = NotificationManagerCompat.from(this)
        if (manager.getNotificationChannelCompat(MONITOR_CHANNEL_ID) == null)
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(MONITOR_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                    .setName(getString(R.string.quest_monitor_channel_name))
                    .build()
            )
        if (manager.getNotificationChannelCompat(FOUND_CHANNEL_ID) == null)
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(FOUND_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName(getString(R.string.quest_monitor_channel_name_found))
                    .setVibrationEnabled(true)
                    .build()
            )
        try {
            val int = Intent(this, MainActivity::class.java)
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(this, 0, int, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(this, 0, int, 0)
            }
            startForeground(MONITOR_NOTIFICATION_ID, NotificationCompat.Builder(this, MONITOR_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(ApplicationConstants.NAME)
                .setContentText(getString(R.string.quest_monitor_passive))
                .setContentIntent(pi)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
            )
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0L, 20.0f, this)
        } catch (e: SecurityException) {
            // there is some foreground issue, and of course location permissions
            this.toast(R.string.quest_monitor_error, Toast.LENGTH_LONG)
        }
        return Binder()
    }

    override fun onLocationChanged(location: Location) {
        // check if we have nearby quests
        if (location.accuracy > 100) return
        val loc = location.toLatLon()
        val q = visibleQuestsSource.getAllVisible(loc.enclosingBoundingBox(50.0)).filter { it.type.dotColor == "no" }
        if (q.isEmpty()) return // todo (later): optionally download
        val closest = q.minBy { loc.distanceTo(it.position) } // lat/lon distance would be sufficient for minimum, but current way is probably fast enough
        val n = getQuestFoundNotification(q.size, closest)
        NotificationManagerCompat.from(this).notify(FOUND_NOTIFICATION_ID, n)
    }

    // not overriding those causes crashes on Android 10 (only?)
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
        NotificationManagerCompat.from(this).cancel(FOUND_NOTIFICATION_ID)
        stopForeground(true)
        running = false
    }
    companion object {
        var running = false
    }
}

private const val MONITOR_NOTIFICATION_ID = 759743090
private const val FOUND_NOTIFICATION_ID = 16540685
private const val MONITOR_CHANNEL_ID = "quest_monitor"
private const val FOUND_CHANNEL_ID = "quest_found"

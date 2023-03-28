package de.westnordost.streetcomplete.screens.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesType
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.quests.getQuestTitle
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.util.buildGeoUri
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import kotlin.math.max

// will be created every time the app is put to background... actually that's not good
class NearbyQuestMonitor : Service(), LocationListener, KoinComponent {

    private val locationManager: LocationManager by lazy { applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager }
    private val prefs: SharedPreferences by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val downloadController: DownloadController by inject()
    private val downloadedTilesDb: DownloadedTilesDao by inject()
    private var lastScanCenter = LatLon(0.0, 0.0)
    private val searchRadius = prefs.getFloat(Prefs.QUEST_MONITOR_RADIUS, 50f).toDouble()
    private val download = prefs.getBoolean(Prefs.QUEST_MONITOR_DOWNLOAD, false)
    private val dataRetainTime = prefs.getInt(Prefs.DATA_RETAIN_TIME, ApplicationConstants.DELETE_OLD_DATA_AFTER_DAYS) * 24L * 60 * 60 * 1000

    private fun getQuestFoundNotification(size: Int, closest: Quest): Notification =
        NotificationCompat.Builder(this, FOUND_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle(getString(R.string.quest_monitor_found, size))
            .setContentText(resources.getQuestTitle(closest.type, emptyMap()))
            .setContentIntent(intent(closest.position))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

    @SuppressLint("UnspecifiedImmutableFlag") // dear android studio: demanding to set mutability for API levels where it's not possible is a rather shitty idea
    private fun intent(position: LatLon): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = buildGeoUri(position.latitude, position.longitude)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
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
            val notification = NotificationCompat.Builder(this, MONITOR_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(ApplicationConstants.NAME)
                .setContentText(getString(R.string.quest_monitor_running))
                .setContentIntent(pi)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(MONITOR_NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
            else
                startForeground(MONITOR_NOTIFICATION_ID, notification)
            if (prefs.getBoolean(Prefs.QUEST_MONITOR_GPS, false))
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefs.getInt(Prefs.GPS_INTERVAL, 0) * 1000L, 0.0f, this)
            if (prefs.getBoolean(Prefs.QUEST_MONITOR_NET, false))
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, prefs.getInt(Prefs.NETWORK_INTERVAL, 5) * 1000L, 0.0f, this)
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0L, 0.0f, this)
        } catch (e: SecurityException) {
            // there is some foreground issue, and of course location permissions
            this.toast(R.string.quest_monitor_error, Toast.LENGTH_LONG)
        }
        return Binder()
    }

    override fun onLocationChanged(location: Location) {
        // check whether we have nearby quests
        if (location.accuracy > searchRadius) return
        val loc = location.toLatLon()
        if (loc.distanceTo(lastScanCenter) < searchRadius * 0.7) return // don't scan if we're still close to previous location
        lastScanCenter = loc
        val quests = visibleQuestsSource.getAllVisible(loc.enclosingBoundingBox(searchRadius)).filter { it.type.dotColor == null }
        if (quests.isEmpty()) {
            NotificationManagerCompat.from(this).cancel(FOUND_NOTIFICATION_ID) // no quest, no notification
            if (download) {
                // check whether surrounding area should be downloaded
                if (downloadController.isDownloadInProgress) return // download already running
                val activeNetworkInfo = getSystemService<ConnectivityManager>()?.activeNetworkInfo ?: return
                if (!activeNetworkInfo.isConnected) return // we are not connected
                val ignoreOlderThan = nowAsEpochMilliseconds() - dataRetainTime
                val tile = loc.enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM).toTilesRect()
                if (downloadedTilesDb.get(tile, ignoreOlderThan).contains(DownloadedTilesType.ALL)) return // we already have the area
                downloadController.download(loc.enclosingBoundingBox(max(150.0, searchRadius))) // download quests in at least 150 m radius (will likely be single z16 tile)
            }
            return
        }
        val closest = quests.minBy {
//            loc.distanceTo(it.position) // no need to do the relatively heavy exact distance calculation
            val lonDiff = loc.latitude - it.position.latitude
            val latDiff = loc.longitude - it.position.longitude
            latDiff * latDiff + lonDiff * lonDiff
        }
        val notification = getQuestFoundNotification(quests.size, closest)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            this.toast("Quests found, but no notification permission") // should not happen, not worth a string resource
        else
            NotificationManagerCompat.from(this).notify(FOUND_NOTIFICATION_ID, notification)
    }

    // not overriding those causes crashes on Android 10 (only?)
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    @Deprecated("Deprecated in Java") // so it crashes without this, but complains about deprecation if it's there? WTF?
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
        NotificationManagerCompat.from(this).cancel(FOUND_NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            stopForeground(STOP_FOREGROUND_REMOVE)
        else
            stopForeground(true) // why the fuck can't it detect that the non-deprecated alternative is not available below N?
        running = false
    }
    companion object {
        var running = false
            private set
    }
}

private const val MONITOR_NOTIFICATION_ID = 759743090
private const val FOUND_NOTIFICATION_ID = 16540685
private const val MONITOR_CHANNEL_ID = "quest_monitor"
private const val FOUND_CHANNEL_ID = "quest_found"

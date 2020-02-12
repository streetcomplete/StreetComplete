package de.westnordost.streetcomplete.data

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

import javax.inject.Inject

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.download.MobileDataAutoDownloadStrategy
import de.westnordost.streetcomplete.data.download.WifiAutoDownloadStrategy
import de.westnordost.streetcomplete.location.FineLocationManager
import de.westnordost.streetcomplete.location.LocationUpdateListener
import de.westnordost.streetcomplete.oauth.OAuthPrefs
import kotlinx.coroutines.*

/** Automatically downloads and uploads new quests around the user's location and uploads quests.
 *
 * Respects the user preference to only sync on wifi or not sync automatically at all
 */
class QuestAutoSyncer @Inject constructor(
    private val questController: QuestController,
    private val mobileDataDownloadStrategy: MobileDataAutoDownloadStrategy,
    private val wifiDownloadStrategy: WifiAutoDownloadStrategy,
    private val context: Context,
    private val prefs: SharedPreferences,
    private val oAuth: OAuthPrefs
) : LocationUpdateListener, VisibleQuestListener, LifecycleObserver, CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private var pos: LatLon? = null

    private var isConnected: Boolean = false
    private var isWifi: Boolean = false

    private val locationManager = FineLocationManager(
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager,
        this::onLocationChanged
    )

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectionStateChanged = updateConnectionState()
            // connecting to i.e. mobile data after being disconnected from wifi -> not interested in that
            val isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false)
            if (!isFailover && connectionStateChanged && isConnected) {
                triggerAutoDownload()
                triggerAutoUpload()
            }
        }
    }

    val isAllowedByPreference: Boolean
        get() {
            val p = Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!)
            return p == Prefs.Autosync.ON || p == Prefs.Autosync.WIFI && isWifi
        }

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME) fun onResume() {
        updateConnectionState()
        context.registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        questController.addListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE) fun onPause() {
        stopPositionTracking()
        context.unregisterReceiver(connectivityReceiver)
        questController.removeListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY) fun onDestroy() {
        coroutineContext.cancel()
    }

    @SuppressLint("MissingPermission")
    fun startPositionTracking() {
        locationManager.requestUpdates(3 * 60 * 1000L, 500f)
    }

    fun stopPositionTracking() {
        locationManager.removeUpdates()
    }

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    override fun onQuestsCreated(quests: Collection<Quest>, group: QuestGroup) { }

    override fun onQuestsRemoved(questIds: Collection<Long>, group: QuestGroup) {
        // amount of quests is reduced -> check if redownloding now makes sense
        triggerAutoDownload()
    }

    /* --------------------------------- LocationUpdateListener --------------------------------- */

    override fun onLocationChanged(location: Location?) {
        if(location == null) return
        pos = OsmLatLon(location.latitude, location.longitude)
        triggerAutoDownload()
    }

    /* ------------------------------------------------------------------------------------------ */

    fun triggerAutoDownload() {
        if (!isAllowedByPreference) return
        val pos = pos ?: return
        if (!isConnected) return
        if (questController.isDownloadInProgress) return

        Log.i(TAG, "Checking whether to automatically download new quests at ${pos.latitude},${pos.longitude}")

        launch {
            val downloadStrategy = if (isWifi) wifiDownloadStrategy else mobileDataDownloadStrategy
            if (downloadStrategy.mayDownloadHere(pos)) {
                try {
                    questController.download(
                        downloadStrategy.getDownloadBoundingBox(pos),
                        downloadStrategy.questTypeDownloadCount
                    )
                } catch (e: IllegalStateException) {
                    // The Android 9 bug described here should not result in a hard crash of the app
                    // https://stackoverflow.com/questions/52013545/android-9-0-not-allowed-to-start-service-app-is-in-background-after-onresume
                    Log.e(TAG, "Cannot start download service", e)
                }
            }
        }
    }

    fun triggerAutoUpload() {
        if (!isAllowedByPreference) return
        if (!isConnected) return
        if (!oAuth.isAuthorized) return

        try {
            questController.upload()
        } catch (e: IllegalStateException) {
            // The Android 9 bug described here should not result in a hard crash of the app
            // https://stackoverflow.com/questions/52013545/android-9-0-not-allowed-to-start-service-app-is-in-background-after-onresume
            Log.e(TAG, "Cannot start upload service", e)
        }
    }

    private fun updateConnectionState(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo ?: return false

        val newIsConnected = info.isConnected
        // metered (usually ad-hoc hotspots) do not count as proper wifis
        val isMetered = connectivityManager.isActiveNetworkMetered
        val newIsWifi = newIsConnected && info.type == ConnectivityManager.TYPE_WIFI && !isMetered

        val result = newIsConnected != isConnected || newIsWifi != isWifi

        isConnected = newIsConnected
        isWifi = newIsWifi
        return result
    }

    companion object {
        private const val TAG = "QuestAutoSyncer"
    }

}

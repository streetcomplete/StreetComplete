package de.westnordost.streetcomplete.data.quest

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadProgressListener
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.download.strategy.MobileDataAutoDownloadStrategy
import de.westnordost.streetcomplete.data.download.strategy.WifiAutoDownloadStrategy
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/** Automatically downloads new quests around the user's location and uploads quests.
 *
 * Respects the user preference to only sync on wifi or not sync automatically at all
 */
class QuestAutoSyncer(
    private val downloadController: DownloadController,
    private val uploadController: UploadController,
    private val mobileDataDownloadStrategy: MobileDataAutoDownloadStrategy,
    private val wifiDownloadStrategy: WifiAutoDownloadStrategy,
    private val context: Context,
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val downloadProgressSource: DownloadProgressSource,
    private val userLoginStatusSource: UserLoginStatusSource,
    private val prefs: SharedPreferences,
    private val teamModeQuestFilter: TeamModeQuestFilter,
    private val downloadedTilesController: DownloadedTilesController
) : DefaultLifecycleObserver {

    private val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("QuestAutoSyncer"))

    private var pos: LatLon? = null

    private var isConnected: Boolean = false
    private var isWifi: Boolean = false

    // new location is known -> check if downloading makes sense now
    private val locationManager = FineLocationManager(context) { location ->
        if (location.accuracy <= 300) {
            pos = location.toLatLon()
            triggerAutoDownload()
        }
    }

    // connection state changed -> check if downloading or uploading is allowed now
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

    // there are unsynced changes -> try uploading now
    private val unsyncedChangesListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { triggerAutoUpload() }
        override fun onDecreased() {}
    }

    // on download finished, should recheck conditions for download
    private val downloadProgressListener = object : DownloadProgressListener {
        override fun onSuccess() {
            triggerAutoDownload()
        }
    }

    private val userLoginStatusListener = object : UserLoginStatusSource.Listener {
        override fun onLoggedIn() {
            triggerAutoUpload()
        }

        override fun onLoggedOut() {}
    }

    private val teamModeChangeListener = object : TeamModeQuestFilter.TeamModeChangeListener {
        override fun onTeamModeChanged(enabled: Boolean) {
            if (!enabled) {
                // because other team members will have solved some of the quests already
                downloadedTilesController.invalidateAll()
                triggerAutoDownload()
            }
        }
    }

    val isAllowedByPreference: Boolean
        get() {
            val p = Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!)
            return p == Prefs.Autosync.ON || p == Prefs.Autosync.WIFI && isWifi
        }

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onCreate(owner: LifecycleOwner) {
        unsyncedChangesCountSource.addListener(unsyncedChangesListener)
        downloadProgressSource.addDownloadProgressListener(downloadProgressListener)
        userLoginStatusSource.addListener(userLoginStatusListener)
        teamModeQuestFilter.addListener(teamModeChangeListener)
    }

    override fun onResume(owner: LifecycleOwner) {
        updateConnectionState()
        if (isConnected) {
            triggerAutoDownload()
            triggerAutoUpload()
        }
        context.registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause(owner: LifecycleOwner) {
        stopPositionTracking()
        context.unregisterReceiver(connectivityReceiver)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        unsyncedChangesCountSource.removeListener(unsyncedChangesListener)
        downloadProgressSource.removeDownloadProgressListener(downloadProgressListener)
        userLoginStatusSource.removeListener(userLoginStatusListener)
        teamModeQuestFilter.removeListener(teamModeChangeListener)
        coroutineScope.coroutineContext.cancelChildren()
    }

    @SuppressLint("MissingPermission")
    fun startPositionTracking() {
        locationManager.requestUpdates(30 * 1000L, 30 * 1000L, 250f)
    }

    fun stopPositionTracking() {
        locationManager.removeUpdates()
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun triggerAutoDownload() {
        val pos = pos ?: return
        if (!isConnected) return
        if (downloadController.isDownloadInProgress) return

        Log.i(TAG, "Checking whether to automatically download new quests at ${pos.latitude.format(7)},${pos.longitude.format(7)}")

        coroutineScope.launch {
            val downloadStrategy = if (isWifi) wifiDownloadStrategy else mobileDataDownloadStrategy
            val downloadBoundingBox = downloadStrategy.getDownloadBoundingBox(pos)
            if (downloadBoundingBox != null) {
                try {
                    downloadController.download(downloadBoundingBox)
                } catch (e: IllegalStateException) {
                    // The Android 9 bug described here should not result in a hard crash of the app
                    // https://stackoverflow.com/questions/52013545/android-9-0-not-allowed-to-start-service-app-is-in-background-after-onresume
                    Log.e(TAG, "Cannot start download service", e)
                }
            }
        }
    }

    private fun triggerAutoUpload() {
        if (!isAllowedByPreference) return
        if (!isConnected) return
        if (!userLoginStatusSource.isLoggedIn) return

        coroutineScope.launch {
            try {
                uploadController.upload()
            } catch (e: IllegalStateException) {
                // The Android 9 bug described here should not result in a hard crash of the app
                // https://stackoverflow.com/questions/52013545/android-9-0-not-allowed-to-start-service-app-is-in-background-after-onresume
                Log.e(TAG, "Cannot start upload service", e)
            }
        }
    }

    private fun updateConnectionState(): Boolean {
        val connectivityManager = context.getSystemService<ConnectivityManager>()!!
        val info = connectivityManager.activeNetworkInfo

        val newIsConnected = info?.isConnected ?: false
        // metered (usually ad-hoc hotspots) do not count as proper wifis
        val isMetered = connectivityManager.isActiveNetworkMetered
        val newIsWifi = newIsConnected && info?.type == ConnectivityManager.TYPE_WIFI && !isMetered

        val result = newIsConnected != isConnected || newIsWifi != isWifi

        isConnected = newIsConnected
        isWifi = newIsWifi
        return result
    }

    companion object {
        private const val TAG = "QuestAutoSyncer"
    }
}

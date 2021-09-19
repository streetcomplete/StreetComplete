package de.westnordost.streetcomplete

import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.Injector.applicationComponent
import de.westnordost.streetcomplete.controls.NotificationButtonFragment
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadProgressListener
import de.westnordost.streetcomplete.data.notifications.Notification
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.ConnectionException
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.ImageUploadServerException
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.VersionBannedException
import de.westnordost.streetcomplete.data.user.AuthorizationException
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.location.createLocationAvailabilityIntentFilter
import de.westnordost.streetcomplete.location.isLocationEnabled
import de.westnordost.streetcomplete.location.LocationRequestFragment
import de.westnordost.streetcomplete.location.LocationState
import de.westnordost.streetcomplete.map.MainFragment
import de.westnordost.streetcomplete.notifications.NotificationsContainerFragment
import de.westnordost.streetcomplete.tutorial.TutorialFragment
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.parseGeoUri
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
    MainFragment.Listener, TutorialFragment.Listener, NotificationButtonFragment.Listener {

	@Inject lateinit var crashReportExceptionHandler: CrashReportExceptionHandler
	@Inject lateinit var locationRequestFragment: LocationRequestFragment
	@Inject lateinit var questAutoSyncer: QuestAutoSyncer
	@Inject lateinit var downloadController: DownloadController
	@Inject lateinit var uploadController: UploadController
	@Inject lateinit var unsyncedChangesCountSource: UnsyncedChangesCountSource
	@Inject lateinit var prefs: SharedPreferences
	@Inject lateinit var userController: UserController

    private var mainFragment: MainFragment? = null

    private val locationAvailabilityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateLocationAvailability()
        }
    }
    private val locationRequestFinishedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = LocationState.valueOf(intent.getStringExtra(LocationRequestFragment.STATE)!!)
            onLocationRequestFinished(state)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationComponent.inject(this)

        lifecycle.addObserver(questAutoSyncer)
        crashReportExceptionHandler.askUserToSendCrashReportIfExists(this)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        if (prefs.getBoolean(Prefs.KEEP_SCREEN_ON, false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        supportFragmentManager.commit {
            add(locationRequestFragment, LocationRequestFragment::class.java.simpleName)
        }

        setContentView(R.layout.activity_main)

        mainFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MainFragment?
        if (savedInstanceState == null) {
            val hasShownTutorial = prefs.getBoolean(Prefs.HAS_SHOWN_TUTORIAL, false)
            if (!hasShownTutorial && !userController.isLoggedIn) {
                supportFragmentManager.commit {
                    setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
                    add(R.id.fragment_container, TutorialFragment())
                }
            }
            if (userController.isLoggedIn && isConnected) {
                userController.updateUser()
            }
        }
        handleGeoUri()
    }

    private fun handleGeoUri() {
        if (Intent.ACTION_VIEW != intent.action) return
        val data = intent.data ?: return
        if ("geo" != data.scheme) return
        val geo = parseGeoUri(data) ?: return
        val zoom = if (geo.zoom == null || geo.zoom < 14)  18f else geo.zoom
        val pos = LatLon(geo.latitude, geo.longitude)
        mainFragment?.setCameraPosition(pos, zoom)
    }

    public override fun onStart() {
        super.onStart()

        registerReceiver(locationAvailabilityReceiver, createLocationAvailabilityIntentFilter())
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationRequestFinishedReceiver, IntentFilter(LocationRequestFragment.ACTION_FINISHED))

        downloadController.showNotification = false
        uploadController.showNotification = false
        uploadController.addUploadProgressListener(uploadProgressListener)
        downloadController.addDownloadProgressListener(downloadProgressListener)
        updateLocationAvailability()
    }

    override fun onBackPressed() {
        if (!forwardBackPressedToChildren()) super.onBackPressed()
    }

    private fun forwardBackPressedToChildren(): Boolean {
        val notificationsContainerFragment = notificationsContainerFragment
        if (notificationsContainerFragment != null) {
            if (notificationsContainerFragment.onBackPressed()) return true
        }
        val mainFragment = mainFragment
        if (mainFragment != null) {
            if (mainFragment.onBackPressed()) return true
        }
        return false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val mainFragment = mainFragment
        if (event.keyCode == KeyEvent.KEYCODE_MENU && mainFragment != null) {
            if (event.action == KeyEvent.ACTION_UP) {
                mainFragment.onClickMainMenu()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    public override fun onPause() {
        super.onPause()
        val pos = mainFragment?.getCameraPosition()?.position ?: return
        prefs.edit {
            putLong(Prefs.MAP_LATITUDE, java.lang.Double.doubleToRawLongBits(pos.latitude))
            putLong(Prefs.MAP_LONGITUDE, java.lang.Double.doubleToRawLongBits(pos.longitude))
        }
    }

    public override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationRequestFinishedReceiver)
        unregisterReceiver(locationAvailabilityReceiver)
        downloadController.showNotification = true
        uploadController.showNotification = true
        uploadController.removeUploadProgressListener(uploadProgressListener)
        downloadController.removeDownloadProgressListener(downloadProgressListener)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        findViewById<View>(R.id.main).requestLayout()
        // recreate the NotificationsContainerFragment because it should load a new layout, see #2330
        supportFragmentManager.commit {
            replace(R.id.notifications_container_fragment, NotificationsContainerFragment())
        }
    }

    private suspend fun ensureLoggedIn() {
        if (!questAutoSyncer.isAllowedByPreference) return
        if (userController.isLoggedIn) return

        // new users should not be immediately pestered to login after each change (#1446)
        if (unsyncedChangesCountSource.getCount() < 3 || dontShowRequestAuthorizationAgain) return

        RequestLoginDialog(this).show()
        dontShowRequestAuthorizationAgain = true
    }

    private val isConnected: Boolean
        get() = getSystemService<ConnectivityManager>()?.activeNetworkInfo?.isConnected == true

    /* ------------------------------ Upload progress listener ---------------------------------- */

    private val uploadProgressListener: UploadProgressListener = object : UploadProgressListener {
        @AnyThread override fun onError(e: Exception) {
            runOnUiThread {
                if (e is VersionBannedException) {
                    var message = getString(R.string.version_banned_message)
                    if (e.banReason != null) {
                        message += "\n\n\n${e.banReason}"
                    }
                    val dialog = AlertDialog.Builder(this@MainActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                    dialog.show()

                    // Makes links in the alert dialog clickable
                    val messageView = dialog.findViewById<View>(android.R.id.message)
                    if (messageView is TextView) {
                        messageView.movementMethod = LinkMovementMethod.getInstance()
                        Linkify.addLinks(messageView, Linkify.WEB_URLS)
                    }
                } else if (e is ConnectionException || e is ImageUploadServerException) {
                    /* A network connection error or server error is not the fault of this app.
                       Nothing we can do about it, so it does not make sense to send an error
                       report. Just notify the user. */
                    toast(R.string.upload_server_error, Toast.LENGTH_LONG)
                } else if (e is AuthorizationException) {
                    // delete secret in case it failed while already having a token -> token is invalid
                    userController.logOut()
                    RequestLoginDialog(this@MainActivity).show()
                } else {
                    crashReportExceptionHandler.askUserToSendErrorReport(this@MainActivity, R.string.upload_error, e)
                }
            }
        }
    }

    /* ----------------------------- Download Progress listener  -------------------------------- */

    private val downloadProgressListener: DownloadProgressListener = object : DownloadProgressListener {
        @AnyThread override fun onError(e: Exception) {
            runOnUiThread {
                // A network connection error is not the fault of this app. Nothing we can do about
                // it, so it does not make sense to send an error report. Just notify the user.
                if (e is ConnectionException) {
                    toast(R.string.download_server_error, Toast.LENGTH_LONG)
                }  else if (e is AuthorizationException) {
                    // delete secret in case it failed while already having a token -> token is invalid
                    userController.logOut()
                } else {
                    crashReportExceptionHandler.askUserToSendErrorReport(this@MainActivity, R.string.download_error, e)
                }
            }
        }
    }

    /* --------------------------------- NotificationButtonFragment.Listener ---------------------------------- */

    override fun onClickShowNotification(notification: Notification) {
        notificationsContainerFragment?.showNotification(notification)
    }

    private val notificationsContainerFragment get() =
        supportFragmentManager.findFragmentById(R.id.notifications_container_fragment) as? NotificationsContainerFragment

    /* --------------------------------- MainFragment.Listener ---------------------------------- */

    override fun onQuestSolved(quest: Quest, source: String?) {
        lifecycleScope.launch { ensureLoggedIn() }
    }

    override fun onCreatedNote(screenPosition: Point) {
        lifecycleScope.launch { ensureLoggedIn() }
    }

    /* ------------------------------- TutorialFragment.Listener -------------------------------- */

    override fun onTutorialFinished() {
        locationRequestFragment.startRequest()

        prefs.edit().putBoolean(Prefs.HAS_SHOWN_TUTORIAL, true).apply()

        val tutorialFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (tutorialFragment != null) {
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
                remove(tutorialFragment)
            }
        }
    }

    /* ------------------------------------ Location listener ----------------------------------- */

    private fun updateLocationAvailability() {
        if (isLocationEnabled) {
            questAutoSyncer.startPositionTracking()
        } else {
            questAutoSyncer.stopPositionTracking()
        }
    }

    private fun onLocationRequestFinished(withLocationState: LocationState) {
        // if denied first time after exiting tutorial: ask again once (i.e. show rationale and ask again)
        if (!withLocationState.isEnabled) {
            if (!prefs.getBoolean(Prefs.FINISHED_FIRST_LOCATION_REQUEST, false)) {
                locationRequestFragment.startRequest()
            } else {
                toast(R.string.no_gps_no_quests, Toast.LENGTH_LONG)
            }
        }
        prefs.edit().putBoolean(Prefs.FINISHED_FIRST_LOCATION_REQUEST, true).apply()
        updateLocationAvailability()
    }

    companion object {
        // per application start settings
        private var dontShowRequestAuthorizationAgain = false
    }
}

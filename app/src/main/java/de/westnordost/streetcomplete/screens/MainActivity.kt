package de.westnordost.streetcomplete.screens

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
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
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.ConnectionException
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadProgressListener
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.ImageUploadServerException
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.VersionBannedException
import de.westnordost.streetcomplete.data.user.AuthorizationException
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.controls.MessagesButtonFragment
import de.westnordost.streetcomplete.screens.main.messages.MessagesContainerFragment
import de.westnordost.streetcomplete.screens.tutorial.TutorialFragment
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.isLocationEnabled
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.location.LocationRequester
import de.westnordost.streetcomplete.util.parseGeoUri
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity :
    BaseActivity(),
    MainFragment.Listener,
    TutorialFragment.Listener,
    MessagesButtonFragment.Listener {

    private val crashReportExceptionHandler: CrashReportExceptionHandler by inject()
    private val questAutoSyncer: QuestAutoSyncer by inject()
    private val downloadController: DownloadController by inject()
    private val uploadController: UploadController by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val userUpdater: UserUpdater by inject()
    private val elementEditsSource: ElementEditsSource by inject()
    private val noteEditsSource: NoteEditsSource by inject()
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource by inject()
    private val userLoginStatusController: UserLoginStatusController by inject()
    private val prefs: SharedPreferences by inject()

    private val requestLocation = LocationRequester(this, this)

    private var mainFragment: MainFragment? = null

    private val elementEditsListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) { lifecycleScope.launch { ensureLoggedIn() } }
        override fun onSyncedEdit(edit: ElementEdit) {}
        override fun onDeletedEdits(edits: List<ElementEdit>) {}
    }

    private val noteEditsListener = object : NoteEditsSource.Listener {
        override fun onAddedEdit(edit: NoteEdit) { lifecycleScope.launch { ensureLoggedIn() } }
        override fun onSyncedEdit(edit: NoteEdit) {}
        override fun onDeletedEdits(edits: List<NoteEdit>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(questAutoSyncer)
        crashReportExceptionHandler.askUserToSendCrashReportIfExists(this)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        setContentView(R.layout.activity_main)

        mainFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MainFragment?
        if (savedInstanceState == null) {
            val hasShownTutorial = prefs.getBoolean(Prefs.HAS_SHOWN_TUTORIAL, false)
            if (!hasShownTutorial && !userLoginStatusController.isLoggedIn) {
                supportFragmentManager.commit {
                    setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
                    add(R.id.fragment_container, TutorialFragment())
                }
            }
            if (userLoginStatusController.isLoggedIn && isConnected) {
                userUpdater.update()
            }
        }

        elementEditsSource.addListener(elementEditsListener)
        noteEditsSource.addListener(noteEditsListener)
    }

    private fun handleGeoUri() {
        if (Intent.ACTION_VIEW != intent.action) return
        val data = intent.data ?: return
        if ("geo" != data.scheme) return
        val geo = parseGeoUri(data) ?: return
        val zoom = if (geo.zoom == null || geo.zoom < 14) 18f else geo.zoom
        val pos = LatLon(geo.latitude, geo.longitude)
        mainFragment?.setCameraPosition(pos, zoom)
    }

    public override fun onStart() {
        super.onStart()

        if (prefs.getBoolean(Prefs.KEEP_SCREEN_ON, false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        downloadController.showNotification = false
        uploadController.showNotification = false
        uploadController.addUploadProgressListener(uploadProgressListener)
        downloadController.addDownloadProgressListener(downloadProgressListener)

        locationAvailabilityReceiver.addListener(::updateLocationAvailability)
        updateLocationAvailability(hasLocationPermission && isLocationEnabled)
    }

    override fun onBackPressed() {
        if (!forwardBackPressedToChildren()) super.onBackPressed()
    }

    private fun forwardBackPressedToChildren(): Boolean {
        val messagesContainerFragment = messagesContainerFragment
        if (messagesContainerFragment != null) {
            if (messagesContainerFragment.onBackPressed()) return true
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
        downloadController.showNotification = true
        uploadController.showNotification = true
        uploadController.removeUploadProgressListener(uploadProgressListener)
        downloadController.removeDownloadProgressListener(downloadProgressListener)
        locationAvailabilityReceiver.removeListener(::updateLocationAvailability)
    }

    override fun onDestroy() {
        super.onDestroy()
        elementEditsSource.removeListener(elementEditsListener)
        noteEditsSource.removeListener(noteEditsListener)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        findViewById<View>(R.id.main).requestLayout()
        // recreate the MessagesContainerFragment because it should load a new layout, see #2330
        supportFragmentManager.commit {
            replace(R.id.messages_container_fragment, MessagesContainerFragment())
        }
    }

    private suspend fun ensureLoggedIn() {
        if (!questAutoSyncer.isAllowedByPreference) return
        if (userLoginStatusController.isLoggedIn) return

        // new users should not be immediately pestered to login after each change (#1446)
        if (unsyncedChangesCountSource.getCount() < 3 || dontShowRequestAuthorizationAgain) return

        RequestLoginDialog(this).show()
        dontShowRequestAuthorizationAgain = true
    }

    private val isConnected: Boolean
        get() = getSystemService<ConnectivityManager>()?.activeNetworkInfo?.isConnected == true

    /* ------------------------------ Upload progress listener ---------------------------------- */

    private val uploadProgressListener: UploadProgressListener = object : UploadProgressListener {
        @AnyThread
        override fun onError(e: Exception) {
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
                    userLoginStatusController.logOut()
                    RequestLoginDialog(this@MainActivity).show()
                } else {
                    crashReportExceptionHandler.askUserToSendErrorReport(this@MainActivity,
                        R.string.upload_error, e)
                }
            }
        }
    }

    /* ----------------------------- Download Progress listener  -------------------------------- */

    private val downloadProgressListener: DownloadProgressListener = object :
        DownloadProgressListener {
        @AnyThread
        override fun onError(e: Exception) {
            runOnUiThread {
                // A network connection error is not the fault of this app. Nothing we can do about
                // it, so it does not make sense to send an error report. Just notify the user.
                if (e is ConnectionException) {
                    toast(R.string.download_server_error, Toast.LENGTH_LONG)
                } else if (e is AuthorizationException) {
                    // delete secret in case it failed while already having a token -> token is invalid
                    userLoginStatusController.logOut()
                } else {
                    crashReportExceptionHandler.askUserToSendErrorReport(this@MainActivity,
                        R.string.download_error, e)
                }
            }
        }
    }

    /* --------------------------------- MessagesButtonFragment.Listener ------------------------ */

    override fun onClickShowMessage(message: Message) {
        messagesContainerFragment?.showMessage(message)
    }

    private val messagesContainerFragment get() =
        supportFragmentManager.findFragmentById(R.id.messages_container_fragment) as? MessagesContainerFragment

    /* --------------------------------- MainFragment.Listener ---------------------------------- */

    override fun onMapInitialized() {
        handleGeoUri()
    }

    /* ------------------------------- TutorialFragment.Listener -------------------------------- */

    override fun onTutorialFinished() {
        lifecycleScope.launch {
            val hasLocation = requestLocation()
            // if denied first time after exiting tutorial: ask again once (i.e. show rationale and ask again)
            if (!hasLocation) {
                requestLocation()
            } else {
                toast(R.string.no_gps_no_quests, Toast.LENGTH_LONG)
            }
        }

        prefs.edit { putBoolean(Prefs.HAS_SHOWN_TUTORIAL, true) }

        val tutorialFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (tutorialFragment != null) {
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
                remove(tutorialFragment)
            }
        }
    }

    /* ------------------------------------ Location listener ----------------------------------- */

    private fun updateLocationAvailability(isAvailable: Boolean) {
        if (isAvailable) {
            questAutoSyncer.startPositionTracking()
        } else {
            questAutoSyncer.stopPositionTracking()
        }
    }

    companion object {
        // per application start settings
        private var dontShowRequestAuthorizationAgain = false
    }
}

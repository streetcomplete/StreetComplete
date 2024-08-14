package de.westnordost.streetcomplete.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import androidx.core.text.parseAsHtml
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.upload.VersionBannedException
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.user.UserLoginController
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.messages.MessagesContainerFragment
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialFragment
import de.westnordost.streetcomplete.screens.tutorial.TutorialFragment
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.ktx.isLocationAvailable
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.location.LocationRequestFragment
import de.westnordost.streetcomplete.util.parseGeoUri
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity :
    BaseActivity(),
    MainFragment.Listener,
    TutorialFragment.Listener,
    OverlaysTutorialFragment.Listener {

    private val crashReportExceptionHandler: CrashReportExceptionHandler by inject()
    private val questAutoSyncer: QuestAutoSyncer by inject()
    private val downloadProgressSource: DownloadProgressSource by inject()
    private val uploadProgressSource: UploadProgressSource by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val elementEditsSource: ElementEditsSource by inject()
    private val noteEditsSource: NoteEditsSource by inject()
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource by inject()
    private val userLoginController: UserLoginController by inject()
    private val urlConfigController: UrlConfigController by inject()
    private val questPresetsSource: QuestPresetsSource by inject()
    private val prefs: Preferences by inject()

    private var mainFragment: MainFragment? = null

    private val requestLocationPermissionResultReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!intent.getBooleanExtra(LocationRequestFragment.GRANTED, false)) {
                toast(R.string.no_gps_no_quests, Toast.LENGTH_LONG)
            }
        }
    }

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

        LocalBroadcastManager.getInstance(this).registerReceiver(
            requestLocationPermissionResultReceiver,
            IntentFilter(LocationRequestFragment.REQUEST_LOCATION_PERMISSION_RESULT)
        )

        lifecycle.addObserver(questAutoSyncer)
        crashReportExceptionHandler.askUserToSendCrashReportIfExists(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        setContentView(R.layout.activity_main)

        mainFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MainFragment?
        if (savedInstanceState == null) {
            supportFragmentManager.commit { add(LocationRequestFragment(), TAG_LOCATION_REQUEST) }
            if (!prefs.hasShownTutorial && !userLoginController.isLoggedIn) {
                supportFragmentManager.commit {
                    setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
                    add(R.id.fragment_container, TutorialFragment())
                }
            }
        }

        elementEditsSource.addListener(elementEditsListener)
        noteEditsSource.addListener(noteEditsListener)

        handleUrlConfig()
    }

    private fun handleUrlConfig() {
        if (intent.action != Intent.ACTION_VIEW) return
        val data = intent.data ?: return
        val config = urlConfigController.parse(data.toString()) ?: return

        val alreadyExists = config.presetName == null || questPresetsSource.getByName(config.presetName) != null
        val name = config.presetName ?: getString(R.string.quest_presets_default_name)

        val htmlName = "<i>" + Html.escapeHtml(name) + "</i>"
        val text = StringBuilder()
        text.append(getString(R.string.urlconfig_apply_message, htmlName))
        text.append("<br><br>")
        if (alreadyExists) {
            text.append("<b>" + getString(R.string.urlconfig_apply_message_overwrite) + "</b>")
            text.append("<br><br>")
        } else {
            text.append(getString(R.string.urlconfig_switch_hint))
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.urlconfig_apply_title)
            .setMessage(text.toString().parseAsHtml())
            .setPositiveButton(android.R.string.ok) { _, _ -> urlConfigController.apply(config) }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show()
    }

    private fun handleGeoUri() {
        if (intent.action != Intent.ACTION_VIEW) return
        val data = intent.data ?: return
        if ("geo" != data.scheme) return
        val geo = parseGeoUri(data) ?: return
        val zoom = if (geo.zoom == null || geo.zoom < 14) 18.0 else geo.zoom
        val pos = LatLon(geo.latitude, geo.longitude)
        mainFragment?.setCameraPosition(pos, zoom)
    }

    public override fun onStart() {
        super.onStart()

        updateScreenOn()
        uploadProgressSource.addListener(uploadProgressListener)
        downloadProgressSource.addListener(downloadProgressListener)

        locationAvailabilityReceiver.addListener(::updateLocationAvailability)
        updateLocationAvailability(isLocationAvailable)
    }

    public override fun onStop() {
        super.onStop()
        uploadProgressSource.removeListener(uploadProgressListener)
        downloadProgressSource.removeListener(downloadProgressListener)
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
        if (userLoginController.isLoggedIn) return

        // new users should not be immediately pestered to login after each change (#1446)
        if (unsyncedChangesCountSource.getCount() < 3 || dontShowRequestAuthorizationAgain) return

        RequestLoginDialog(this).show()
        dontShowRequestAuthorizationAgain = true
    }

    /* ------------------------------- Preferences listeners ------------------------------------ */

    private fun updateScreenOn() {
        if (prefs.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /* ------------------------------ Upload progress listener ---------------------------------- */

    private val uploadProgressListener = object : UploadProgressSource.Listener {
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
                } else if (e is ConnectionException) {
                    /* A network connection error or server error is not the fault of this app.
                       Nothing we can do about it, so it does not make sense to send an error
                       report. Just notify the user. */
                    toast(R.string.upload_server_error, Toast.LENGTH_LONG)
                } else if (e is AuthorizationException) {
                    // delete secret in case it failed while already having a token -> token is invalid
                    userLoginController.logOut()
                    RequestLoginDialog(this@MainActivity).show()
                } else {
                    crashReportExceptionHandler.askUserToSendErrorReport(this@MainActivity,
                        R.string.upload_error, e)
                }
            }
        }
    }

    /* ----------------------------- Download Progress listener  -------------------------------- */

    private val downloadProgressListener = object : DownloadProgressSource.Listener {
        @AnyThread
        override fun onError(e: Exception) {
            runOnUiThread {
                // A network connection error is not the fault of this app. Nothing we can do about
                // it, so it does not make sense to send an error report. Just notify the user.
                if (e is ConnectionException) {
                    toast(R.string.download_server_error, Toast.LENGTH_LONG)
                } else if (e is AuthorizationException) {
                    // delete secret in case it failed while already having a token -> token is invalid
                    userLoginController.logOut()
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
        requestLocation()

        prefs.hasShownTutorial = true
        removeTutorialFragment()
    }

    private fun requestLocation() {
        (supportFragmentManager.findFragmentByTag(TAG_LOCATION_REQUEST) as? LocationRequestFragment)?.startRequest()
    }

    private fun removeTutorialFragment() {
        val tutorialFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (tutorialFragment != null) {
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
                remove(tutorialFragment)
            }
        }
    }

    /* ---------------------------- OverlaysButtonFragment.Listener ----------------------------- */

    override fun onShowOverlaysTutorial() {
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
            add(R.id.fragment_container, OverlaysTutorialFragment())
        }
    }

    /* --------------------------- OverlaysTutorialFragment.Listener ---------------------------- */

    override fun onOverlaysTutorialFinished() {
        prefs.hasShownOverlaysTutorial = true
        removeTutorialFragment()
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
        private const val TAG_LOCATION_REQUEST = "LocationRequestFragment"

        // per application start settings
        private var dontShowRequestAuthorizationAgain = false
    }
}

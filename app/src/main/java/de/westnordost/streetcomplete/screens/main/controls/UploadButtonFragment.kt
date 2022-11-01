package de.westnordost.streetcomplete.screens.main.controls

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Fragment that shows the upload button, including upload progress etc. */
class UploadButtonFragment : Fragment(R.layout.fragment_upload_button) {

    private val uploadController: UploadController by inject()
    private val userLoginStatusSource: UserLoginStatusSource by inject()
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource by inject()
    private val prefs: SharedPreferences by inject()

    private val uploadButton get() = view as UploadButton

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { viewLifecycleScope.launch { updateCount() } }
        override fun onDecreased() { viewLifecycleScope.launch { updateCount() } }
    }

    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { viewLifecycleScope.launch { updateProgress(true) } }
        override fun onFinished() { viewLifecycleScope.launch { updateProgress(false) } }
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadButton.setOnClickListener {
            if (isConnected()) {
                uploadChanges()
            } else {
                context?.toast(R.string.offline)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        /* Only show the button if autosync is off */
        uploadButton.isGone = isAutosync
        if (!isAutosync) {
            viewLifecycleScope.launch { updateCount() }
            updateProgress(uploadController.isUploadInProgress)
            unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
            uploadController.addUploadProgressListener(uploadProgressListener)
        }
    }

    override fun onStop() {
        super.onStop()
        uploadController.removeUploadProgressListener(uploadProgressListener)
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
    }

    // ---------------------------------------------------------------------------------------------

    private val isAutosync: Boolean get() =
        Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) == Prefs.Autosync.ON

    private suspend fun updateCount() {
        uploadButton.uploadableCount = unsyncedChangesCountSource.getCount()
    }

    private fun updateProgress(isUploadInProgress: Boolean) {
        uploadButton.isEnabled = !isUploadInProgress
    }

    private fun uploadChanges() {
        // because the app should ask for permission even if there is nothing to upload right now
        if (!userLoginStatusSource.isLoggedIn) {
            context?.let { RequestLoginDialog(it).show() }
        } else {
            uploadController.upload()
        }
    }

    /* Does not necessarily mean that the user has internet. But if he is not connected, he will
     * not have internet */
    private fun isConnected(): Boolean {
        val connectivityManager = context?.getSystemService<ConnectivityManager>()
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

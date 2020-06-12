package de.westnordost.streetcomplete.controls

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountListener
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog
import kotlinx.coroutines.*
import javax.inject.Inject

/** Fragment that shows the upload button, including upload progress etc. */
class UploadButtonFragment : Fragment(R.layout.fragment_upload_button),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var uploadController: UploadController
    @Inject internal lateinit var userController: UserController
    @Inject internal lateinit var unsyncedChangesCountSource: UnsyncedChangesCountSource
    @Inject internal lateinit var prefs: SharedPreferences

    private val uploadButton get() = view as UploadButton

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountListener {
        override fun onUnsyncedChangesCountIncreased() { launch(Dispatchers.Main) { updateCount() }}
        override fun onUnsyncedChangesCountDecreased() { launch(Dispatchers.Main) { updateCount() }}
    }

    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { launch(Dispatchers.Main) { updateProgress(true) } }
        override fun onFinished() { launch(Dispatchers.Main) { updateProgress(false) } }
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    init {
        Injector.instance.applicationComponent.inject(this)
    }

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
        if (!isAutosync) {
            uploadButton.visibility = View.VISIBLE
            updateCount()
            updateProgress(uploadController.isUploadInProgress)
            uploadController.addUploadProgressListener(uploadProgressListener)
            unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
        } else {
            uploadButton.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        uploadController.removeUploadProgressListener(uploadProgressListener)
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    // ---------------------------------------------------------------------------------------------

    private val isAutosync: Boolean get() =
        Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) == Prefs.Autosync.ON

    private fun updateCount() {
        uploadButton.uploadableCount = unsyncedChangesCountSource.count
    }

    private fun updateProgress(isUploadInProgress: Boolean) {
        if (isUploadInProgress) {
            uploadButton.isEnabled = false
            uploadButton.showProgress = true
        } else {
            uploadButton.isEnabled = true
            uploadButton.showProgress = false
        }
    }

    private fun uploadChanges() {
        // because the app should ask for permission even if there is nothing to upload right now
        if (!userController.isLoggedIn) {
            context?.let { RequestLoginDialog(it).show() }
        } else {
            uploadController.upload()
        }
    }

    /** Does not necessarily mean that the user has internet. But if he is not connected, he will
      * not have internet  */
    private fun isConnected(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

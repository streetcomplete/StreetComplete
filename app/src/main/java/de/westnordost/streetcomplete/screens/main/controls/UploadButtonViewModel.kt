package de.westnordost.streetcomplete.screens.main.controls

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.platform.InternetConnectionState
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class UploadButtonViewModel : ViewModel() {
    abstract val isAutosync: StateFlow<Boolean>
    abstract val isUploadInProgress: StateFlow<Boolean>
    abstract val unsyncedChangesCount: StateFlow<Int>

    abstract val isLoggedIn: Boolean
    abstract val isConnected: Boolean

    abstract fun upload()
}

class UploadButtonViewModelImpl(
    private val uploadController: UploadController,
    private val uploadProgressSource: UploadProgressSource,
    private val userLoginStatusSource: UserLoginStatusSource,
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val internetConnectionState: InternetConnectionState,
    private val prefs: ObservableSettings,
) : UploadButtonViewModel() {

    override val isAutosync = MutableStateFlow(false)
    override val isUploadInProgress = MutableStateFlow(uploadProgressSource.isUploadInProgress)
    override val unsyncedChangesCount = MutableStateFlow(0)

    override var isLoggedIn: Boolean = false
        private set
    override val isConnected: Boolean get() = internetConnectionState.isConnected

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { unsyncedChangesCount.update { it + 1 } }
        override fun onDecreased() { unsyncedChangesCount.update { it - 1 } }
    }

    private val uploadProgressListener = object : UploadProgressSource.Listener {
        override fun onStarted() { isUploadInProgress.value = true }
        override fun onFinished() { isUploadInProgress.value = false }
    }

    private val autosyncListener: SettingsListener

    init {
        launch(IO) {
            isLoggedIn = userLoginStatusSource.isLoggedIn
        }
        launch(IO) {
            isAutosync.value = prefs.getString(Prefs.AUTOSYNC, ApplicationConstants.DEFAULT_AUTOSYNC) == Prefs.Autosync.ON.name
        }
        launch(IO) {
            unsyncedChangesCount.value = unsyncedChangesCountSource.getCount()
        }

        autosyncListener = prefs.addStringListener(Prefs.AUTOSYNC, ApplicationConstants.DEFAULT_AUTOSYNC) { autosync ->
            isAutosync.value = autosync == Prefs.Autosync.ON.name
        }
        unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
        uploadProgressSource.addListener(uploadProgressListener)
    }

    override fun onCleared() {
        uploadProgressSource.removeListener(uploadProgressListener)
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
        autosyncListener.deactivate()
    }

    override fun upload() {
        uploadController.upload(isUserInitiated = true)
    }
}

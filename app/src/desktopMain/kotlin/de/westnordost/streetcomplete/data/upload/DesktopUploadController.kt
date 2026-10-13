package de.westnordost.streetcomplete.data.upload

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

class DesktopUploadController(private val uploader: Uploader) : UploadController {
    // Uploader already logs errors and notifies its listeners
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, _ -> }
    )
    private var job: Job? = null

    override fun upload(isUserInitiated: Boolean) {
        scope.launch {
            // like on Android: while an upload is running, another one is not started
            if (job?.isActive == true) return@launch
            job = currentCoroutineContext().job
            uploader.upload()
        }
    }

    fun close() { scope.cancel() }
}

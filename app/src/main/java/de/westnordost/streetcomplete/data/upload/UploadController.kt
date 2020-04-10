package de.westnordost.streetcomplete.data.upload

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import javax.inject.Inject
import javax.inject.Singleton

/** Controls uploading */
@Singleton class UploadController @Inject constructor(
    private val context: Context
): UploadProgressSource {

    private var uploadServiceIsBound = false
    private var uploadService: UploadService.Interface? = null
    private val uploadServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            uploadService = service as UploadService.Interface
            uploadService?.setProgressListener(uploadProgressRelay)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            uploadService = null
        }
    }
    private val uploadProgressRelay = UploadProgressRelay()

    override val isUploadInProgress: Boolean get() =
        uploadService?.isUploadInProgress == true

    init {
        bindServices()
    }

    /** Collect and upload all changes made by the user  */
    fun upload() {
        context.startService(UploadService.createIntent(context))
    }

    private fun bindServices() {
        uploadServiceIsBound = context.bindService(
            Intent(context, UploadService::class.java),
            uploadServiceConnection, Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindServices() {
        if (uploadServiceIsBound) context.unbindService(uploadServiceConnection)
        uploadServiceIsBound = false
    }

    override fun addUploadProgressListener(listener: UploadProgressListener) {
        uploadProgressRelay.addListener(listener)
    }
    override fun removeUploadProgressListener(listener: UploadProgressListener) {
        uploadProgressRelay.removeListener(listener)
    }
}
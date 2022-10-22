package de.westnordost.streetcomplete.quests.note_discussion

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ActivityForResultLauncher
import de.westnordost.streetcomplete.util.decodeScaledBitmapAndNormalize
import de.westnordost.streetcomplete.util.ktx.hasCameraPermission
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/** Requests permission to, takes a photo and rescales it */
class TakePhoto(
    activityResultCaller: ActivityResultCaller,
    private val askUserToAcknowledgeCameraPermissionRationale: suspend () -> Boolean,
) {
    private val requestPermission = ActivityForResultLauncher(activityResultCaller, ActivityResultContracts.RequestPermission())
    private val takePhoto = ActivityForResultLauncher(activityResultCaller, ActivityResultContracts.TakePicture())

    /** Returns the file path where to find the taken photo or null if no photo has been taken.
     *
     *  May return an ActivityNotFoundException if there is no camera app or an IOException if the
     *  photo file could not be created. */
    suspend operator fun invoke(activity: Activity): String? {
        if (!activity.hasCameraPermission) {
            if (!requestCameraPermission(activity)) {
                return null
            }
        }

        var file: File? = null
        try {
            file = createImageFile(activity)
            val photoUri = FileProvider.getUriForFile(activity, activity.getString(R.string.fileprovider_authority), file)
            val saved = takePhoto(photoUri)
            if (!saved) {
                deleteImageFile(file)
                return null
            }
            rescaleImageFile(file)

            return file.path
        } catch (e: Exception) {
            file?.let { deleteImageFile(it) }
            throw e
        }
    }

    private suspend fun requestCameraPermission(activity: Activity): Boolean {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            if (!askUserToAcknowledgeCameraPermissionRationale()) {
                return false
            }
        }
        return requestPermission(Manifest.permission.CAMERA)
    }

    private fun createImageFile(context: Context): File {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFileName = "photo_" + nowAsEpochMilliseconds() + ".jpg"
        val file = File(directory, imageFileName)
        if (!file.createNewFile()) throw IOException("Photo file with exactly the same name already exists")
        return file
    }

    private fun rescaleImageFile(file: File) {
        val bitmap = decodeScaledBitmapAndNormalize(file.path, ApplicationConstants.ATTACH_PHOTO_MAXWIDTH, ApplicationConstants.ATTACH_PHOTO_MAXHEIGHT) ?: throw IOException()
        val out = FileOutputStream(file.path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, ApplicationConstants.ATTACH_PHOTO_QUALITY, out)
    }

    private fun deleteImageFile(file: File) {
        if (file.exists()) file.delete()
    }
}

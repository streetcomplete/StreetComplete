package de.westnordost.streetcomplete.util.photo

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.logs.Log
import java.io.File

class TakePhotoFragment : Fragment() {

    interface Listener {
        fun onTookPhoto(path: String)
    }

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture(), ::onTookPhoto)

    private var newPhotoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newPhotoFile = savedInstanceState?.getString(NEW_PHOTO_FILE)?.let { File(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NEW_PHOTO_FILE, newPhotoFile?.path)
    }

    fun takePhoto() {
        val context = context ?: return
        try {
            val file = createPhotoFile(context)
            newPhotoFile = file
            val photoUri = FileProvider.getUriForFile(context, getString(R.string.fileprovider_authority), file)
            takePhoto.launch(photoUri)
        } catch (e: Exception) {
            newPhotoFile?.let { if (it.exists()) it.delete() }
            when (e) {
                is ActivityNotFoundException -> context.toast(R.string.no_camera_app)
                else -> {
                    Log.e(TAG, "Unable to create photo", e)
                    context.toast(R.string.quest_leave_new_note_create_image_error)
                }
            }
        }
    }

    private fun onTookPhoto(hasSavedPhoto: Boolean) {
        val file = newPhotoFile
        if (!hasSavedPhoto) {
            newPhotoFile?.let { if (it.exists()) it.delete() }
            return
        }
        if (file != null) {
            rescalePhoto(file)
            listener?.onTookPhoto(file.path)
        }
    }

    companion object {
        private const val TAG = "TakePhotoFragment"
        private const val NEW_PHOTO_FILE = "photo_file"
    }
}

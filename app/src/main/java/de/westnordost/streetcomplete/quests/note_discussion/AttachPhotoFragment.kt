package de.westnordost.streetcomplete.quests.note_discussion

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.TAG_GPS_IMG_DIRECTION
import androidx.exifinterface.media.ExifInterface.TAG_GPS_IMG_DIRECTION_REF
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.deleteImages
import de.westnordost.streetcomplete.databinding.FragmentAttachPhotoBinding
import de.westnordost.streetcomplete.util.decodeScaledBitmapAndNormalize
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

class AttachPhotoFragment : Fragment(R.layout.fragment_attach_photo) {

    private val binding by viewBinding(FragmentAttachPhotoBinding::bind)

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture(), ::onTookPhoto)

    private lateinit var noteImageAdapter: NoteImageAdapter
    private var newPhotoFile: File? = null

    val imagePaths: List<String> get() = noteImageAdapter.list
    var hasGpxAttached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paths = savedInstanceState?.getStringArrayList(PHOTO_PATHS) ?: ArrayList()
        noteImageAdapter = NoteImageAdapter(paths, requireContext())
        newPhotoFile = savedInstanceState?.getString(NEW_PHOTO_FILE)?.let { File(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.isGone = !requireActivity().packageManager.hasSystemFeature(FEATURE_CAMERA_ANY)

        binding.takePhotoButton.setOnClickListener { takePhoto() }
        binding.photosList.adapter = noteImageAdapter
        noteImageAdapter.registerAdapterDataObserver(AdapterDataChangedWatcher { updateHintVisibility() })

        binding.attachedGpxView.isGone = !hasGpxAttached

        updateHintVisibility()
    }

    private fun updateHintVisibility() {
        binding.photosList.isGone = imagePaths.isEmpty()
        binding.photosAreUsefulExplanation.isGone = imagePaths.isNotEmpty()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(PHOTO_PATHS, ArrayList(imagePaths))
        outState.putString(NEW_PHOTO_FILE, newPhotoFile?.path)
    }

    private fun takePhoto() {
        val activity = activity ?: return
        try {
            val file = createImageFile(activity)
            newPhotoFile = file
            val photoUri = FileProvider.getUriForFile(activity, activity.getString(R.string.fileprovider_authority), file)
            takePhoto.launch(photoUri)
        } catch (e: Exception) {
            newPhotoFile?.let { deleteImageFile(it) }
            when (e) {
                is ActivityNotFoundException -> context?.toast(R.string.no_camera_app)
                else -> {
                    Log.e(TAG, "Unable to create photo", e)
                    context?.toast(R.string.quest_leave_new_note_create_image_error)
                }
            }
        }
    }

    private fun onTookPhoto(hasSavedPhoto: Boolean) {
        val file = newPhotoFile
        if (!hasSavedPhoto) {
            newPhotoFile?.let { deleteImageFile(it) }
            return
        }
        if (file != null) {
            val exif = ExifInterface(file)
            rescaleImageFile(file)
            copyDirectionExifData(file, exif)

            noteImageAdapter.list.add(file.path)
            noteImageAdapter.notifyItemInserted(imagePaths.size - 1)
        }
    }

    fun deleteImages() {
        deleteImages(imagePaths)
    }

    companion object {
        private const val TAG = "AttachPhotoFragment"
        private const val PHOTO_PATHS = "photo_paths"
        private const val NEW_PHOTO_FILE = "photo_file"
    }
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

private fun copyDirectionExifData(file: File, exif: ExifInterface) {
    val newExif = ExifInterface(file.path)
    newExif.setAttribute(TAG_GPS_IMG_DIRECTION, exif.getAttribute(TAG_GPS_IMG_DIRECTION))
    newExif.setAttribute(TAG_GPS_IMG_DIRECTION_REF, exif.getAttribute(TAG_GPS_IMG_DIRECTION_REF))
    newExif.saveAttributes()
}

private fun deleteImageFile(file: File) {
    if (file.exists()) file.delete()
}

package de.westnordost.streetcomplete.quests.note_discussion

import android.content.ActivityNotFoundException
import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.ApplicationConstants.ATTACH_PHOTO_MAXHEIGHT
import de.westnordost.streetcomplete.ApplicationConstants.ATTACH_PHOTO_MAXWIDTH
import de.westnordost.streetcomplete.ApplicationConstants.ATTACH_PHOTO_QUALITY
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.deleteImages
import de.westnordost.streetcomplete.databinding.FragmentAttachPhotoBinding
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.util.ActivityForResultLauncher
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.decodeScaledBitmapAndNormalize
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AttachPhotoFragment : Fragment(R.layout.fragment_attach_photo) {

    private val binding by viewBinding(FragmentAttachPhotoBinding::bind)
    private val takePhoto = ActivityForResultLauncher(this, ActivityResultContracts.TakePicture())

    private lateinit var noteImageAdapter: NoteImageAdapter

    val imagePaths: List<String> get() = noteImageAdapter.list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paths = savedInstanceState?.getStringArrayList(PHOTO_PATHS) ?: ArrayList()
        noteImageAdapter = NoteImageAdapter(paths, requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.isGone = !requireActivity().packageManager.hasSystemFeature(FEATURE_CAMERA_ANY)

        binding.takePhotoButton.setOnClickListener { lifecycleScope.launch { takePhoto() } }
        binding.photosList.adapter = noteImageAdapter
        noteImageAdapter.registerAdapterDataObserver(AdapterDataChangedWatcher { updateHintVisibility() })

        updateHintVisibility()
    }

    private fun updateHintVisibility() {
        binding.photosList.isGone = imagePaths.isEmpty()
        binding.photosAreUsefulExplanation.isGone = imagePaths.isNotEmpty()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(PHOTO_PATHS, ArrayList(imagePaths))
    }

    private suspend fun takePhoto() {
        var file: File? = null
        try {
            file = createImageFile()
            val photoUri = FileProvider.getUriForFile(requireContext(), getString(R.string.fileprovider_authority), file)
            val saved = takePhoto(photoUri)
            if (!saved) {
                deleteImageFile(file)
                return
            }
            rescaleImageFile(file)

            noteImageAdapter.list.add(file.path)
            noteImageAdapter.notifyItemInserted(imagePaths.size - 1)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to create photo", e)
            file?.let { deleteImageFile(it) }
            when (e) {
                is ActivityNotFoundException -> context?.toast(R.string.no_camera_app)
                else -> context?.toast(R.string.quest_leave_new_note_create_image_error)
            }
        }
    }

    private fun createImageFile(): File {
        val directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFileName = "photo_" + System.currentTimeMillis() + ".jpg"
        val file = File(directory, imageFileName)
        if (!file.createNewFile()) throw IOException("Photo file with exactly the same name already exists")
        return file
    }

    private fun rescaleImageFile(file: File) {
        val bitmap = decodeScaledBitmapAndNormalize(file.path, ATTACH_PHOTO_MAXWIDTH, ATTACH_PHOTO_MAXHEIGHT) ?: throw IOException()
        val out = FileOutputStream(file.path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, ATTACH_PHOTO_QUALITY, out)
    }

    private fun deleteImageFile(file: File) {
        if (file.exists()) file.delete()
    }

    fun deleteImages() {
        deleteImages(imagePaths)
    }

    companion object {
        private const val TAG = "AttachPhotoFragment"

        private const val PHOTO_PATHS = "photo_paths"
    }
}

package de.westnordost.streetcomplete.quests.note_discussion

import android.content.ActivityNotFoundException
import android.content.SharedPreferences
import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.deleteImages
import de.westnordost.streetcomplete.databinding.FragmentAttachPhotoBinding
import de.westnordost.streetcomplete.util.ktx.hasCameraPermission
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.inject
import java.lang.Exception
import kotlin.coroutines.resume

class AttachPhotoFragment : Fragment(R.layout.fragment_attach_photo) {

    private val binding by viewBinding(FragmentAttachPhotoBinding::bind)
    private val prefs: SharedPreferences by inject()
    private val launchTakePhoto = TakePhoto(this, ::askUserToAcknowledgeCameraPermissionRationale, prefs)

    private lateinit var noteImageAdapter: NoteImageAdapter

    val imagePaths: List<String> get() = noteImageAdapter.list
    var hasGpxAttached = false

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
    }

    private suspend fun takePhoto() {
        try {
            val filePath = launchTakePhoto(requireActivity())
            if (filePath == null) {
                if (activity?.hasCameraPermission == false) context?.toast(R.string.no_camera_permission_toast)
                return
            }
            noteImageAdapter.list.add(filePath)
            noteImageAdapter.notifyItemInserted(imagePaths.size - 1)
        } catch (e: Exception) {
            when (e) {
                is ActivityNotFoundException -> context?.toast(R.string.no_camera_app)
                else -> {
                    Log.e(TAG, "Unable to create photo", e)
                    context?.toast(R.string.quest_leave_new_note_create_image_error)
                }
            }
        }
    }

    fun deleteImages() {
        deleteImages(imagePaths)
    }

    /* ----------------------------------- Permission request ----------------------------------- */

    /** Show dialog that explains why the camera permission is necessary. Returns whether the user
     *  acknowledged the rationale. */
    private suspend fun askUserToAcknowledgeCameraPermissionRationale(): Boolean =
        suspendCancellableCoroutine { cont ->
            val dlg = AlertDialog.Builder(requireContext())
                .setTitle(R.string.no_camera_permission_warning_title)
                .setMessage(R.string.no_camera_permission_warning)
                .setPositiveButton(android.R.string.ok) { _, _ -> cont.resume(true) }
                .setNegativeButton(android.R.string.cancel) { _, _ -> cont.resume(false) }
                .setOnCancelListener { cont.resume(false) }
                .create()
            cont.invokeOnCancellation { dlg.cancel() }
            dlg.show()
        }

    companion object {
        private const val TAG = "AttachPhotoFragment"
        private const val PHOTO_PATHS = "photo_paths"
    }
}

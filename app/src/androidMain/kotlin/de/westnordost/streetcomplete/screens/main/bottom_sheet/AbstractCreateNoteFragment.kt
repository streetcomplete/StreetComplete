package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.photo.TakePhotoFragment
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.koin.android.ext.android.inject
import java.io.File
import kotlin.getValue

/** Abstract base class for a bottom sheet that lets the user create a note */
abstract class AbstractCreateNoteFragment :
    AbstractBottomSheetFragment(), TakePhotoFragment.Listener {

    private val fileSystem: FileSystem by inject()

    protected abstract val okButtonContainer: View
    protected abstract val okButton: View

    protected var noteText: MutableState<String> = mutableStateOf("")
    protected var noteImagePaths: MutableState<List<String>> = mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (childFragmentManager.findFragmentByTag(TAG_TAKE_PHOTO) == null) {
            childFragmentManager.commit { add(TakePhotoFragment(), TAG_TAKE_PHOTO) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        okButton.setOnClickListener { onClickOk() }

        updateOkButtonEnablement()
    }

    private fun onClickOk() {
        onComposedNote(noteText.value, noteImagePaths.value)
    }

    override fun onDiscard() {
        for (path in noteImagePaths.value) {
            fileSystem.delete(Path(path), mustExist = false)
        }
    }

    override fun isRejectingClose() =
        noteText.value.isNotBlank() || noteImagePaths.value.isNotEmpty()

    protected fun updateOkButtonEnablement() {
        if (noteText.value.isNotBlank()) {
            okButtonContainer.popIn()
        } else {
            okButtonContainer.popOut()
        }
    }

    protected abstract fun onComposedNote(text: String, imagePaths: List<String>)

    protected fun takePhoto() {
        (childFragmentManager.findFragmentByTag(TAG_TAKE_PHOTO) as? TakePhotoFragment)?.takePhoto()
    }

    override fun onTookPhoto(path: String) {
        noteImagePaths.value += path
    }

    protected fun deleteImage(path: String) {
        fileSystem.delete(Path(path), mustExist = false)
        noteImagePaths.value = noteImagePaths.value.filter { it != path }
    }

    companion object {
        private const val TAG_TAKE_PHOTO = "TakePhotoFragment"
    }
}

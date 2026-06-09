package de.westnordost.streetcomplete.ui.util.photo

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.util.ktx.launch
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlin.collections.plus

abstract class PhotosViewModel : ViewModel() {

    /** Whether taking a photo is supported (on this device) at all */
    abstract fun isTakePhotoSupported(): Boolean

    /** Launch the taking of a photo */
    abstract fun takePhoto()

    /** Photo files currently already taken */
    abstract val imagePaths: StateFlow<List<String>>

    abstract fun deleteImagePath(index: Int)

    abstract fun deleteAllImagePaths()
}

class PhotosViewModelImpl(
    private val checkHasCamera: HasCameraChecker,
    private val fileSystem: FileSystem,
) : PhotosViewModel() {

    override val imagePaths = MutableStateFlow<List<String>>(emptyList())

    override fun isTakePhotoSupported(): Boolean = checkHasCamera()

    override fun takePhoto() {
        launch(Dispatchers.IO) {
            val file = FileKit.openCameraPicker()
            if (file != null) {
                val compressedImage = FileKit.compressImage(
                    file = file,
                    quality = ApplicationConstants.ATTACH_PHOTO_QUALITY,
                    maxWidth = ApplicationConstants.ATTACH_PHOTO_MAX_SIZE,
                    maxHeight = ApplicationConstants.ATTACH_PHOTO_MAX_SIZE,
                )
                file.write(compressedImage)
                imagePaths.value + file.path
            }
        }
    }

    override fun deleteImagePath(index: Int) {
        val image = imagePaths.value[index]
        imagePaths.value = imagePaths.value.filterIndexed { i, file -> i != index }
        launch(Dispatchers.IO) {
            fileSystem.delete(Path(image))
        }
    }

    override fun deleteAllImagePaths() {
        imagePaths.value = emptyList()
        launch(Dispatchers.IO) {
            imagePaths.value.forEach { fileSystem.delete(Path(it), mustExist = false) }
        }
    }
}

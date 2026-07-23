package de.westnordost.streetcomplete.ui.util.photo

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

abstract class PhotosViewModel : ViewModel() {

    /** Whether taking a photo is supported (on this device) at all */
    abstract fun isTakePhotoSupported(): Boolean

    /** Photo files currently already taken */
    abstract val imagePaths: StateFlow<List<String>>

    abstract fun addImagePath(path: String)

    abstract fun deleteImagePath(index: Int)

    abstract fun deleteAllImagePaths()
}

class PhotosViewModelImpl(
    private val checkHasCamera: HasCameraChecker,
    private val fileSystem: FileSystem,
) : PhotosViewModel() {

    override val imagePaths = MutableStateFlow<List<String>>(emptyList())

    override fun isTakePhotoSupported(): Boolean = checkHasCamera()

    override fun addImagePath(path: String) {
        imagePaths.value += path
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

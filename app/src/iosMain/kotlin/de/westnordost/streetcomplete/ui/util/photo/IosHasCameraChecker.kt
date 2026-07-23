package de.westnordost.streetcomplete.ui.util.photo

import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType

object IosHasCameraChecker : HasCameraChecker {
    override fun invoke(): Boolean =
        UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    )
}

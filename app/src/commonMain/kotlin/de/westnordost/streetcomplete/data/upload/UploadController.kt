package de.westnordost.streetcomplete.data.upload

/** Controls uploading */
interface UploadController {
    /** Collect and upload all changes made by the user  */
    fun upload(isUserInitiated: Boolean)
}

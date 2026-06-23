package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.ConnectionException

/** Upload and activate a list of image paths to an instance of the
 *  https://github.com/streetcomplete/sc-photo-service
 */
interface PhotoServiceApiClient {
    /** Upload list of images.
     *
     *  @throws ConnectionException on connection or server error */
    suspend fun upload(imagePaths: List<String>): List<String>

    /** Activate the images in the given note.
     *
     *  @throws ConnectionException on connection or server error */
    suspend fun activate(noteId: Long)
}

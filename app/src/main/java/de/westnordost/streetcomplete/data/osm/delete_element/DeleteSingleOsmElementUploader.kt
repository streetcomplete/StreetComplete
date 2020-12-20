package de.westnordost.streetcomplete.data.osm.delete_element

import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import java.net.HttpURLConnection
import javax.inject.Inject

class DeleteSingleOsmElementUploader @Inject constructor(private val mapDataApi: MapDataApi) {

    fun upload(changesetId: Long, element: OsmElement) {
        try {
            uploadDeletion(changesetId, element)
        } catch (e: OsmConflictException) {
            throw ElementConflictException(e.message, e)
        } catch (e: OsmNotFoundException) {
            throw ElementDeletedException(e.message, e)
        }
    }

    private fun uploadDeletion(changesetId: Long, element: OsmElement) {
        try {
            element.isDeleted = true
            mapDataApi.uploadChanges(changesetId, listOf(element), null)
        } catch (e: OsmApiException) {
            // element can't be deleted because it is f.e. a node in a way. So, we'll just degrade
            // it to a vertex then (clear the tags)
            if (e.errorCode == HttpURLConnection.HTTP_PRECON_FAILED) {
                element.isDeleted = false
                element.tags.clear()
                mapDataApi.uploadChanges(changesetId, listOf(element), null)
            } else throw e
        }
    }
}

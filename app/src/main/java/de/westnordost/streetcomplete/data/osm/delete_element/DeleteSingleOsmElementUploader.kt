package de.westnordost.streetcomplete.data.osm.delete_element

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import javax.inject.Inject

class DeleteSingleOsmElementUploader @Inject constructor(private val mapDataApi: MapDataApi) {

    fun upload(changesetId: Long, element: OsmElement) {
        element.isDeleted = true
        try {
            mapDataApi.uploadChanges(changesetId, listOf(element), null)
        } catch (e: OsmConflictException) {
            throw ElementConflictException(e.message, e)
        } catch (e: OsmNotFoundException) {
            throw ElementDeletedException(e.message, e)
        }
    }
}

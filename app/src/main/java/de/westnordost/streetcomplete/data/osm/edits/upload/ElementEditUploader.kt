package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.ElementUpdates
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.uploadChanges
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ApiMapDataRepository
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.upload.ConflictException
import javax.inject.Inject

class ElementEditUploader @Inject constructor(
    private val changesetManager: OpenQuestChangesetsManager,
    private val mapDataApi: MapDataApi
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ConflictException if element has been changed server-side in an incompatible way
     *  */
    fun upload(edit: ElementEdit, idProvider: ElementIdProvider): ElementUpdates {
        val element = edit.fetchElement() ?: throw ConflictException()

        val repos = ApiMapDataRepository(mapDataApi)
        val uploadElements = edit.action.createUpdates(element, repos, idProvider)

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(edit.questType, edit.source)
            uploadInChangeset(changesetId, uploadElements)
        } catch (e: ConflictException) {
            val changesetId = changesetManager.createChangeset(edit.questType, edit.source)
            uploadInChangeset(changesetId, uploadElements)
        }
    }

    /** Upload the changes for a single change. Returns the updated element(s). */
    private fun uploadInChangeset(changesetId: Long, elements: Collection<Element>): ElementUpdates {
        try {
            return mapDataApi.uploadChanges(changesetId, elements)
        } catch (e: OsmConflictException) {
            throw ConflictException(e.message, e)
        } catch (e: OsmApiException) {
            throw ConflictException(e.message, e)
        }
    }

    private fun ElementEdit.fetchElement() = when (elementType) {
        Element.Type.NODE     -> mapDataApi.getNode(elementId)
        Element.Type.WAY      -> mapDataApi.getWay(elementId)
        Element.Type.RELATION -> mapDataApi.getRelation(elementId)
    }
}

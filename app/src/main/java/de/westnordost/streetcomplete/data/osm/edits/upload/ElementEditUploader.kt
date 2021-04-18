package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.*
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
    fun upload(edit: ElementEdit, idProvider: ElementIdProvider): MapDataUpdates {
        val element = edit.fetchElement() ?: throw ConflictException()

        val uploadElements = edit.action.createUpdates(element, mapDataApi, idProvider)

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(edit.questType, edit.source)
            uploadInChangeset(changesetId, uploadElements)
        } catch (e: ConflictException) {
            val changesetId = changesetManager.createChangeset(edit.questType, edit.source)
            uploadInChangeset(changesetId, uploadElements)
        }
    }

    /** Upload the changes for a single change. Returns the updated element(s). */
    private fun uploadInChangeset(changesetId: Long, elements: Collection<Element>): MapDataUpdates {
        try {
            return mapDataApi.uploadChanges(changesetId, elements)
        } catch (e: OsmConflictException) {
            throw ConflictException(e.message, e)
        } catch (e: OsmApiException) {
            throw ConflictException(e.message, e)
        }
    }

    private fun ElementEdit.fetchElement() = when (elementType) {
        ElementType.NODE     -> mapDataApi.getNode(elementId)
        ElementType.WAY      -> mapDataApi.getWay(elementId)
        ElementType.RELATION -> mapDataApi.getRelation(elementId)
    }
}

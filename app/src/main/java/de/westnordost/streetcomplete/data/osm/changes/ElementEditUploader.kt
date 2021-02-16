package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.ElementUpdates
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.uploadChanges
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.mapdata.ApiMapDataRepository
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import javax.inject.Inject

class ElementEditUploader @Inject constructor(
    private val changesetManager: OpenQuestChangesetsManager,
    private val mapDataApi: MapDataApi
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ElementConflictException if element has been changed server-side in an incompatible way
     *  @throws ElementDeletedException if element has been deleted server-side
     *  */
    fun upload(edit: ElementEdit, idProvider: ElementIdProvider): ElementUpdates {
        val element = edit.fetchElement() ?: throw ElementDeletedException()

        val repos = ApiMapDataRepository(mapDataApi)
        val uploadElements = edit.action.createUpdates(element, repos, idProvider)

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(edit.questType, edit.source)
            uploadInChangeset(changesetId, uploadElements)
        } catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(edit.questType, edit.source)
            uploadInChangeset(changesetId, uploadElements)
        }
    }

    /** Upload the changes for a single change. Returns the updated element(s).
     *  Throws  */
    private fun uploadInChangeset(changesetId: Long, elements: Collection<Element>): ElementUpdates {
        try {
            return mapDataApi.uploadChanges(changesetId, elements)
        } catch (e: OsmConflictException) {
            throw ChangesetConflictException(e.message, e)
        } catch (e: OsmApiException) {
            throw ElementConflictException(e.message, e)
        }
    }

    private fun ElementEdit.fetchElement() = when (elementType) {
        Element.Type.NODE     -> mapDataApi.getNode(elementId)
        Element.Type.WAY      -> mapDataApi.getWay(elementId)
        Element.Type.RELATION -> mapDataApi.getRelation(elementId)
    }
}

package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.ApplicationConstants.EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.copy

class ElementEditUploader(
    private val changesetManager: OpenChangesetsManager,
    private val mapDataApi: MapDataApi,
    private val mapDataController: MapDataController,
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ConflictException if element has been changed server-side in an incompatible way
     *  */
    fun upload(edit: ElementEdit, idProvider: ElementIdProvider): MapDataUpdates {
        val remoteChanges by lazy { edit.action.createUpdates(edit.originalElement, edit.fetchElement(mapDataApi), mapDataApi, idProvider) }
        val localChanges by lazy { edit.action.createUpdates(edit.originalElement, edit.fetchElement(mapDataController), mapDataController, idProvider) }
        val useRemoteChanges = edit.action::class in EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES

        return try {
            if (useRemoteChanges)
                uploadChanges(edit, remoteChanges, false)
            else
                uploadChanges(edit, localChanges, false)
        } catch (e: ConflictException) {
            // either changeset was closed, or element modified, or local element was cleaned from db
            if (useRemoteChanges) {
                // probably changeset closed
                uploadChanges(edit, remoteChanges, true)
            } else {
                // anything of the 3 may be the problem, try again with remote changes
                try {
                    uploadChanges(edit, remoteChanges, false)
                } catch (e: ConflictException) {
                    // probably changeset closed
                    uploadChanges(edit, remoteChanges, true)
                }
            }
        }
    }

    private fun uploadChanges(edit: ElementEdit, mapDataChanges: MapDataChanges, newChangeset: Boolean): MapDataUpdates {
        val changesetId = if (newChangeset) changesetManager.createChangeset(edit.type, edit.source)
            else changesetManager.getOrCreateChangeset(edit.type, edit.source)
        return mapDataApi.uploadChanges(changesetId, mapDataChanges)
    }

    private fun ElementEdit.fetchElement(mapDataRepository: MapDataRepository) = when (elementType) {
        ElementType.NODE     -> mapDataRepository.getNode(elementId)
        ElementType.WAY      -> mapDataRepository.getWay(elementId)
        ElementType.RELATION -> mapDataRepository.getRelation(elementId)
    }
}

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

        return if (edit.action::class in EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES) {
            try {
                uploadChanges(edit, remoteChanges, false)
            } catch (e: ConflictException) {
                // probably changeset closed
                uploadChanges(edit, remoteChanges, true)
            }
        } else {
            try {
                uploadChanges(edit, localChanges, false)
            } catch (e: ConflictException) {
                // either changeset was closed, or element modified, or local element was cleaned from db
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

    private fun MapDataRepository.get(elementType: ElementType, elementId: Long) = when (elementType) {
        ElementType.NODE     -> getNode(elementId)
        ElementType.WAY      -> getWay(elementId)
        ElementType.RELATION -> getRelation(elementId)
    }
}

package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.copy

class ElementEditUploader(
    private val changesetManager: OpenChangesetsManager,
    private val mapDataApi: MapDataApi
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ConflictException if element has been changed server-side in an incompatible way
     *  */
    fun upload(edit: ElementEdit, idProvider: ElementIdProvider, localElement: Element?): MapDataUpdates {
        val remoteElement by lazy { edit.fetchElement() }
        val changesOnRemoteElement by lazy { edit.action.createUpdates(edit.originalElement, remoteElement, mapDataApi, idProvider) }
        val changesOnLocalElement by lazy { edit.action.createUpdates(edit.originalElement, localElement, mapDataApi, idProvider) }

        return try {
            uploadChanges(edit, changesOnLocalElement, false)
        } catch (e: ConflictException) {
            // either changeset was closed, or element modified, or local element was cleaned from db
            // compare remote and local elements, but consider that the timestamps may differ
            if (remoteElement?.copy(timestampEdited = 0) == localElement?.copy(timestampEdited = 0)) {
                // element unchanged -> probably changeset was closed
                uploadChanges(edit, changesOnLocalElement, true)
            } else {
                // element changed -> create changes from remote element and try again
                try {
                    uploadChanges(edit, changesOnRemoteElement, false)
                } catch (e: ConflictException) {
                    // probably the changeset was closed -> upload again
                    uploadChanges(edit, changesOnRemoteElement, true)
                }
            }
        }
    }

    private fun uploadChanges(edit: ElementEdit, mapDataChanges: MapDataChanges, newChangeset: Boolean): MapDataUpdates {
        val changesetId = if (newChangeset) changesetManager.createChangeset(edit.type, edit.source)
            else changesetManager.getOrCreateChangeset(edit.type, edit.source)
        return mapDataApi.uploadChanges(changesetId, mapDataChanges)
    }

    private fun ElementEdit.fetchElement() = when (elementType) {
        ElementType.NODE     -> mapDataApi.getNode(elementId)
        ElementType.WAY      -> mapDataApi.getWay(elementId)
        ElementType.RELATION -> mapDataApi.getRelation(elementId)
    }
}

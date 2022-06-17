package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.upload.ConflictException

class ElementEditUploader(
    private val changesetManager: OpenChangesetsManager,
    private val mapDataApi: MapDataApi
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ConflictException if element has been changed server-side in an incompatible way
     *  */
    fun upload(edit: ElementEdit, idProvider: ElementIdProvider): MapDataUpdates {
        val element = edit.fetchElement()

        val mapDataChanges = edit.action.createUpdates(edit.originalElement, element, mapDataApi, idProvider)

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(edit.type, edit.source)
            mapDataApi.uploadChanges(changesetId, mapDataChanges)
        } catch (e: ConflictException) {
            val changesetId = changesetManager.createChangeset(edit.type, edit.source)
            mapDataApi.uploadChanges(changesetId, mapDataChanges)
        }
    }

    private fun ElementEdit.fetchElement() = when (elementType) {
        ElementType.NODE     -> mapDataApi.getNode(elementId)
        ElementType.WAY      -> mapDataApi.getWay(elementId)
        ElementType.RELATION -> mapDataApi.getRelation(elementId)
    }
}

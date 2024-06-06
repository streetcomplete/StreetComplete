package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates

class ElementEditUploader(
    private val changesetManager: OpenChangesetsManager,
    private val mapDataApi: MapDataApi,
    private val mapDataController: MapDataController
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ConflictException if element has been changed server-side in an incompatible way
     *  */
    fun upload(edit: ElementEdit, getIdProvider: () -> ElementIdProvider): MapDataUpdates {
        val remoteChanges by lazy { edit.action.createUpdates(mapDataApi, getIdProvider()) }
        val localChanges by lazy { edit.action.createUpdates(mapDataController, getIdProvider()) }

        val mustUseRemoteData = edit.action::class in ApplicationConstants.EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES

        return if (mustUseRemoteData) {
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
        val changesetId =
            if (newChangeset) {
                changesetManager.createChangeset(edit.type, edit.source, edit.position)
            } else {
                changesetManager.getOrCreateChangeset(edit.type, edit.source, edit.position, edit.isNearUserLocation)
            }
        return mapDataApi.uploadChanges(changesetId, mapDataChanges, ApplicationConstants.IGNORED_RELATION_TYPES)
    }
}

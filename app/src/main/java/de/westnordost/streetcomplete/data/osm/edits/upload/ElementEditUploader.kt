package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.RemoteMapDataRepository

class ElementEditUploader(
    private val changesetManager: OpenChangesetsManager,
    private val mapDataApi: MapDataApiClient,
    private val mapDataController: MapDataController
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ConflictException if element has been changed server-side in an incompatible way
     */
    suspend fun upload(edit: ElementEdit, getIdProvider: () -> ElementIdProvider): MapDataUpdates {
        val remoteChanges by lazy { edit.action.createUpdates(RemoteMapDataRepository(mapDataApi), getIdProvider()) }
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

    private suspend fun uploadChanges(
        edit: ElementEdit,
        changes: MapDataChanges,
        newChangeset: Boolean
    ): MapDataUpdates {
        val changesetId = if (newChangeset) {
            changesetManager.createChangeset(edit.type, edit.source, edit.position)
        } else {
            changesetManager.getOrCreateChangeset(edit.type, edit.source, edit.position, edit.isNearUserLocation)
        }
        return mapDataApi.uploadChanges(changesetId, changes, ApplicationConstants.IGNORED_RELATION_TYPES)
    }
}

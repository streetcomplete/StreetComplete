package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.ApplicationConstants.EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES
import de.westnordost.streetcomplete.ApplicationConstants.IGNORED_RELATION_TYPES
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.ChangesetTooLargeException
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
        // build changes on top of map data cached in the local database (faster)
        val localChanges by lazy { edit.action.createUpdates(mapDataController, getIdProvider()) }

        // build changes on top of map data downloaded ad-hoc from remote (additional HTTP requests -> slower)
        val remoteChanges by lazy { edit.action.createUpdates(RemoteMapDataRepository(mapDataApi), getIdProvider()) }

        // certain edit types don't allow building changes on top of cached map data
        val mustUseRemoteData = edit.action::class in EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES

        if (!mustUseRemoteData) {
            try {
                return uploadChanges(edit, localChanges, false)
            }
            // either changeset was closed, element modified on remote, element not available in DB
            catch (e: ConflictException) {
                // continue execution outside of this if-block (-> try with remote data)
            }
            // changeset too large -> try again with new changeset
            catch (e: ChangesetTooLargeException) {
                try {
                    return uploadChanges(edit, localChanges, true)
                }
                // we have a new changeset already -> one last try with using remote data
                catch (e: ConflictException) {
                    return uploadChanges(edit, remoteChanges, false)
                }
            }
        }

        try {
            return uploadChanges(edit, remoteChanges, false)
        }
        // probably changeset closed -> try again with new changeset
        catch (e: ConflictException) {
            return uploadChanges(edit, remoteChanges, true)
        }
        // changeset too large -> try again with new changeset
        catch (e: ChangesetTooLargeException) {
            return uploadChanges(edit, remoteChanges, true)
        }

        // Finally, if an uncaught ConflictException is thrown, it means this is because the changes
        // of this edit conflict with the current version of the element (on remote) it targets, as
        // we excluded all the other reasons why a ConflictException might be thrown.
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
        return mapDataApi.uploadChanges(changesetId, changes, IGNORED_RELATION_TYPES)
    }
}

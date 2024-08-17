package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.RemoteMapDataRepository
import de.westnordost.streetcomplete.data.user.UserLoginController
import de.westnordost.streetcomplete.util.ktx.copy

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

        // fake upload in debug mode: create pseudo-random new (positive!) ids that are unlikely to clash with real ids
        if (BuildConfig.DEBUG && !UserLoginController.loggedIn) {
            val creationsByNewId = localChanges.creations.associateBy { Long.MAX_VALUE - Int.MAX_VALUE + it.hashCode() }
            val updates = MapDataUpdates(
                updated = (localChanges.modifications + creationsByNewId.map { it.value.copy(id = it.key) })
                    .map { element ->
                    // need to update node ids of ways, don't care about relations
                    if (element is Way && element.nodeIds.any { it < 0 }) {
                        val newNodeIds = element.nodeIds.map { id ->
                            if (id > 0) id
                            else
                                creationsByNewId.entries.first { it.value.id == id }.key
                        }
                        element.copy(nodeIds = newNodeIds)
                    } else element
                },
                deleted = localChanges.deletions.map { it.key },
                idUpdates = creationsByNewId.map { ElementIdUpdate(it.value.type, it.value.id, it.key) }
            )
            return updates
        }

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

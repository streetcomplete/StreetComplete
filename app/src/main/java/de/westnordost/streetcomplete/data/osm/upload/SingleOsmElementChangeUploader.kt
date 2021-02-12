package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import javax.inject.Inject

class SingleOsmElementChangeUploader @Inject constructor(
    private val changesetManager: OpenQuestChangesetsManager,
    private val singleOsmElementTagChangesUploader: SingleOsmElementTagChangesUploader,
    private val splitSingleOsmWayUploader: SplitSingleOsmWayUploader,
    private val deleteSingleOsmElementUploader: DeleteSingleOsmElementUploader
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ElementConflictException if element has been changed server-side in an incompatible way
     *  @throws ElementDeletedException if element has been deleted server-side */
    fun upload(
        change: OsmElementChange,
        element: Element,
        idProvider: NewOsmElementIdProvider?
    ): ElementUpdates {
        return try {
            val changesetId = changesetManager.getOrCreateChangeset(change.questType, change.source)
            uploadInChangeset(changesetId, change, element, idProvider)
        } catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(change.questType, change.source)
            uploadInChangeset(changesetId, change, element, idProvider)
        }
    }

    /** Upload the changes for a single change. Returns the updated element(s) */
    private fun uploadInChangeset(
        changesetId: Long,
        change: OsmElementChange,
        element: Element,
        idProvider: NewOsmElementIdProvider?
    ): ElementUpdates {
        return when(change) {
            is ChangeOsmElementTags ->
                singleOsmElementTagChangesUploader.upload(changesetId, change, element)
            is DeleteOsmElement ->
                deleteSingleOsmElementUploader.upload(changesetId, element)
            is RevertChangeOsmElementTags ->
                singleOsmElementTagChangesUploader.upload(changesetId, change, element)
            is SplitOsmWay ->
                splitSingleOsmWayUploader.upload(changesetId, element as Way, change.splits, idProvider)
        }
    }
}

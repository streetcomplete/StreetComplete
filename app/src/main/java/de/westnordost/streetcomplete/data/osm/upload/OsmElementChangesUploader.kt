package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater

import java.util.concurrent.atomic.AtomicBoolean

abstract class OsmElementChangesUploader(
    private val changesetManager: OpenQuestChangesetsManager,
    private val osmElementChangesController: OsmElementChangesController,
    private val osmElementController: OsmElementController,
    private val singleOsmElementTagChangesUploader: SingleOsmElementTagChangesUploader,
    private val splitSingleOsmWayUploader: SplitSingleOsmWayUploader,
    private val deleteSingleOsmElementUploader: DeleteSingleOsmElementUploader,
    // TODO all this necessary? Couldn't there be listeners?
    private val statisticsUpdater: StatisticsUpdater
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        while (true) {
            if (cancelled.get()) break
            val change = osmElementChangesController.getOldestUnsynced() ?: break

            try {
                val elementUpdates = uploadSingle(change)
                onUploadSuccessful(change)
                // TODO really put all?
                osmElementController.putAll(elementUpdates.updated)
            } catch (e: ElementIncompatibleException) {
                // TODO what, delete??
                osmElementController.deleteAll(listOf(ElementKey(change.elementType, change.elementId)))
                onUploadFailed(change, e)
            } catch (e: ElementConflictException) {
                onUploadFailed(change, e)
            }
        }
    }

    private fun uploadSingle(change: OsmElementChange): ElementUpdates {
        // TODO remove this??
        val element = osmElementController.get(change.elementType, change.elementId)
            ?: throw ElementDeletedException("Element deleted")

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(change.questType, change.source)
            uploadSingle(changesetId, change, element)
        } catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(change.questType, change.source)
            uploadSingle(changesetId, change, element)
        }
    }

    /** Upload the changes for a single change. Returns the updated element(s) */
    private fun uploadSingle(changesetId: Long, change: OsmElementChange, element: Element): ElementUpdates {
        return when(change) {
            is ChangeOsmElementTags ->
                singleOsmElementTagChangesUploader.upload(changesetId, change, element)
            is DeleteOsmElement ->
                deleteSingleOsmElementUploader.upload(changesetId, element)
            is RevertChangeOsmElementTags ->
                singleOsmElementTagChangesUploader.upload(changesetId, change, element)
            is SplitOsmWay ->
                splitSingleOsmWayUploader.upload(changesetId, element as Way, change.splits)
        }
    }

    private fun onUploadSuccessful(change: OsmElementChange) {
        osmElementChangesController.markSynced(change.id!!)

        val questName = change.questType::class.simpleName!!
        if (change is IsRevert) {
            statisticsUpdater.subtractOne(questName, change.position)
        } else {
            statisticsUpdater.addOne(questName, change.position)
        }

        Log.d(TAG, "Uploaded a ${change::class.simpleName}")

        uploadedChangeListener?.onUploaded(change.questType.name, change.position)
    }

    private fun onUploadFailed(change: OsmElementChange, e: Throwable) {
        osmElementChangesController.delete(change)

        Log.d(TAG, "Dropped a ${change::class.simpleName}: ${e.message}")

        uploadedChangeListener?.onDiscarded(change.questType.name, change.position)
    }

    companion object {
        private const val TAG = "Upload"
    }
}

private val QuestType<*>.name get() = javaClass.simpleName

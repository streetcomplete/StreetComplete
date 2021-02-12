package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class OsmElementChangesUploader @Inject constructor(
    private val osmElementChangesController: OsmElementChangesController,
    private val osmElementController: OsmElementController,
    private val singleUploader: SingleOsmElementChangeUploader,
    // TODO all this necessary? Couldn't there be listeners?
    private val statisticsUpdater: StatisticsUpdater
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        while (true) {
            if (cancelled.get()) break
            val change = osmElementChangesController.getOldestUnsynced() ?: break

            try {
                val element = osmElementController.get(change.elementType, change.elementId) ?:
                    throw ElementDeletedException()
                val elementUpdates = singleUploader.upload(change, element)
                onUploadSuccessful(change)
                // TODO really put all?
                osmElementController.putAll(elementUpdates.updated)
            } catch (e: ElementDeletedException) {
                // TODO what, delete??
                osmElementController.deleteAll(listOf(ElementKey(change.elementType, change.elementId)))
                onUploadFailed(change, e)
            } catch (e: ElementConflictException) {
                onUploadFailed(change, e)
            }
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

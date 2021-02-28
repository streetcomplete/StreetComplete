package de.westnordost.streetcomplete.data.osm.edits.upload

import android.util.Log
import de.westnordost.streetcomplete.data.osm.edits.*
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ElementEditsUploader @Inject constructor(
    private val elementEditsController: ElementEditsController,
    private val mapDataController: MapDataController,
    private val singleUploader: ElementEditUploader,
    private val statisticsUpdater: StatisticsUpdater
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        while (true) {
            if (cancelled.get()) break
            val edit = elementEditsController.getOldestUnsynced() ?: break
            val idProvider = elementEditsController.getIdProvider(edit.id!!)
            uploadEdit(edit, idProvider)
        }
    }

    private fun uploadEdit(edit: ElementEdit, idProvider: ElementIdProvider) {
        val questTypeName = edit.questType::class.simpleName!!
        val editActionClassName = edit.action::class.simpleName!!

        try {
            val updates = singleUploader.upload(edit, idProvider)

            Log.d(TAG, "Uploaded a $editActionClassName")
            uploadedChangeListener?.onUploaded(questTypeName, edit.position)

            elementEditsController.synced(edit, updates)
            mapDataController.updateAll(updates)

            if (edit.action is IsRevertAction) {
                statisticsUpdater.subtractOne(questTypeName, edit.position)
            } else {
                statisticsUpdater.addOne(questTypeName, edit.position)
            }

        } catch (e: ElementConflictException) {
            Log.d(TAG, "Dropped a $editActionClassName: ${e.message}")
            uploadedChangeListener?.onDiscarded(questTypeName, edit.position)

            // TODO update the lement in question?!

            elementEditsController.syncFailed(edit)
        }
    }

    companion object {
        private const val TAG = "ElementEditsUploader"
    }
}

package de.westnordost.streetcomplete.data.osm.edits.upload

import android.util.Log
import de.westnordost.osmapi.map.*
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.edits.*
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ElementEditsUploader @Inject constructor(
    private val elementEditsController: ElementEditsController,
    private val mapDataController: MapDataController,
    private val singleUploader: ElementEditUploader,
    private val mapDataApi: MapDataApi,
    private val statisticsUpdater: StatisticsUpdater
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        while (true) {
            if (cancelled.get()) break
            val edit = elementEditsController.getOldestUnsynced() ?: break
            val idProvider = elementEditsController.getIdProvider(edit.id)
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

        } catch (e: ConflictException) {
            Log.d(TAG, "Dropped a $editActionClassName: ${e.message}")
            uploadedChangeListener?.onDiscarded(questTypeName, edit.position)

            elementEditsController.syncFailed(edit)

            val mapData = fetchElementComplete(edit.elementType, edit.elementId)
            if (mapData != null) {
                mapDataController.updateAll(ElementUpdates(updated = mapData.toList()))
            } else {
                val elementKey = ElementKey(edit.elementType, edit.elementId)
                mapDataController.updateAll(ElementUpdates(deleted = listOf(elementKey)))
            }
        }
    }

    private fun fetchElementComplete(elementType: Element.Type, elementId: Long): MapData? =
        when(elementType) {
            NODE -> mapDataApi.getNode(elementId)?.let { MutableMapData(listOf(it)) }
            WAY -> mapDataApi.getWayComplete(elementId)
            RELATION -> mapDataApi.getRelationComplete(elementId)
        }

    companion object {
        private const val TAG = "ElementEditsUploader"
    }
}

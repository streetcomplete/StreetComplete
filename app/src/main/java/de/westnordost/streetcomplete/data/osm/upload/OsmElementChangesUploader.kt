package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class OsmElementChangesUploader @Inject constructor(
    private val osmElementChangesController: OsmElementChangesController
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        while (osmElementChangesController.getUnsyncedChangesCount() > 0) {
            if (cancelled.get()) break
            val result = osmElementChangesController.syncOldestChange() ?: break

            val questTypeName = result.change.questType::class.simpleName!!
            val pos = result.change.position

            if (result.success)
                uploadedChangeListener?.onUploaded(questTypeName, pos)
            else
                uploadedChangeListener?.onDiscarded(questTypeName, pos)
        }
    }
}

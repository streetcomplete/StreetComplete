package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ElementEditsUploader @Inject constructor(
    private val elementEditsController: ElementEditsController
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        while (elementEditsController.getUnsyncedEditsCount() > 0) {
            if (cancelled.get()) break
            val result = elementEditsController.syncOldestEdit() ?: break

            val questTypeName = result.edit.questType::class.simpleName!!
            val pos = result.edit.position

            if (result.success)
                uploadedChangeListener?.onUploaded(questTypeName, pos)
            else
                uploadedChangeListener?.onDiscarded(questTypeName, pos)
        }
    }
}

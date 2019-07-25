package de.westnordost.streetcomplete.data.osm.upload

import android.os.CancellationSignal
import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.persist.SplitWayDao
import de.westnordost.streetcomplete.data.osm.persist.WayDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import javax.inject.Inject

/** Gets all split ways from local DB and uploads them via the OSM API */
class SplitWaysUpload @Inject constructor(
    private val splitWayDB: SplitWayDao,
    private val wayDao: WayDao,
    private val changesetManager: OpenQuestChangesetsManager,
    private val splitSingleOsmWayUpload: SplitSingleWayUpload
) {
    private val TAG = "SplitOsmWayUpload"

    var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized fun upload(signal: CancellationSignal) {
        if (signal.isCanceled) return
        Log.i(TAG, "Splitting ways")
        for (quest in splitWayDB.getAll()) {
            if (signal.isCanceled) break

            try {
                uploadSingle(quest)
                Log.d(TAG, "Uploaded split way #${quest.wayId}")
                uploadedChangeListener?.onUploaded()
            } catch (e: ElementConflictException) {
                Log.d(TAG, "Dropped split for way #${quest.wayId}: ${e.message}")
                uploadedChangeListener?.onDiscarded()
            }

            splitWayDB.delete(quest.id)
        }
    }

    private fun uploadSingle(quest: OsmQuestSplitWay): List<Element> {
        val way = wayDao.get(quest.wayId) ?: throw ElementDeletedException("Way deleted")

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(quest.questType, quest.source)
            splitSingleOsmWayUpload.upload(changesetId, way, quest.splits)
        } catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(quest.questType, quest.source)
            splitSingleOsmWayUpload.upload(changesetId, way, quest.splits)
        }
    }
}

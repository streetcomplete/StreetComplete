package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.SplitWayDao
import de.westnordost.streetcomplete.data.osm.persist.WayDao
import javax.inject.Inject

class SplitOsmWaysUpload @Inject constructor(
    private val splitWaysDao: SplitWaysDao,
    private val wayDao: WayDao,
    private val splitWayDao: SplitWayDao,
    private val splitSingleOsmWayUpload: SplitSingleOsmWayUpload
) {
    private val TAG = "SplitOsmWayUpload"

    fun upload(changesetsUpload: OsmQuestChangesetsUpload) {
        Log.i(TAG, "Splitting ways")
        changesetsUpload.upload(splitWaysDao.getAll(), this::uploadSingle)
    }

    private fun uploadSingle(changesetId: Long, quest: OsmQuest): List<Element> {
        val way = wayDao.get(quest.elementId)
        val splits = splitWayDao.get(quest.elementId)
        try {
            return splitSingleOsmWayUpload.upload(changesetId, way, splits)
        } catch (e: ElementConflictException) {
            Log.d(TAG, "Dropping split for way #${quest.elementId}: ${e.message}")
            throw e
        } finally {
            splitWaysDao.delete()
        }
    }
}

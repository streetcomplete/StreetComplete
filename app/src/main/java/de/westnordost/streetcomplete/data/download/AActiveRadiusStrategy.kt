package de.westnordost.streetcomplete.data.download

import android.util.Log

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.util.area
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import de.westnordost.streetcomplete.util.enclosingTilesRect
import kotlin.math.max

/** Quest auto download strategy that observes that a minimum amount of quests in a predefined
 * radius around the user is not undercut  */
abstract class AActiveRadiusStrategy(
    private val visibleQuestsSource: VisibleQuestsSource,
    private val downloadedTilesDao: DownloadedTilesDao,
    private val questTypesProvider: OrderedVisibleQuestTypesProvider
) : QuestAutoDownloadStrategy {

    protected abstract val minQuestsInActiveRadiusPerKm2: Int
    protected abstract val activeRadii: IntArray
    protected abstract val downloadRadius: Int

    private fun mayDownloadHere(pos: LatLon, radius: Int, questTypeNames: List<String>): Boolean {
        val bbox = pos.enclosingBoundingBox(radius.toDouble())

        // nothing more to download
        val tiles = bbox.enclosingTilesRect(ApplicationConstants.QUEST_TILE_ZOOM)
        val questExpirationTime = ApplicationConstants.REFRESH_QUESTS_AFTER
        val ignoreOlderThan = max(0, System.currentTimeMillis() - questExpirationTime)
        val alreadyDownloaded = downloadedTilesDao.get(tiles, ignoreOlderThan).toSet()
        val notAlreadyDownloaded = mutableListOf<String>()
        for (questTypeName in questTypeNames) {
            if (!alreadyDownloaded.contains(questTypeName)) notAlreadyDownloaded.add(questTypeName)
        }

        if (notAlreadyDownloaded.isEmpty()) {
            Log.i(TAG, "Not downloading quests because everything has been downloaded already in ${radius}m radius")
            return false
        }

        if (alreadyDownloaded.isNotEmpty()) {
            val areaInKm2 = bbox.area() / 1000.0 / 1000.0
            // got enough quests in vicinity
            val visibleQuests = visibleQuestsSource.getAllVisibleCount(bbox, alreadyDownloaded)
            if (visibleQuests / areaInKm2 > minQuestsInActiveRadiusPerKm2) {
                Log.i(TAG, "Not downloading quests because there are enough quests in ${radius}m radius")
                return false
            }
        }

        return true
    }

    override fun mayDownloadHere(pos: LatLon): Boolean {
        val questTypeNames = questTypesProvider.get().map { it.javaClass.simpleName }
        return activeRadii.any { radius ->
            mayDownloadHere(pos, radius, questTypeNames)
        }
    }

    override fun getDownloadBoundingBox(pos: LatLon): BoundingBox {
        return pos.enclosingBoundingBox(downloadRadius.toDouble())
    }

    companion object {
        private const val TAG = "AutoQuestDownload"
    }
}

package de.westnordost.streetcomplete.data.download

import android.util.Log

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesType
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.util.*
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Quest auto download strategy decides how big of an area to download based on the quest density */
abstract class AVariableRadiusStrategy(
    private val visibleQuestsSource: VisibleQuestsSource,
    private val downloadedTilesDao: DownloadedTilesDao
) : QuestAutoDownloadStrategy {

    protected abstract val maxDownloadAreaInKm2: Double
    protected abstract val desiredQuestCountInVicinity: Int

    override fun getDownloadBoundingBox(pos: LatLon): BoundingBox? {
        val tileZoom = ApplicationConstants.QUEST_TILE_ZOOM

        val thisTile = pos.enclosingTile(tileZoom)
        val hasMissingQuestsForThisTile = hasMissingQuestsFor(thisTile.toTilesRect())

        // if at the location where we are, there is nothing yet, first download the tiniest
        // possible bbox (~ 360x360m) so that we can estimate the quest density
        if (hasMissingQuestsForThisTile) {
            Log.i(TAG, "Downloading tiny area around user")
            return thisTile.asBoundingBox(tileZoom)
        }

        // otherwise, see if anything is missing in a variable radius, based on quest density
        val density = getQuestDensityFor(thisTile.asBoundingBox(tileZoom))
        val maxRadius = sqrt( maxDownloadAreaInKm2 * 1000 * 1000 / PI )

        var radius = if (density > 0) sqrt( desiredQuestCountInVicinity / ( PI * density )) else maxRadius

        radius = min( radius, maxRadius)

        val activeBoundingBox = pos.enclosingBoundingBox(radius)
        if (hasMissingQuestsFor(activeBoundingBox.enclosingTilesRect(tileZoom))) {
            Log.i(TAG, "Downloading in radius of ${radius.toInt()} meters around user")
            return activeBoundingBox
        }
        Log.i(TAG, "All downloaded in radius of ${radius.toInt()} meters around user")
        return null
    }

    /** return the quest density in quests per m² for this given [boundingBox]*/
    private fun getQuestDensityFor(boundingBox: BoundingBox): Double {
        val areaInKm = boundingBox.area()
        val visibleQuestCount = visibleQuestsSource.getAllVisibleCount(boundingBox)
        return visibleQuestCount / areaInKm
    }

    /** return if there are any quests in the given tiles rect that haven't been downloaded yet */
    private fun hasMissingQuestsFor(tilesRect: TilesRect): Boolean {
        val questExpirationTime = ApplicationConstants.REFRESH_QUESTS_AFTER
        val ignoreOlderThan = max(0, System.currentTimeMillis() - questExpirationTime)
        return !downloadedTilesDao.get(tilesRect, ignoreOlderThan).contains(DownloadedTilesType.QUESTS)
    }

    companion object {
        private const val TAG = "AutoQuestDownload"
    }
}

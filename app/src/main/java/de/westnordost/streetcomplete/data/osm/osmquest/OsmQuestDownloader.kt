package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log

import java.util.concurrent.FutureTask

import javax.inject.Inject

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.intersects
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osmnotes.NotePositionsSource
import java.util.*
import kotlin.collections.ArrayList

/** Takes care of downloading one quest type in a bounding box and persisting the downloaded quests.
 *  Calls download(bbox) on the quest type */
class OsmQuestDownloader @Inject constructor(
    private val elementDB: MergedElementDao,
    private val osmQuestController: OsmQuestController,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val notePositionsSource: NotePositionsSource,
    private val elementEligibleForOsmQuestChecker: ElementEligibleForOsmQuestChecker
) {
    private val countryBoundaries: CountryBoundaries get() = countryBoundariesFuture.get()

    fun download(questType: OsmElementQuestType<*>, bbox: BoundingBox): Boolean {
        val questTypeName = questType.getName()

        val countries = questType.enabledInCountries
        if (!countryBoundaries.intersects(bbox, countries)) {
            Log.i(TAG, "$questTypeName: Skipped because it is disabled for this country")
            return true
        }

        val elements = mutableListOf<Element>()
        val quests = ArrayList<OsmQuest>()
        val truncatedBlacklistedPositions = notePositionsSource.getAllPositions(bbox).map { it.truncateTo5Decimals() }.toSet()

        val time = System.currentTimeMillis()
        val success = questType.download(bbox) { element, geometry ->
            if (elementEligibleForOsmQuestChecker.mayCreateQuestFrom(questType, element, geometry, truncatedBlacklistedPositions)) {
                val quest = OsmQuest(questType, element.type, element.id, geometry!!)

                quests.add(quest)
                elements.add(element)
            }
        }
        if (!success) return false

        // elements must be put into DB first because quests have foreign keys on it
        elementDB.putAll(elements)

        val replaceResult = osmQuestController.replaceInBBox(quests, bbox, listOf(questTypeName))

        // note: this could be done after ALL osm quest types have been downloaded if this
        // turns out to be slow if done for every quest type
        elementDB.deleteUnreferenced()
        questType.cleanMetadata()

        val secondsSpent = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"$questTypeName: Added ${replaceResult.added} new and removed ${replaceResult.deleted} already resolved quests (total: ${quests.size}) in ${secondsSpent}s")

        return true
    }



    companion object {
        private const val TAG = "QuestDownload"
    }
}

private fun QuestType<*>.getName() = javaClass.simpleName

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5

private fun Element.toLogString() = "${type.name.toLowerCase(Locale.US)} #$id"

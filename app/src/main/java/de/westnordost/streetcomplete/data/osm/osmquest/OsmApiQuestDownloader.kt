package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.intersects
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osmnotes.NotePositionsSource
import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.*
import java.util.concurrent.FutureTask
import javax.inject.Inject
import javax.inject.Provider
import kotlin.collections.ArrayList

/** Does one API call to get all the map data and generates quests from that. Calls isApplicable
 *  on all the quest types on all elements in the downloaded data. */
class OsmApiQuestDownloader @Inject constructor(
    private val elementDB: MergedElementDao,
    private val osmQuestController: OsmQuestController,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val notePositionsSource: NotePositionsSource,
    private val osmApiMapDataProvider: Provider<OsmApiMapData>,
    private val elementEligibleForOsmQuestChecker: ElementEligibleForOsmQuestChecker
) {
    fun download(questTypes: List<OsmMapDataQuestType<*>>, bbox: BoundingBox) {
        if (questTypes.isEmpty()) return

        var time = System.currentTimeMillis()

        val mapData = osmApiMapDataProvider.get()
        mapData.initWith(bbox)

        val quests = ArrayList<OsmQuest>()
        val questElements = HashSet<Element>()

        val secondsSpentDownloading = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Downloaded ${mapData.nodes.size} nodes, ${mapData.ways.size} ways and ${mapData.relations.size} relations in ${secondsSpentDownloading}s")
        time = System.currentTimeMillis()

        val truncatedBlacklistedPositions = notePositionsSource.getAllPositions(bbox).map { it.truncateTo5Decimals() }.toSet()

        for (questType in questTypes) {
            // TODO multithreading!
            val questTypeName = questType.getName()

            val countries = questType.enabledInCountries
            if (!countryBoundariesFuture.get().intersects(bbox, countries)) {
                Log.i(TAG, "$questTypeName: Skipped because it is disabled for this country")
                continue
            }

            for (element in questType.getApplicableElements(mapData)) {
                val geometry = mapData.getGeometry(element.type, element.id)
                if (!elementEligibleForOsmQuestChecker.mayCreateQuestFrom(questType, geometry, truncatedBlacklistedPositions)) continue

                val quest = OsmQuest(questType, element.type, element.id, geometry!!)

                quests.add(quest)
                questElements.add(element)
            }
        }
        val secondsSpentAnalyzing = (System.currentTimeMillis() - time) / 1000

        // elements must be put into DB first because quests have foreign keys on it
        elementDB.putAll(questElements)

        val questTypeNames = questTypes.map { it.getName() }
        val replaceResult = osmQuestController.replaceInBBox(quests, bbox, questTypeNames)

        elementDB.deleteUnreferenced()

        for (questType in questTypes) {
            questType.cleanMetadata()
        }

        Log.i(TAG,"${questTypeNames.joinToString()}: Added ${replaceResult.added} new and removed ${replaceResult.deleted} already resolved quests (total: ${quests.size}) in ${secondsSpentAnalyzing}s")
    }

    companion object {
        private const val TAG = "QuestDownload"
    }
}

private fun QuestType<*>.getName() = javaClass.simpleName

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5
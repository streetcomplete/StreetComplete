package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.intersects
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.getMapAndHandleTooBigQuery
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osmnotes.NotePositionsSource
import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.*
import java.util.concurrent.FutureTask
import javax.inject.Inject
import kotlin.collections.ArrayList

/** Does one API call to get all the map data and generates quests from that. Calls isApplicable
 *  on all the quest types on all elements in the downloaded data. */
class OsmApiQuestDownloader @Inject constructor(
    private val elementDB: MergedElementDao,
    private val osmQuestController: OsmQuestController,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val notePositionsSource: NotePositionsSource,
    private val mapDataApi: MapDataApi,
    private val elementGeometryCreator: ElementGeometryCreator,
    private val elementEligibleForOsmQuestChecker: ElementEligibleForOsmQuestChecker
) {
    private val countryBoundaries: CountryBoundaries get() = countryBoundariesFuture.get()

    // TODO TEST
    fun downloadMultiple(questTypes: List<OsmElementQuestType<*>>, bbox: BoundingBox): List<OsmElementQuestType<*>> {
        val skippedQuestTypes = mutableSetOf<OsmElementQuestType<*>>()

        var time = System.currentTimeMillis()

        val mapData = mapDataApi.getMapAndHandleTooBigQuery(bbox)

        val elementGeometries = EnumMap<Element.Type, MutableMap<Long, ElementGeometry>>(Element.Type::class.java)
        elementGeometries[Element.Type.NODE] = mutableMapOf()
        elementGeometries[Element.Type.WAY] = mutableMapOf()
        elementGeometries[Element.Type.RELATION] = mutableMapOf()

        val quests = ArrayList<OsmQuest>()
        val questElements = HashSet<Element>()

        val secondsSpentDownloading = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Downloaded ${mapData.nodes.size} nodes, ${mapData.ways.size} ways and ${mapData.relations.size} relations in ${secondsSpentDownloading}s")
        time = System.currentTimeMillis()

        val truncatedBlacklistedPositions = notePositionsSource.getAllPositions(bbox).map { it.truncateTo5Decimals() }.toSet()

        for (questType in questTypes) {
            val questTypeName = questType.getName()

            val countries = questType.enabledInCountries
            if (!countryBoundaries.intersects(bbox, countries)) {
                Log.i(TAG, "$questTypeName: Skipped because it is disabled for this country")
                continue
            }

            for (element in mapData) {
                val appliesToElement = questType.isApplicableTo(element)
                if (appliesToElement == null) {
                    skippedQuestTypes.add(questType)
                    break
                }
                if (!appliesToElement) continue
                if (!elementGeometries[element.type]!!.containsKey(element.id)) {
                    val geometry = elementGeometryCreator.create(element, mapData) ?: continue
                    elementGeometries[element.type]!![element.id] = geometry
                }
                val geometry = elementGeometries[element.type]!![element.id]
                if (!elementEligibleForOsmQuestChecker.mayCreateQuestFrom(questType, element, geometry, truncatedBlacklistedPositions)) continue

                val quest = OsmQuest(questType, element.type, element.id, geometry!!)

                quests.add(quest)
                questElements.add(element)
            }
        }
        val downloadedQuestTypes = questTypes.filterNot { skippedQuestTypes.contains(it) }
        val downloadedQuestTypeNames = downloadedQuestTypes.map { it.getName() }

        val secondsSpentAnalyzing = (System.currentTimeMillis() - time) / 1000

        if (downloadedQuestTypeNames.isNotEmpty()) {

            // elements must be put into DB first because quests have foreign keys on it
            elementDB.putAll(questElements)

            val replaceResult = osmQuestController.replaceInBBox(quests, bbox, downloadedQuestTypeNames)

            elementDB.deleteUnreferenced()

            for (questType in downloadedQuestTypes) {
                questType.cleanMetadata()
            }

            Log.i(TAG,"${downloadedQuestTypeNames.joinToString()}: Added ${replaceResult.added} new and removed ${replaceResult.deleted} already resolved quests (total: ${quests.size}) in ${secondsSpentAnalyzing}s")
        } else {
            Log.i(TAG,"Added and removed no quests because no quest types were downloaded, in ${secondsSpentAnalyzing}s")
        }

        return downloadedQuestTypes
    }

    companion object {
        private const val TAG = "QuestDownload"
    }
}

private fun QuestType<*>.getName() = javaClass.simpleName

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5
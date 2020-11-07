package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log
import de.westnordost.countryboundaries.isInAny
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.intersects
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException
import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.getRelationComplete
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.osmapi.map.isRelationComplete
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osmnotes.NotePositionsSource
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.util.measuredLength
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.FutureTask
import javax.inject.Inject
import javax.inject.Provider

/** Does one API call to get all the map data and generates quests from that. Calls getApplicableElements
 *  on all the quest types */
class OsmApiQuestDownloader @Inject constructor(
    private val elementDB: MergedElementDao,
    private val osmQuestController: OsmQuestController,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val notePositionsSource: NotePositionsSource,
    private val mapDataApi: MapDataApi,
    private val mapDataWithGeometry: Provider<CachingMapDataWithGeometry>,
    private val elementGeometryCreator: ElementGeometryCreator
) : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    fun download(questTypes: List<OsmElementQuestType<*>>, bbox: BoundingBox) {
        if (questTypes.isEmpty()) return

        var time = System.currentTimeMillis()

        val completeRelationGeometries = mutableMapOf<Long, ElementGeometry?>()

        val mapData = mapDataWithGeometry.get()
        getMapAndHandleTooBigQuery(bbox, mapData)
        // bbox should be the bbox of the complete download
        mapData.handle(bbox)

        val quests = ConcurrentLinkedQueue<OsmQuest>()
        val questElements = Collections.newSetFromMap(ConcurrentHashMap<Element, Boolean>(5000))

        val secondsSpentDownloading = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Downloaded ${mapData.nodes.size} nodes, ${mapData.ways.size} ways and ${mapData.relations.size} relations in ${secondsSpentDownloading}s")
        time = System.currentTimeMillis()

        val truncatedBlacklistedPositions = notePositionsSource.getAllPositions(bbox).map { it.truncateTo5Decimals() }.toSet()

        val countryBoundaries = countryBoundariesFuture.get()

        runBlocking {
            for (questType in questTypes) {
                launch(Dispatchers.Default) {
                    val questTypeName = questType.getName()
                    if (!countryBoundaries.intersects(bbox, questType.enabledInCountries)) {
                        Log.d(TAG, "$questTypeName: Skipped because it is disabled for this country")
                    } else {
                        var i = 0
                        val questTime = System.currentTimeMillis()
                        for (element in questType.getApplicableElements(mapData)) {
                            val geometry = getCompleteGeometry(element.type, element.id, mapData, completeRelationGeometries)
                            val quest = createQuest(questType, element, geometry, truncatedBlacklistedPositions) ?: continue

                            quests.add(quest)
                            questElements.add(element)
                            ++i
                        }
                        Log.d(TAG, "$questTypeName: Found $i quests in ${System.currentTimeMillis() - questTime}ms")
                    }
                }
            }
        }
        val secondsSpentAnalyzing = (System.currentTimeMillis() - time) / 1000

        Log.i(TAG,"Created ${quests.size} quests in ${secondsSpentAnalyzing}s")

        time = System.currentTimeMillis()

        // elements must be put into DB first because quests have foreign keys on it
        elementDB.putAll(questElements)

        val questTypeNames = questTypes.map { it.getName() }
        val replaceResult = osmQuestController.replaceInBBox(quests, bbox, questTypeNames)

        elementDB.deleteUnreferenced()

        for (questType in questTypes) {
            questType.cleanMetadata()
        }

        val secondsSpentPersisting = (System.currentTimeMillis() - time) / 1000

        Log.i(TAG,"Persisting ${quests.size} quests in ${secondsSpentPersisting}s")

        Log.i(TAG,"Added ${replaceResult.added} new and removed ${replaceResult.deleted} already resolved quests")
    }

    private fun createQuest(questType: OsmElementQuestType<*>, element: Element, geometry: ElementGeometry?, blacklistedPositions: Set<LatLon>): OsmQuest? {
        // invalid geometry -> can't show this quest, so skip it
        val pos = geometry?.center ?: return null

        // do not create quests whose marker is at/near a blacklisted position
        if (blacklistedPositions.contains(pos.truncateTo5Decimals()))  return null

        // do not create quests in countries where the quest is not activated
        val countries = questType.enabledInCountries
        if (!countryBoundariesFuture.get().isInAny(pos, countries))  return null

        // do not create quests that refer to geometry that is too long for a surveyor to be expected to survey
        if (geometry is ElementPolylinesGeometry) {
            val totalLength = geometry.polylines.sumByDouble { it.measuredLength() }
            if (totalLength > MAX_GEOMETRY_LENGTH_IN_METERS) {
                return null
            }
        }

        return OsmQuest(questType, element.type, element.id, geometry)
    }

    private fun getCompleteGeometry(
        elementType: Element.Type,
        elementId: Long,
        mapData: MapDataWithGeometry,
        cache: MutableMap<Long, ElementGeometry?>
    ): ElementGeometry? {
        return when(elementType) {
            NODE -> mapData.getNodeGeometry(elementId)
            WAY -> mapData.getWayGeometry(elementId)
            // relations are downloaded incomplete from the OSM API, we want the complete geometry here
            RELATION -> getCompleteRelationGeometry(elementId, mapData, cache)
        }
    }

    private fun getCompleteRelationGeometry(id: Long, mapData: MapDataWithGeometry, cache: MutableMap<Long, ElementGeometry?>): ElementGeometry? {
        if (!cache.containsKey(id)) {
            synchronized(cache) {
                if (!cache.containsKey(id)) {
                    cache[id] = createCompleteRelationGeometry(id, mapData)
                }
            }
        }
        return cache[id]
    }

    private fun createCompleteRelationGeometry(id: Long, mapData: MapDataWithGeometry): ElementGeometry? {
        val isComplete = mapData.isRelationComplete(id)
        if (isComplete) {
            // if the relation is already complete within the given mapData, we can just take it from there
            return mapData.getRelationGeometry(id)
        } else {
            // otherwise we need to query the API first and create it from that data instead
            val completeRelationData = mapDataApi.getRelationComplete(id)
            val relation = mapData.getRelation(id) ?: return null
            return elementGeometryCreator.create(relation, completeRelationData, false)
        }
    }

    private fun getMapAndHandleTooBigQuery(bounds: BoundingBox, mapDataHandler: MapDataHandler) {
        try {
            mapDataApi.getMap(bounds, mapDataHandler)
        } catch (e : OsmQueryTooBigException) {
            for (subBounds in bounds.splitIntoFour()) {
                getMapAndHandleTooBigQuery(subBounds, mapDataHandler)
            }
        }
    }

    companion object {
        private const val TAG = "QuestDownload"
    }
}

private fun QuestType<*>.getName() = javaClass.simpleName

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5

private fun BoundingBox.splitIntoFour(): List<BoundingBox> {
    val center = OsmLatLon((maxLatitude + minLatitude) / 2, (maxLongitude + minLongitude) / 2)
    return listOf(
        BoundingBox(minLatitude,     minLongitude,     center.latitude, center.longitude),
        BoundingBox(minLatitude,     center.longitude, center.latitude, maxLongitude),
        BoundingBox(center.latitude, minLongitude,     maxLatitude,     center.longitude),
        BoundingBox(center.latitude, center.longitude, maxLatitude,     maxLongitude)
    )
}

const val MAX_GEOMETRY_LENGTH_IN_METERS = 600

package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log

import java.util.concurrent.FutureTask

import javax.inject.Inject

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.intersects
import de.westnordost.countryboundaries.isInAny
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.getMap
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.util.measuredLength
import java.util.*
import kotlin.collections.ArrayList

/** Takes care of downloading one quest type in a bounding box and persisting the downloaded quests */
class OsmQuestDownloader @Inject constructor(
        private val elementDB: MergedElementDao,
        private val osmQuestController: OsmQuestController,
        private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
        private val mapDataApi: MapDataApi,
        private val elementGeometryCreator: ElementGeometryCreator
) {
    private val countryBoundaries: CountryBoundaries get() = countryBoundariesFuture.get()

    // TODO TEST
    // TODO maybe move/merge to QuestGiver?
    fun downloadMultiple(questTypes: List<OsmElementQuestType<*>>, bbox: BoundingBox, blacklistedPositions: Set<LatLon>): List<OsmElementQuestType<*>> {
        val skippedQuestTypes = mutableSetOf<OsmElementQuestType<*>>()

        var time = System.currentTimeMillis()

        // TODO what if API returns that the area is too big?
        val mapData = mapDataApi.getMap(bbox)

        val elementGeometries = EnumMap<Element.Type, MutableMap<Long, ElementGeometry>>(Element.Type::class.java)
        elementGeometries[NODE] = mutableMapOf()
        elementGeometries[WAY] = mutableMapOf()
        elementGeometries[RELATION] = mutableMapOf()

        val quests = ArrayList<OsmQuest>()
        val questElements = HashSet<Element>()

        val secondsSpentDownloading = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Downloaded ${mapData.nodes.size} nodes, ${mapData.ways.size} ways and ${mapData.relations.size} relations in ${secondsSpentDownloading}s")
        time = System.currentTimeMillis()

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
                if (!mayCreateQuestFrom(questType, element, geometry, blacklistedPositions)) continue

                val quest = OsmQuest(questType, element.type, element.id, geometry!!)

                quests.add(quest)
                questElements.add(element)
            }
        }
        val downloadedQuestTypes = questTypes.filterNot { skippedQuestTypes.contains(it) }
        val downloadedQuestTypeNames = downloadedQuestTypes.map { it.getName() }

        // elements must be put into DB first because quests have foreign keys on it
        elementDB.putAll(questElements)

        val replaceResult = osmQuestController.replaceInBBox(quests, bbox, downloadedQuestTypeNames)

        elementDB.deleteUnreferenced()

        for(questType in downloadedQuestTypes) {
            questType.cleanMetadata()
        }

        val secondsSpentAnalyzing = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"${downloadedQuestTypeNames.joinToString()}: Added ${replaceResult.added} new and removed ${replaceResult.deleted} already resolved quests (total: ${quests.size}) in ${secondsSpentAnalyzing}s")

        return downloadedQuestTypes
    }

    fun download(questType: OsmElementQuestType<*>, bbox: BoundingBox, blacklistedPositions: Set<LatLon>): Boolean {
        val questTypeName = questType.getName()

        val countries = questType.enabledInCountries
        if (!countryBoundaries.intersects(bbox, countries)) {
            Log.i(TAG, "$questTypeName: Skipped because it is disabled for this country")
            return true
        }

        val elements = mutableListOf<Element>()
        val quests = ArrayList<OsmQuest>()
        val truncatedBlacklistedPositions = blacklistedPositions.map { it.truncateTo5Decimals() }.toSet()

        val time = System.currentTimeMillis()
        val success = questType.download(bbox) { element, geometry ->
            if (mayCreateQuestFrom(questType, element, geometry, truncatedBlacklistedPositions)) {
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

    private fun mayCreateQuestFrom(
            questType: OsmElementQuestType<*>, element: Element, geometry: ElementGeometry?,
            blacklistedPositions: Set<LatLon>
    ): Boolean {
        val questTypeName = questType.getName()

        // invalid geometry -> can't show this quest, so skip it
        if (geometry == null) {
            // classified as warning because it might very well be a bug on the geometry creation on our side
            Log.w(TAG, "$questTypeName: Not adding a quest because the element ${element.toLogString()} has no valid geometry")
            return false
        }
        val pos = geometry.center

        // do not create quests whose marker is at/near a blacklisted position
        if (blacklistedPositions.contains(pos.truncateTo5Decimals())) {
            Log.d(TAG, "$questTypeName: Not adding a quest for ${element.toLogString()} because there is a note at that position")
            return false
        }

        // do not create quests in countries where the quest is not activated
        val countries = questType.enabledInCountries
        if (!countryBoundaries.isInAny(pos, countries)) {
            Log.d(TAG, "$questTypeName: Not adding a quest for ${element.toLogString()} because the quest is disabled in this country")
            return false
        }

        // do not create quests that refer to geometry that is too long for a surveyor to be expected to survey
        if (geometry is ElementPolylinesGeometry) {
            val totalLength = geometry.polylines.sumByDouble { it.measuredLength() }
            if (totalLength > MAX_GEOMETRY_LENGTH_IN_METERS) {
                Log.d(TAG, "$questTypeName: Not adding a quest for ${element.toLogString()} because the geometry is too long")
                return false
            }
        }

        return true
    }

    companion object {
        private const val TAG = "QuestDownload"
    }
}

const val MAX_GEOMETRY_LENGTH_IN_METERS = 500

private fun QuestType<*>.getName() = javaClass.simpleName

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5

private fun Element.toLogString() = "${type.name.toLowerCase(Locale.US)} #$id"

private fun LatLon.toLogString() = "$latitude, $longitude"

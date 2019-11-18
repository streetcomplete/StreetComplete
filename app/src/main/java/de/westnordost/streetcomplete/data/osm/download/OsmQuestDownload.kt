package de.westnordost.streetcomplete.data.osm.download

import android.util.Log

import java.util.Locale
import java.util.concurrent.FutureTask

import javax.inject.Inject

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.*
import de.westnordost.streetcomplete.util.SphericalEarthMath

const val MAX_GEOMETRY_LENGTH_IN_METERS = 500

private const val TAG = "QuestDownload"

class OsmQuestDownload @Inject constructor(
    private val geometryDB: ElementGeometryDao,
    private val elementDB: MergedElementDao,
    private val osmQuestDB: OsmQuestDao,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>
) {
    private val countryBoundaries: CountryBoundaries get() = countryBoundariesFuture.get()

    var questListener: VisibleQuestListener? = null

    fun download(questType: OsmElementQuestType<*>, bbox: BoundingBox, blacklistedPositions: Set<LatLon>): Boolean {
        val questTypeName = questType.getName()

        if (!questType.enabledInCountries.intersectsBBox(bbox, countryBoundaries)) {
            Log.i(TAG, "$questTypeName: Skipped because it is disabled for this country")
            return true
        }
        Log.i(TAG, "$questTypeName: Starting")

        val geometryRows = ArrayList<ElementGeometryEntry>()
        val elements = HashMap<ElementKey, Element>()
        val quests = ArrayList<OsmQuest>()
        val previousQuestIdsByElement = osmQuestDB.getAll( bounds = bbox, questTypes = listOf(questTypeName))
            .associate { ElementKey(it.elementType, it.elementId) to it.id!! }.toMutableMap()
        val truncatedBlacklistedPositions = blacklistedPositions.map { it.truncateTo5Decimals() }.toSet()

        val time = System.currentTimeMillis()
        val success = questType.download(bbox) { element, geometry ->
            if (mayCreateQuestFrom(questType, element, geometry, truncatedBlacklistedPositions)) {
                val quest = OsmQuest(questType, element.type, element.id, geometry!!)

                geometryRows.add(ElementGeometryEntry(element.type, element.id, quest.geometry))
                quests.add(quest)
                val elementKey = ElementKey(element.type, element.id)
                elements[elementKey] = element
                previousQuestIdsByElement.remove(elementKey)
            }
        }
        if (!success) return false

        // geometry and elements must be put into DB first because quests have foreign keys on it
        geometryDB.putAll(geometryRows)
        elementDB.putAll(elements.values)

        val newQuestsByQuestType = osmQuestDB.addAll(quests)

        if (questListener != null) {
            // it is null if this quest is already in the DB, so don't call onQuestCreated
            quests.removeAll { it.id == null }
            if (quests.isNotEmpty()) questListener?.onQuestsCreated(quests, QuestGroup.OSM)
        }

        if (previousQuestIdsByElement.isNotEmpty()) {
            questListener?.onQuestsRemoved(previousQuestIdsByElement.values, QuestGroup.OSM)
            osmQuestDB.deleteAllIds(previousQuestIdsByElement.values)
        }

        // note: this could be done after ALL osm quest types have been downloaded if this
        // turns out to be slow if done for every quest type
        geometryDB.deleteUnreferenced()
        elementDB.deleteUnreferenced()
        questType.cleanMetadata()

        val obsoleteAmount = previousQuestIdsByElement.size
        val secondsSpent = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"$questTypeName: Added $newQuestsByQuestType new and removed $obsoleteAmount already resolved quests. (Total: ${quests.size}) in ${secondsSpent}s")

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

        // do not create quests that refer to geometry that is too long for a surveyor to be expected to survey
        if (geometry is ElementPolylinesGeometry) {
            val distance = geometry.polylines.sumByDouble { SphericalEarthMath.distance(it) }
            if (distance > MAX_GEOMETRY_LENGTH_IN_METERS) {
                Log.d(TAG, "$questTypeName: Not adding a quest at ${pos.toLogString()} because the geometry is too long")
                return false
            }
        }

        // do not create quests whose marker is at/near a blacklisted position
        if (blacklistedPositions.contains(pos.truncateTo5Decimals())) {
            Log.d(TAG, "$questTypeName: Not adding a quest at ${pos.toLogString()} because there is a note at that position")
            return false
        }

        // do not create quests in countries where the quest is not activated
        if (!questType.enabledInCountries.containsPosition(pos, countryBoundaries)) {
            Log.d(TAG, "$questTypeName: Not adding a quest at ${pos.toLogString()} because the quest is disabled in this country")
            return false
        }

        return true
    }
}

private fun QuestType<*>.getName() = javaClass.simpleName

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5

private fun Element.toLogString() = "${type.name.toLowerCase(Locale.US)} #$id"

private fun LatLon.toLogString() = "$latitude, $longitude"

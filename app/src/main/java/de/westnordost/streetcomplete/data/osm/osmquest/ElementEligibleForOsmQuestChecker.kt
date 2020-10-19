package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.isInAny
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.util.measuredLength
import java.util.concurrent.FutureTask
import javax.inject.Inject

/** Checks if the given quest type may be created based on its geometry and location */
class ElementEligibleForOsmQuestChecker @Inject constructor(
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
) {
    fun mayCreateQuestFrom(
        questType: OsmElementQuestType<*>, geometry: ElementGeometry?, blacklistedPositions: Set<LatLon>
    ): Boolean {
        // invalid geometry -> can't show this quest, so skip it
        val pos = geometry?.center ?: return false

        // do not create quests whose marker is at/near a blacklisted position
        if (blacklistedPositions.contains(pos.truncateTo5Decimals())) {
            return false
        }

        // do not create quests in countries where the quest is not activated
        val countries = questType.enabledInCountries
        if (!countryBoundariesFuture.get().isInAny(pos, countries)) {
            return false
        }

        // do not create quests that refer to geometry that is too long for a surveyor to be expected to survey
        if (geometry is ElementPolylinesGeometry) {
            val totalLength = geometry.polylines.sumByDouble { it.measuredLength() }
            if (totalLength > MAX_GEOMETRY_LENGTH_IN_METERS) {
                return false
            }
        }

        return true
    }
}

const val MAX_GEOMETRY_LENGTH_IN_METERS = 500

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5
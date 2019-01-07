package de.westnordost.streetcomplete.quests.localized_name.data

import java.util.regex.Pattern

import javax.inject.Inject

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler

// TODO only open in order to be able to mock it in tests
open class PutRoadNameSuggestionsHandler @Inject constructor(
    private val roadNameSuggestionsDao: RoadNameSuggestionsDao) :
    MapDataWithGeometryHandler {

    override fun handle(element: Element, geometry: ElementGeometry?) {
        if (element.type != Element.Type.WAY) return
        val points = geometry?.polylines?.get(0) ?: return
        val namesByLanguage = element.tags?.toRoadNameByLanguage() ?: return

        roadNameSuggestionsDao.putRoad(element.id, namesByLanguage, points)
    }

    private fun Map<String,String>.toRoadNameByLanguage(): Map<String, String>? {
        val result = mutableMapOf<String,String>()
        val namePattern = Pattern.compile("name(:(.*))?")
        for ((key, value) in this) {
            val m = namePattern.matcher(key)
            if (m.matches()) {
                val languageCode = m.group(2) ?: ""
                result[languageCode] = value
            }
        }
        return if (result.isEmpty()) null else result
    }
}

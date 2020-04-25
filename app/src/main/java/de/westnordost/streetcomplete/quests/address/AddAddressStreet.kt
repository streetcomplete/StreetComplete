package de.westnordost.streetcomplete.quests.address

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import java.util.regex.Pattern

class AddAddressStreet(
        private val overpassApi: OverpassMapDataAndGeometryApi,
        private val roadNameSuggestionsDao: RoadNameSuggestionsDao
) : OsmElementQuestType<AddressStreetAnswer> {
    override val commitMessage = "Add street/place names to address"
    override val icon = R.drawable.ic_quest_housenumber_street

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_street_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val housenumber = tags["addr:housenumber"]
        return if (housenumber != null) arrayOf(housenumber) else arrayOf()
    }

    override fun createForm() = AddAddressStreetForm()

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        if (!overpassApi.query(getOverpassQuery(bbox), handler)) return false
        if (!overpassApi.query(getStreetNameSuggestionsOverpassQuery(bbox), this::putRoadNameSuggestion)) return false
        return true
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + """
            relation["type"="associatedStreet"];
            > -> .inStreetRelation;

            way["highway" ~ "^(${ALL_ROADS.joinToString("|")})$"]["name"] -> .named_roads;

            nwr["addr:street"!~".*"]["addr:housenumber"]["addr:place"!~".*"]
                (around.named_roads:$MAX_DIST_FOR_ROAD_NAME_SUGGESTION_IN_METERS) -> .missing_data;

            (.missing_data; - .inStreetRelation;);""" +
                    getQuestPrintStatement()

    // TODO: probably either this should be modified to match AddRoadName with custom set of roads  with config array migrated to OsmTaggings
    // TODO: or AddRoadName can use something like that
    /** return overpass query string to get roads with names
     * */
    private fun getStreetNameSuggestionsOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + "\n" + """
        way[highway ~ "^(${ALL_ROADS.joinToString("|")})$"][name];
        out body geom;""".trimIndent()

    override fun applyAnswerTo(answer: AddressStreetAnswer, changes: StringMapChangesBuilder) {
        val key = when(answer) {
            is StreetName -> "addr:street"
            is PlaceName -> "addr:place"
        }
        for ((languageCode, name) in answer.localizedNames) {
            if (languageCode.isEmpty()) {
                changes.addOrModify(key, name)
            } else {
                changes.addOrModify("$key:$languageCode", name)
            }
        }
    }

    private fun putRoadNameSuggestion(element: Element, geometry: ElementGeometry?) {
        if (element.type != Element.Type.WAY) return
        if (geometry !is ElementPolylinesGeometry) return
        val namesByLanguage = element.tags?.toRoadNameByLanguage() ?: return

        roadNameSuggestionsDao.putRoad(element.id, namesByLanguage, geometry.polylines.first())
    }

    companion object {
        //TODO: this only appears to pick up nodes, so street-name suggestions don't appear
        // in the middle of a long straight-stretch. There might need to be a better way.
        const val MAX_DIST_FOR_ROAD_NAME_SUGGESTION_IN_METERS = 250.0
    }
}

/** OSM tags (i.e. name:de=Bäckergang) to map of language code -> name (i.e. de=Bäckergang) */
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

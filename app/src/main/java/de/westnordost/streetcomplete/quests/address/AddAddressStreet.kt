package de.westnordost.streetcomplete.quests.address

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import java.util.regex.Pattern

class AddAddressStreet(
        private val overpassServer: OverpassMapDataAndGeometryDao,
        private val roadNameSuggestionsDao: RoadNameSuggestionsDao
) : OsmElementQuestType<AddressStreetAnswer> {
    override val commitMessage = "Add street/place names to address"
    override val icon = R.drawable.ic_quest_housenumber_street

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_street_title

    override fun createForm() = AddAddressStreetForm()

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        Log.wtf("aaa", getOverpassQuery(bbox))
        if (!overpassServer.query(getOverpassQuery(bbox), handler)) return false
        if (!overpassServer.query(getStreetNameSuggestionsOverpassQuery(bbox), this::putRoadNameSuggestion)) return false
        return true
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + """
            relation["type"="associatedStreet"];
            > -> .inStreetRelation;

            way["highway" ~ "^(${OsmTaggings.ALL_ROADS.joinToString("|")})$"]["name"] -> .named_roads;

            nwr["addr:street"!~".*"]["addr:housenumber"]["addr:place"!~".*"](around.named_roads:150) -> .missing_data;

            (.missing_data; - .inStreetRelation;);""" +
                    getQuestPrintStatement()

    // TODO: probably either this should be modified to match AddRoadName with custom set of roads  with config array migrated to OsmTaggings
    // TODO: or AddRoadName can use something like that
    /** return overpass query string to get roads with names
     * */
    private fun getStreetNameSuggestionsOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + "\n" + """
        way[highway ~ "^(${OsmTaggings.ALL_ROADS.joinToString("|")})$"][name];
        out body geom;""".trimIndent()

    override fun applyAnswerTo(answer: AddressStreetAnswer, changes: StringMapChangesBuilder) {
        var key = ""
        when(answer){
            is StreetName -> {key = "addr:street"}
            is PlaceName -> {key = "addr:place"}
        }
        //TODO: can we actually get addr:street/place:langcode here, and if so,
        // should we polute OSM with this?
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
        const val MAX_DIST_FOR_ROAD_NAME_SUGGESTION_IN_METERS = 100.0
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

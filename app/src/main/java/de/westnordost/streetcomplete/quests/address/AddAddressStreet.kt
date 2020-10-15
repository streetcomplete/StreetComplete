package de.westnordost.streetcomplete.quests.address

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.road_name.data.putRoadNameSuggestion

class AddAddressStreet(
        private val overpassApi: OverpassMapDataAndGeometryApi,
        private val roadNameSuggestionsDao: RoadNameSuggestionsDao
) : OsmElementQuestType<AddressStreetAnswer> {

    override val commitMessage = "Add street/place names to address"
    override val icon = R.drawable.ic_quest_housenumber_street
    // In Japan, housenumbers usually have block numbers, not streets
    override val enabledInCountries = AllCountriesExcept("JP")

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_street_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val housenumber = tags["addr:streetnumber"] ?: tags["addr:housenumber"]
        return if (housenumber != null) arrayOf(housenumber) else arrayOf()
    }

    override fun createForm() = AddAddressStreetForm()

    /* cannot be determined offline because the quest kinda needs the street name suggestions
       to work conveniently (see #1856) */
    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        if (!overpassApi.query(getStreetNameSuggestionsOverpassQuery(bbox), roadNameSuggestionsDao::putRoadNameSuggestion)) return false
        if (!overpassApi.query(getOverpassQuery(bbox), handler)) return false
        return true
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + """
            relation["type"="associatedStreet"]; > -> .inStreetRelation;
            $ADDRESSES_WITHOUT_STREETS -> .missing_data;
            (.missing_data; - .inStreetRelation;);
            """.trimIndent() + getQuestPrintStatement()

    /** return overpass query string to get roads with names around addresses without streets
     * */
    private fun getStreetNameSuggestionsOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + "\n" + """
        $ADDRESSES_WITHOUT_STREETS -> .address_missing_street;
        $ROADS_WITH_NAMES -> .named_roads;
        way.named_roads(
            around.address_missing_street: $MAX_DIST_FOR_ROAD_NAME_SUGGESTION);
        out body geom;""".trimIndent()

    override fun applyAnswerTo(answer: AddressStreetAnswer, changes: StringMapChangesBuilder) {
        val key = when(answer) {
            is StreetName -> "addr:street"
            is PlaceName -> "addr:place"
        }
        changes.add(key, answer.name)
    }

    companion object {
        const val MAX_DIST_FOR_ROAD_NAME_SUGGESTION = 100.0

        private val ADDRESSES_WITHOUT_STREETS = """
                (
                    nwr["addr:housenumber"][!"addr:street"][!"addr:place"][!"addr:block_number"];
                    nwr["addr:streetnumber"][!"addr:street"];
                )""".trimIndent()
        private val ROADS_WITH_NAMES =
                "way[highway ~ \"^(${ALL_ROADS.joinToString("|")})$\"][name]"
    }
}

package de.westnordost.streetcomplete.quests.address

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.tagfilters.FiltersParser
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
        val housenumber = tags["addr:streetnumber"] ?: tags["addr:housenumber"]
        return if (housenumber != null) arrayOf(housenumber) else arrayOf()
    }

    override fun createForm() = AddAddressStreetForm()

    override fun isApplicableTo(element: Element): Boolean? =
            ADDRESSES_WITHOUT_STREETS_TFE.matches(element)

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        if (!overpassApi.query(getOverpassQuery(bbox), handler)) return false
        if (!overpassApi.query(getStreetNameSuggestionsOverpassQuery(bbox), roadNameSuggestionsDao::putRoadNameSuggestion)) return false
        return true
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + """
            relation["type"="associatedStreet"];
            > -> .inStreetRelation;

            $ROADS_WITH_NAMES -> .named_roads;

            $ADDRESSES_WITHOUT_STREETS
                (around.named_roads:$MAX_DIST_FOR_ROAD_NAME_SUGGESTION) -> .missing_data;

            (.missing_data; - .inStreetRelation;);""".trimIndent() +
                    getQuestPrintStatement()

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
        for ((languageCode, name) in answer.localizedNames) {
            if (languageCode.isEmpty()) {
                changes.addOrModify(key, name)
            } else {
                changes.addOrModify("$key:$languageCode", name)
            }
        }
    }

    companion object {
        // This only picks up nodes, so street-name suggestions don't appear
        // in the middle of a long straight-stretch (longer than 0.5km). This happens rarely
        // enough that I don't think it is a concern.
        const val MAX_DIST_FOR_ROAD_NAME_SUGGESTION = 250.0

        private const val ADDRESSES_WITHOUT_STREETS = """
                (
                    nwr["addr:housenumber"][!"addr:street"][!"addr:place"][!"addr:block_number"];
                    nwr["addr:streetnumber"][!"addr:street"];
                );""".trimIndent()
        // this must be the same as above but in tag filter expression syntax
        private val ADDRESSES_WITHOUT_STREETS_TFE by lazy { FiltersParser().parse("""
                nodes, ways, relations with 
                  addr:housenumber and !addr:street and !addr:place and !addr:block_number
                  or addr:streetnumber and !addr:street
        """)}

        private val ROADS_WITH_NAMES =
                "way[highway ~ \"^(${ALL_ROADS.joinToString("|")})$\"][name]"
    }
}

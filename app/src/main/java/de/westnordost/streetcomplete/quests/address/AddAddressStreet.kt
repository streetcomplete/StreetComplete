package de.westnordost.streetcomplete.quests.address

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.osmapi.overpass.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao

class AddAddressStreet(
        private val overpassServer: OverpassMapDataDao,
        private val roadNameSuggestionsDao: RoadNameSuggestionsDao,
        private val putRoadNameSuggestionsHandler: PutRoadNameSuggestionsHandler
) : OsmElementQuestType<AddressStreetAnswer> {
    override val commitMessage = "Add address"
    override val icon = R.drawable.ic_quest_housenumber_street

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_street_title

    override fun createForm() = AddAddressStreetForm()

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        Log.wtf("aaa", getOverpassQuery(bbox))
        return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler)
                && overpassServer.getAndHandleQuota(getStreetNameSuggestionsOverpassQuery(bbox),putRoadNameSuggestionsHandler)
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
        when(answer){
            is StreetName -> {changes.add("addr:street", answer.name)}
            is PlaceName -> {changes.add("addr:place", answer.name)}
        }
    }

    companion object {
        const val MAX_DIST_FOR_ROAD_NAME_SUGGESTION_IN_METERS = 100.0
    }
}

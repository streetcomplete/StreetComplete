package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.tagfilters.FiltersParser
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.localized_name.data.putRoadNameSuggestion

class AddRoadName(
    private val overpassApi: OverpassMapDataAndGeometryApi,
    private val roadNameSuggestionsDao: RoadNameSuggestionsDao
) : OsmElementQuestType<RoadNameAnswer> {

    override val enabledInCountries = AllCountriesExcept("JP")
    override val commitMessage = "Determine road names and types"
    override val wikiLink = "Key:name"
    override val icon = R.drawable.ic_quest_street_name
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) =
        if (tags["highway"] == "pedestrian")
            R.string.quest_streetName_pedestrian_title
        else
            R.string.quest_streetName_title

    override fun isApplicableTo(element: Element) = ROADS_WITHOUT_NAMES_TFE.matches(element)

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        if (!overpassApi.query(getOverpassQuery(bbox), handler)) return false
        if (!overpassApi.query(getStreetNameSuggestionsOverpassQuery(bbox), roadNameSuggestionsDao::putRoadNameSuggestion)) return false
        return true
    }

    /** returns overpass query string for creating the quests */
    private fun getOverpassQuery(bbox: BoundingBox) =
        bbox.toGlobalOverpassBBox() + "\n" +
        ROADS_WITHOUT_NAMES + "->.unnamed;\n" +
        "(\n" +
        "  way.unnamed['access' !~ '^(private|no)$'];\n" +
        "  way.unnamed['foot']['foot' !~ '^(private|no)$'];\n" +
        "); " +
        getQuestPrintStatement()

    /** return overpass query string to get roads with names near roads that don't have names
     *  private roads are not filtered out here, partially to reduce complexity but also
     *  because the road may have a private segment that is named already or is close to a road
     *  with a useful name
     * */
    private fun getStreetNameSuggestionsOverpassQuery(bbox: BoundingBox) =
        bbox.toGlobalOverpassBBox() + "\n" + """
        $ROADS_WITHOUT_NAMES -> .without_names;
        $ROADS_WITH_NAMES -> .with_names;
        way.with_names(around.without_names: $MAX_DIST_FOR_ROAD_NAME_SUGGESTION );
        out body geom;""".trimIndent()

    override fun createForm() = AddRoadNameForm()

    override fun applyAnswerTo(answer: RoadNameAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoRoadName        -> changes.add("noname", "yes")
            is RoadIsServiceRoad -> changes.modify("highway", "service")
            is RoadIsTrack       -> changes.modify("highway", "track")
            is RoadIsLinkRoad    -> {
                val prevValue = changes.getPreviousValue("highway")
                if (prevValue?.matches("primary|secondary|tertiary".toRegex()) == true) {
                    changes.modify("highway", prevValue + "_link")
                }
            }
            is RoadName -> {
                val singleName = answer.localizedNames.singleOrNull()
                if (singleName?.isRef() == true) {
                    changes.add("ref", singleName.name)
                } else {
                    applyAnswerRoadName(answer, changes)
                }
            }
        }
    }

    private fun applyAnswerRoadName(answer: RoadName, changes: StringMapChangesBuilder) {
        for ((languageTag, name) in answer.localizedNames) {
            val key = when (languageTag) {
                "" -> "name"
                "international" -> "int_name"
                else -> "name:$languageTag"
            }
            changes.addOrModify(key, name)
        }
        // these params are passed from the form only to update the road name suggestions so that
        // newly input street names turn up in the suggestions as well
        val roadNameByLanguage = answer.localizedNames.associate { it.languageTag to it.name }
        roadNameSuggestionsDao.putRoad( answer.wayId, roadNameByLanguage, answer.wayGeometry)
    }

    companion object {
        const val MAX_DIST_FOR_ROAD_NAME_SUGGESTION = 30.0 //m

        // to avoid spam, only ask for names on a limited set of roads
        private const val NAMEABLE_ROADS =
                "primary|secondary|tertiary|unclassified|residential|living_street|pedestrian"
        private const val ROADS_WITHOUT_NAMES =
                "way[highway ~ \"^($NAMEABLE_ROADS)$\"][!name][!ref][noname != yes][!junction][area != yes]"
        // this must be the same as above but in tag filter expression syntax
        private val ROADS_WITHOUT_NAMES_TFE by lazy { FiltersParser().parse(
                "ways with highway ~ $NAMEABLE_ROADS and !name and !ref and noname != yes and !junction and area != yes"
        )}

        // but we can find name suggestions on any type of already-named road
        private val ROADS_WITH_NAMES =
                "way[highway ~ \"^(${ALL_ROADS.joinToString("|")})$\"][name]"
    }
}

private fun LocalizedName.isRef() =
    languageTag.isEmpty() && name.matches("[A-Z]{0,3}[ -]?[0-9]{0,5}".toRegex())

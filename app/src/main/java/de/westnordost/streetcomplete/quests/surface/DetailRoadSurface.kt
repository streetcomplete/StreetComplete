package de.westnordost.streetcomplete.quests.surface

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.tagfilters.FiltersParser
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox


class DetailRoadSurface(private val overpassMapDataApi: OverpassMapDataAndGeometryApi) : OsmElementQuestType<DetailSurfaceAnswer> {
    override val commitMessage = "More detailed surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface_detail

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags["area"] == "yes"

        return if (hasName) {
            if (isSquare)
                R.string.ic_quest_surface_detailed_square_name_title
            else
                R.string.ic_quest_surface_detailed_name_title
        } else {
            if (isSquare)
                R.string.ic_quest_surface_detailed_square_title
            else
                R.string.ic_quest_surface_detailed_title
        }
    }

    override fun createForm() = DetailRoadSurfaceForm()

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassMapDataApi.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if(!REQUIRED_MINIMAL_MATCH_TFE.matches(element)) {
            return false;
        }
        element.tags.forEach {
            if(it.key.contains("surface:")) {
                return false;
            }
            if(it.key.contains(":surface")) {
                return false;
            }
        }
        return true;
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
        bbox.toGlobalOverpassBBox() + "\n" + """

          way[surface~"^(${UNDETAILED_SURFACE_TAG_MATCH})${'$'}"][segregated!="yes"][highway ~ "^${ HIGHWAY_TAG_MATCH }${'$'}"] -> .surface_without_detail;
          // https://taginfo.openstreetmap.org//search?q=%3Asurface
          // https://taginfo.openstreetmap.org//search?q=surface:
          way[surface~"^(${UNDETAILED_SURFACE_TAG_MATCH})${'$'}"][~"(:surface|surface:)" ~ "."] -> .extra_tags;
          way.surface_without_detail[surface ~ "^(${UNDETAILED_SURFACE_TAG_MATCH})${'$'}"][access !~ "^(private|no)${'$'}"] -> .not_private;
          way.surface_without_detail[surface ~ "^(${UNDETAILED_SURFACE_TAG_MATCH})${'$'}"][foot][foot !~ "^(private|no)${'$'}"] -> .foot_access;
          ((.not_private; .foot_access;); - .extra_tags;);
        """.trimIndent() + "\n" +
        getQuestPrintStatement()

    private val HIGHWAY_TAG_MATCH = ROADS_WITH_SURFACES_BROADLY_DEFINED.joinToString("|")
    private val UNDETAILED_SURFACE_TAG_MATCH = "paved|unpaved"
    private val REQUIRED_MINIMAL_MATCH_TFE by lazy { FiltersParser().parse(
            "ways with surface ~ ${UNDETAILED_SURFACE_TAG_MATCH} and segregated!=yes and highway ~ ${HIGHWAY_TAG_MATCH} and (access !~ private|no or (foot and foot !~ private|no))"
    )}

    override val isSplitWayEnabled = true

    override fun applyAnswerTo(answer: DetailSurfaceAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is SurfaceAnswer -> {
                changes.modify("surface", answer.value)
            }
            is DetailingImpossibleAnswer -> {
                changes.add("surface:note", answer.value)
            }
        }
    }

    companion object {
        // well, all roads have surfaces, what I mean is that not all ways with highway key are
        // "something with a surface"
        // see https://github.com/westnordost/StreetComplete/pull/327#discussion_r121937808
        private val ROADS_WITH_SURFACES_BROADLY_DEFINED = arrayOf(
                "trunk","trunk_link","motorway","motorway_link",
                "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
                "unclassified", "residential", "living_street", "pedestrian", "track", "road",
                "service"
        )
    }
}

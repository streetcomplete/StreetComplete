package de.westnordost.streetcomplete.quests.width

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.ROADS_ASSUMED_TO_BE_PAVED
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.ANYTHING_PAVED
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker

class AddRoadWidth(
    private val checkArSupport: ArSupportChecker
) : OsmElementQuestType<WidthAnswer> {

    private val nodeFilter by lazy { """
       nodes with
         traffic_calming ~ ${ROAD_NARROWERS.joinToString("|")}
         and (!width or source:width ~ ".*estimat.*")
         and (!maxwidth or source:maxwidth ~ ".*estimat.*")
    """.toElementFilterExpression() }

    private val wayFilter by lazy { """
        ways with (
          (
            highway ~ trunk|primary|secondary|tertiary|unclassified|residential
            and (lane_markings = no or lanes < 2)
          ) or (
            highway = residential
            and (
              maxspeed < 33
              or maxspeed = walk
              or ~"${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}" ~ ".*:(zone)?:?([1-9]|[1-2][0-9]|30)"
            )
            and lane_markings != yes and (!lanes or lanes < 2)
          )
          or highway = living_street
          or highway = service and service = alley
        )
        and area != yes
        and (!width or source:width ~ ".*estimat.*")
        and (surface ~ ${ANYTHING_PAVED.joinToString("|")} or highway ~ ${ROADS_ASSUMED_TO_BE_PAVED.joinToString("|")})
        and (access !~ private|no or (foot and foot !~ private|no))
        and foot != no
        and placement != transition
    """.toElementFilterExpression() }

    override val changesetComment = "Determine road widths"
    override val wikiLink = "Key:width"
    override val icon = R.drawable.ic_quest_street_width
    override val achievements = listOf(EditTypeAchievement.CAR)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else 0

    override fun getTitle(tags: Map<String, String>) = R.string.quest_road_width_title

    override fun getApplicableElements(mapData: MapDataWithGeometry) =
        mapData.nodes.filter { nodeFilter.matches(it) } + mapData.ways.filter { wayFilter.matches(it) }

    override fun isApplicableTo(element: Element) =
        nodeFilter.matches(element) || wayFilter.matches(element)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with traffic_calming ~ choker|chicane|island|choked_island|choked_table")

    override fun createForm() = AddWidthForm()

    override fun applyAnswerTo(answer: WidthAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val key = if (tags["traffic_calming"] in ROAD_NARROWERS) "maxwidth" else "width"

        tags[key] = answer.width.toOsmValue()

        if (answer.isARMeasurement) {
            tags["source:$key"] = "ARCore"
        } else {
            tags.remove("source:$key")
        }

        // update width:carriageway if it is set
        if (key == "width" && tags.containsKey("width:carriageway")) {
            tags["width:carriageway"] = answer.width.toOsmValue()
        }
    }
}

private val ROAD_NARROWERS = setOf("choker", "chicane", "choked_table")

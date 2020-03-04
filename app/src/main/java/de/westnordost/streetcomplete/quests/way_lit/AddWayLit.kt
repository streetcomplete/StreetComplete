package de.westnordost.streetcomplete.quests.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddWayLit(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    /* Using sidewalk as a tell-tale tag for (urban) streets which reached a certain level of
       development. I.e. non-urban streets will usually not even be lit in industrialized
       countries.
       Also, only include paths only for those which are equal to footway/cycleway to exclude
       most hike paths and trails.

        See #427 for discussion. */
    override val tagFilters = """
        ways with
        (
          highway ~ ${LIT_RESIDENTIAL_ROADS.joinToString("|")}
          or highway ~ ${LIT_NON_RESIDENTIAL_ROADS.joinToString("|")} and
          (
            sidewalk ~ both|left|right|yes|separate
            or ~source:maxspeed|maxspeed:type|zone:maxspeed|zone:traffic ~ .+:urban
          )
          or highway ~ ${LIT_WAYS.joinToString("|")}
          or highway = path and (foot = designated or bicycle = designated)
        )
        and !lit
        and (access !~ private|no or (foot and foot !~ private|no))
    """

    override val commitMessage = "Add whether way is lit"
    override val icon = R.drawable.ic_quest_lantern
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val type = tags["highway"]
        val hasName = tags.containsKey("name")
        val isRoad = LIT_NON_RESIDENTIAL_ROADS.contains(type) || LIT_RESIDENTIAL_ROADS.contains(type)

        return when {
            hasName -> R.string.quest_way_lit_named_title
            isRoad  -> R.string.quest_way_lit_road_title
            else    -> R.string.quest_way_lit_title
        }
    }

    override fun createForm() = WayLitForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("lit", answer)
    }

    companion object {
        private val LIT_RESIDENTIAL_ROADS = arrayOf("residential", "living_street", "pedestrian")

        private val LIT_NON_RESIDENTIAL_ROADS =
            arrayOf("primary", "primary_link", "secondary", "secondary_link",
                    "tertiary", "tertiary_link", "unclassified", "service")

        private val LIT_WAYS = arrayOf("footway", "cycleway", "steps")
    }
}

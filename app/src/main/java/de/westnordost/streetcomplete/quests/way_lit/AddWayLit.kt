package de.westnordost.streetcomplete.quests.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.DayNightCycle.ONLY_NIGHT
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.lit.applyTo

class AddWayLit : OsmFilterQuestType<WayLitOrIsStepsAnswer>() {

    /* Using sidewalk, source:maxspeed=*urban etc and a urban-like maxspeed as tell-tale tags for
       (urban) streets which reached a certain level of development. I.e. non-urban streets will
       usually not even be lit in industrialized countries.

       Also, only include paths only for those which are equal to footway/cycleway to exclude
       most hike paths and trails.

        See #427 for discussion. */
    override val elementFilter = """
        ways with
        (
          highway ~ ${LIT_RESIDENTIAL_ROADS.joinToString("|")}
          or highway ~ ${LIT_NON_RESIDENTIAL_ROADS.joinToString("|")} and
          (
            sidewalk ~ both|left|right|yes|separate
            or sidewalk:both = yes
            or sidewalk:left = yes
            or sidewalk:right = yes
            or ~"${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}" ~ ".*:(urban|.*zone.*|nsl_restricted)"
            or maxspeed <= 60
          )
          or highway ~ ${LIT_WAYS.joinToString("|")}
          or highway = path and (foot = designated or bicycle = designated)
        )
        and (access !~ private|no or (foot and foot !~ private|no))
        and
        (
          !lit
          or lit = no and lit older today -8 years
          or lit older today -16 years
        )
        and indoor != yes
        and ~path|footway|cycleway !~ link
    """

    override val changesetComment = "Specify whether ways are lit"
    override val wikiLink = "Key:lit"
    override val icon = R.drawable.ic_quest_lantern
    override val achievements = listOf(PEDESTRIAN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_overlay
    override val dayNightCycle = ONLY_NIGHT

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lit_title

    override fun createForm() = WayLitForm()

    override fun applyAnswerTo(answer: WayLitOrIsStepsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is IsActuallyStepsAnswer -> tags.changeToSteps()
            is WayLit -> answer.litStatus.applyTo(tags)
        }
    }

    companion object {
        private val LIT_RESIDENTIAL_ROADS = arrayOf("residential", "living_street", "pedestrian")

        private val LIT_NON_RESIDENTIAL_ROADS = arrayOf(
            "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
            "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "service"
        )

        private val LIT_WAYS = arrayOf("footway", "cycleway", "steps")
    }
}

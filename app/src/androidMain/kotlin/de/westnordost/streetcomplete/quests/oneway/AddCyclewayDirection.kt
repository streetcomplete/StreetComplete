package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.BACKWARD
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.FORWARD
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.NO_ONEWAY
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_maxspeed
import de.westnordost.streetcomplete.resources.quest_arrow_tutorial
import de.westnordost.streetcomplete.resources.quest_cycleway_direction_title

class AddCyclewayDirection : OsmFilterQuestType<OnewayAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
          (
              (highway ~ path|footway) or (highway = cycleway)
              and (footway = sidewalk or is_sidepath = yes)
              and bicycle ~ yes|designated
          )
          and !oneway
          and !oneway:bicycle
          and area != yes
          and junction != roundabout
          and access !~ private|no
    """

    override val changesetComment = "Specify in which direction cyclists may ride this path"
    override val wikiLink = "Key:oneway:bicycle"
    override val icon = R.drawable.quest_bicycleway_oneway
    override val title = Res.string.quest_cycleway_direction_title
    override val hasMarkersAtEnds = true
    override val achievements = listOf(BICYCLIST)
    override val hint = Res.string.quest_arrow_tutorial
    override val defaultDisabledMessage = Res.string.default_disabled_msg_visible_sign_bicycle_sidewalk_access
    override val enabledInCountries = NoCountriesExcept("DE", "AT", "DK", "NL", "FI", "NO")

    override fun createForm() = AddCyclewayDirectionForm()

    override fun applyAnswerTo(
        answer: OnewayAnswer,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        val key = if (tags["highway"] == "cycleway" &&
            tags["foot"] !in setOf("yes", "designated") &&
            tags["segregated"] != "yes"
        ) {
            "oneway"
        } else {
            "oneway:bicycle"
        }

        tags[key] = when (answer) {
            FORWARD -> "yes"
            BACKWARD -> "-1"
            NO_ONEWAY -> "no"
        }
    }
}

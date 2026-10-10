package de.westnordost.streetcomplete.quests.cycleway

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.*
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.oneway.AddOnewayForm
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept

class AddCyclewayDirection : OsmFilterQuestType<OnewayAnswer>() {

    override val elementFilter = """
        ways with
          highway ~ path|footway|cycleway
          and (footway = sidewalk or is_sidepath = yes)
          and bicycle ~ yes|designated
          and !oneway
          and !oneway:bicycle
          and area != yes
          and junction != roundabout
          and access !~ private|no
    """

    override val changesetComment = "Specify in which direction cyclists may ride this path"
    override val wikiLink = "Key:oneway:bicycle"
    override val icon = Res.drawable.quest_bicycleway_bidirectional
    override val title = Res.string.quest_cycleway_direction_title
    override val achievements = listOf(BICYCLIST)
    override val hint = Res.string.quest_arrow_tutorial
    override val defaultDisabledMessage = Res.string.default_disabled_msg_cycleway
    override val enabledInCountries = NoCountriesExcept("DE", "AT", "DK", "NL", "FI", "NO")

    @Composable
    override fun Form(
        on: (QuestAction<OnewayAnswer>) -> Unit,
        element: Element,
        geometry: ElementGeometry,
        countryInfo: CountryInfo,
    ) {
        AddOnewayForm(on, geometry)
    }

    override fun applyAnswerTo(answer: OnewayAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
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

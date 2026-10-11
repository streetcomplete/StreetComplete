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
          and (
            footway = sidewalk
            or path = sidewalk
            or is_sidepath = yes
            or footway = sidepath
            or path = sidepath
            or cycleway = sidepath
          )
          and (
            bicycle ~ yes|designated
            or (highway = cycleway and !bicycle)
          )
          and !oneway
          and !oneway:bicycle
          and !oneway:conditional
          and !oneway:bicycle:conditional
          and !bicycle:forward
          and !bicycle:backward
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
    /**
     * Country selection based on the research and sources in [issue #4827](https://github.com/streetcomplete/StreetComplete/issues/4827#issuecomment-6113562219).
     */
    override val enabledInCountries = NoCountriesExcept("DE", "DK", "NL", "FI", "NO", "BE", "HR")

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
        val key = if (
            tags["highway"] == "cycleway"
            && tags["foot"] != "yes"
            && tags["foot"] != "designated"
            && tags["segregated"] != "yes"
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

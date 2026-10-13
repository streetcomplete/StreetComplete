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
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept

class AddCyclewayDirection : OsmFilterQuestType<OnewayAnswer>() {

    override val elementFilter = """
        ways with
          highway ~ path|footway|cycleway
          and (
            ~footway|cycleway|path ~ sidewalk|sidepath
            or is_sidepath = yes
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

    override val enabledInCountries = NoCountriesExcept(
        "DE", // https://www.gesetze-im-internet.de/stvo_2013/__2.html, https://www.bonn.de/themen-entdecken/verkehr-mobilitaet/radverkehr-regeln-und-rechte.php
        "DK", // https://www.retsinformation.dk/eli/lta/2026/118/pdf
        "NL", // https://wiki.openstreetmap.org/wiki/NL:Cycleway
        "FI", // https://www.liikenneturva.fi/en/road-safety/cycling, https://www.traficom.fi/files/media/publication/Traficom_Py%C3%B6r%C3%A4ilij%C3%A4n_Liikennes%C3%A4%C3%A4nt%C3%B6opas_FI.pdf
        "NO", // https://www.vegvesen.no/en/traffic-information/traffic-safety/norwegian-traffic-rules/traffic-rules-for-cyclists/
        "BE", // https://www.politie.be/5353/vragen/verkeerstips/mag-je-op-een-fietspad-steeds-in-twee-richtingen-rijden
        "HR"  // https://www.zakon.hr/z/78/zakon-o-sigurnosti-prometa-na-cestama
    )

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
        tags[key] = answer.osmValue
    }
}

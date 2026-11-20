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
        "DE", // StVO § 2(4): left-hand cycle paths without signs 237, 240 or 241 require the supplementary sign "Radverkehr frei".
        // https://www.gesetze-im-internet.de/stvo_2013/__2.html#:~:text=Linke%20Radwege%20ohne%20die%20Zeichen%20237%2C%20240%20oder%20241,durch%20das%20allein%20stehende%20Zusatzzeichen%20%E2%80%9ERadverkehr%20frei%E2%80%9C%20angezeigt%20ist.

        // Bonn: "As a rule, cycle paths may only be used in one direction on the right-hand side of the road, unless use in both directions is explicitly signposted."
        // https://www.bonn.de/themen-entdecken/verkehr-mobilitaet/radverkehr-regeln-und-rechte.php?loc=en#:~:text=Separate%20cycle%20path%20and%20footpath

        "DK", // Færdselsloven § 14(2): cyclists may only use the cycle path on the right side of the road in the direction of traffic, unless otherwise indicated by markings.
        // https://www.retsinformation.dk/eli/lta/2026/118#:~:text=Cyklist%20og%20f%C3%B8rer%20af%20lille%20knallert%20m%C3%A5%20kun%20anvende%20cykelsti%20i%20h%C3%B8jre%20side%20af%20vejen%20i%20f%C3%A6rdselsretningen%2C%20medmindre%20andet%20er%20tilkendegivet%20ved%20afm%C3%A6rkning.

        "NL", // NL:Cycleway, "Een- of tweerichtingspad": one-way is the default for cycle paths alongside roads under Dutch law.
        // https://wiki.openstreetmap.org/wiki/NL:Tag:highway=cycleway#:~:text=Volgens%20de%20Nederlandse%20wet%20is%20dit%20voor%20fietspaden%20langs%20wegen%20de%20standaard.

        "FI", // Road Traffic Act 729/2018, sign D5: a two-way cycle path is indicated by supplementary sign H23.2 or H9.1.
        // https://www.finlex.fi/fi/lainsaadanto/2018/729#:~:text=Kaksisuuntainen%20py%C3%B6r%C3%A4tie%20osoitetaan%20lis%C3%A4kilvell%C3%A4%20H23.2%20tai%20H9.1

        "BE", // Police guidance: D7/D9/D10 signage must be visible in both directions for a two-way path.
        // https://www.politie.be/5353/vragen/verkeerstips/mag-je-op-een-fietspad-steeds-in-twee-richtingen-rijden#:~:text=Een%20fietspad%20kan%20ook%20worden%20aangeduid%20met%20het%20verkeersbord%20D7%2C%20D9%20of%20D10.,In%20het%20andere%20geval%20mag%20je%20dus%20niet%20aan%20de%20linkerkant%20van%20de%20rijbaan%20fietsen.

        "HR"  // Road Traffic Safety Act, Article 47(1): roadside cycle paths follow the direction of travel; two-way paths must be arranged and marked for bidirectional cycling.
        // https://www.zakon.hr/z/78/zakon-o-sigurnosti-prometa-na-cestama#:~:text=Vozilo%20se%20kre%C4%87e%20desnom%20stranom%20kolnika,desnom%20stranom%20trake%20ili%20staze%20u%20smjeru%20kretanja.
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

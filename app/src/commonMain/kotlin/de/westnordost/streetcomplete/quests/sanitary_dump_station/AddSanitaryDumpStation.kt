package de.westnordost.streetcomplete.quests.sanitary_dump_station

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddSanitaryDumpStation : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
         (
           tourism = caravan_site
           or tourism = camp_site and caravans = yes and !backcountry
         )
         and access !~ private|no
         and !sanitary_dump_station
    """

    override val changesetComment = "Specify if there is a sanitary dump station at camp or caravan site"
    override val wikiLink = "Key:sanitary_dump_station"
    override val icon = Res.drawable.quest_poo
    override val title = Res.string.quest_sanitary_dump_station_title
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)
    override val hint = Res.string.quest_sanitary_dump_station_description
    override val hintImages = listOf(
        Res.drawable.sanitary_dump_station_sign1,
        Res.drawable.sanitary_dump_station_sign2
    )

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["sanitary_dump_station"] = answer.toYesNo()
    }
}

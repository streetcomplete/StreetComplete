package de.westnordost.streetcomplete.quests.hairdresser

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddHairdresserCustomers : OsmFilterQuestType<HairdresserCustomers>() {

    override val elementFilter = """
        nodes, ways with
          (
              shop = hairdresser
              and hairdresser != barber
              and !female and !male
              and !male:signed and !female:signed
          )
    """
    override val changesetComment = "Survey hairdresser's customers"
    override val wikiLink = "Tag:shop=hairdresser"
    override val icon = Res.drawable.quest_hairdresser
    override val title = Res.string.quest_hairdresser_title
    override val achievements = listOf(CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(onAnswer: (QuestAnswer<HairdresserCustomers>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            items = HairdresserCustomers.entries,
            itemContent = { Text(stringResource(it.text)) },
            onAnswer = onAnswer
        )
    }

    override fun applyAnswerTo(answer: HairdresserCustomers, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == HairdresserCustomers.NOT_SIGNED) {
            tags["male:signed"] = "no"
            tags["female:signed"] = "no"
        } else {
            if (answer.isMale) tags["male"] = "yes"
            if (answer.isFemale) tags["female"] = "yes"
        }
        tags.remove("unisex")
    }
}

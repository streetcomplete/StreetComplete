package de.westnordost.streetcomplete.quests.powerpoles_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPowerPolesMaterial : OsmFilterQuestType<PowerPolesMaterialAnswer>() {

    override val elementFilter = """
        nodes with
          (power = pole or man_made = utility_pole)
          and !material
    """
    override val changesetComment = "Specify power poles material type"
    override val wikiLink = "Tag:power=pole"
    override val icon = Res.drawable.quest_power
    override val title = Res.string.quest_powerPolesMaterial_title
    override val achievements = listOf(BUILDING)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        // and also show the (power) lines themselves
        mapData.filter("nodes with power = pole or man_made = utility_pole") +
        mapData.filter("ways with power ~ line|minor_line or communication = line or telecom = line")

    // map data density is usually lower where there are power poles and more context is necessary
    // when looking at them from afar
    override val highlightedElementsRadius get() = 100.0

    @Composable
    override fun Form(onAnswer: (QuestAnswer<PowerPolesMaterialAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = PowerPolesMaterial.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onAnswer = onAnswer,
            otherAnswers = listOf(
                AnswerItem(stringResource(Res.string.quest_powerPolesMaterial_is_terminal)) {
                    onAnswer(Answer(PowerLineAnchoredToBuilding))
                }
            )
        )
    }

    override fun applyAnswerTo(answer: PowerPolesMaterialAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer is PowerPolesMaterial) {
            tags["material"] = answer.osmValue
        } else if (answer is PowerLineAnchoredToBuilding) {
            tags["power"] = "terminal"
        }
    }
}

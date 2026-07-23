package de.westnordost.streetcomplete.quests.power_attachment

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPowerAttachment : OsmFilterQuestType<PowerAttachment>() {

    override val elementFilter = """
        nodes with
          power ~ tower|pole|insulator
          and !line_attachment
          and disused != yes and ruined != yes and abandoned != yes
    """
    override val changesetComment = "Specify line_attachment power support"
    override val wikiLink = "Key:line_attachment"
    override val icon = Res.drawable.quest_power
    override val title = Res.string.quest_powerAttachment_title
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        // and also show the (power) lines themselves
        mapData.filter("nodes with power ~ tower|pole|insulator") +
        mapData.filter("ways with power ~ line|minor_line")

    // map data density is usually lower where there are power poles and more context is necessary
    // when looking at them from afar
    override val highlightedElementsRadius get() = 100.0

    @Composable
    override fun Form(on: (QuestAction<PowerAttachment>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = PowerAttachment.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        )
    }

    override fun applyAnswerTo(answer: PowerAttachment, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["line_attachment"] = answer.osmValue
    }
}

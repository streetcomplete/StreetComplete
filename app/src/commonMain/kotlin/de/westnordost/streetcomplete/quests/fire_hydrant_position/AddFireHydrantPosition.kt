package de.westnordost.streetcomplete.quests.fire_hydrant_position

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFireHydrantPosition : OsmFilterQuestType<FireHydrantPosition>() {

    override val elementFilter = """
        nodes with
         emergency = fire_hydrant and
         (!fire_hydrant:position or fire_hydrant:position ~ "\?|fixme") and
         (fire_hydrant:type = pillar or fire_hydrant:type = underground)
    """
    override val changesetComment = "Specify fire hydrant positions"
    override val wikiLink = "Tag:emergency=fire_hydrant"
    override val icon = Res.drawable.quest_fire_hydrant_grass
    override val title = Res.string.quest_fireHydrant_position_title
    override val achievements = listOf(LIFESAVER)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with emergency = fire_hydrant")

    @Composable
    override fun Form(onAnswer: (QuestAnswer<FireHydrantPosition>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = FireHydrantPosition.entries,
            itemsPerRow = 2,
            itemContent = { item ->
                val isPillar = element.tags["fire_hydrant:type"] == "pillar"
                ImageWithLabel(painterResource(item.getIcon(isPillar)), stringResource(item.title))
            },
            onAnswer = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: FireHydrantPosition, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fire_hydrant:position"] = answer.osmValue
    }
}

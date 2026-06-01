package de.westnordost.streetcomplete.quests.fire_hydrant

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFireHydrantType : OsmFilterQuestType<FireHydrantType>() {

    override val elementFilter = "nodes with emergency = fire_hydrant and !fire_hydrant:type"
    override val changesetComment = "Specify fire hydrant types"
    override val wikiLink = "Tag:emergency=fire_hydrant"
    override val icon = Res.drawable.quest_fire_hydrant
    override val title = Res.string.quest_fireHydrant_type_title
    override val achievements = listOf(LIFESAVER)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with emergency = fire_hydrant")

    @Composable
    override fun Form(on: (QuestAction<FireHydrantType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = FireHydrantType.entries,
            itemsPerRow = 2,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            on = on,
        )
    }

    override fun applyAnswerTo(answer: FireHydrantType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fire_hydrant:type"] = answer.osmValue
    }
}

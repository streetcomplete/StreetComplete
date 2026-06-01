package de.westnordost.streetcomplete.quests.recycling

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.OVERGROUND_CONTAINER
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.RECYCLING_CENTRE
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.UNDERGROUND_CONTAINER
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddRecyclingType : OsmFilterQuestType<RecyclingType>() {

    override val elementFilter = "nodes, ways with amenity = recycling and !recycling_type"
    override val changesetComment = "Specify type of recycling amenities"
    override val wikiLink = "Key:recycling_type"
    override val icon = Res.drawable.quest_recycling
    override val title = Res.string.quest_recycling_type_title
    override val achievements = listOf(CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity ~ recycling|waste_disposal|waste_basket")

    @Composable
    override fun Form(on: (QuestAction<RecyclingType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = RecyclingType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            on = on,
        )
    }

    override fun applyAnswerTo(answer: RecyclingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            RECYCLING_CENTRE -> {
                tags["recycling_type"] = "centre"
            }
            OVERGROUND_CONTAINER -> {
                tags["recycling_type"] = "container"
                tags["location"] = "overground"
            }
            UNDERGROUND_CONTAINER -> {
                tags["recycling_type"] = "container"
                tags["location"] = "underground"
            }
        }
    }
}

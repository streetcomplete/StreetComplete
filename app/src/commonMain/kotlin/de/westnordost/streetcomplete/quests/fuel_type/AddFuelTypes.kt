package de.westnordost.streetcomplete.quests.fuel_type

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFuelTypes : OsmFilterQuestType<Set<FuelTypes>>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = fuel
          and access !~ private|no
          and !fuel:cng
          and !fuel:diesel
          and !fuel:e10
          and !fuel:e85
          and !fuel:lpg
          and !fuel:octane_85
          and !fuel:octane_87
          and !fuel:octane_89
          and !fuel:octane_91
          and !fuel:octane_92
          and !fuel:octane_95
          and !fuel:octane_98
    """

    override val changesetComment = "Specify fuel types that are available"
    override val wikiLink = "Key:fuel:*"
    override val icon = Res.drawable.quest_fuel_self_service
    override val title = Res.string.quest_fuel_types_title
    override val achievements = listOf(CAR)

    @Composable
    override fun Form(on: (QuestAction<Set<FuelTypes>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val initialSelectedItems = remember(element) {
            parseAddFuelTypes(element.tags)
        }

        ItemsSelectQuestForm(
            on = on,
            items = FuelTypes.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            initialSelectedItems = initialSelectedItems,
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes, ways, relations with
              amenity ~ fuel
        """)

    override fun applyAnswerTo(answer: Set<FuelTypes>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.forEach { tags["fuel:${it.value}"] = "yes" }
        tags.updateCheckDate()
    }
}

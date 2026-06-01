package de.westnordost.streetcomplete.quests.drinking_water

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddDrinkingWater : OsmFilterQuestType<DrinkingWater>() {

    override val elementFilter = """
        nodes, ways with (
          man_made = water_tap
          or man_made = water_well
          or natural = spring
          or amenity = fountain and fountain = stone_block
        )
        and access !~ private|no and indoor != yes
        and !drinking_water
        and !drinking_water:legal
        and drinking_water:signed != no
        and drinking_water:legal:signed != no
        and amenity != drinking_water
        and (!intermittent or intermittent = no)
        and (!seasonal or seasonal = no)
        and (!disused or disused = no)
        and (!ruins or ruins = no)
    """
    override val changesetComment = "Specify whether water is drinkable"
    override val wikiLink = "Key:drinking_water"
    override val icon = Res.drawable.quest_drinking_water
    override val title = Res.string.quest_drinking_water_title2
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes with
             (
                 man_made = water_tap
                 or man_made = water_well
                 or natural = spring
                 or amenity = drinking_water
             )
             and access !~ private|no
        """)

    @Composable
    override fun Form(on: (QuestAction<DrinkingWater>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            items = DrinkingWater.entries,
            itemContent = { Text(stringResource(it.text)) },
            on = on
        )
    }

    override fun applyAnswerTo(answer: DrinkingWater, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            DrinkingWater.POTABLE_SIGNED -> {
                tags["drinking_water"] = "yes"
                tags["drinking_water:legal"] = "yes"
            }
            DrinkingWater.NOT_POTABLE_SIGNED -> {
                tags["drinking_water"] = "no"
                tags["drinking_water:legal"] = "no"
            }
            DrinkingWater.UNSIGNED -> {
                tags["drinking_water:signed"] = "no"
            }
        }
    }
}

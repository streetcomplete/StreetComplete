package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddDrinkingWater : OsmFilterQuestType<DrinkingWater>() {

    override val elementFilter = """
        nodes with (
          man_made = water_tap
          or man_made = water_well
          or natural = spring
        )
        and access !~ private|no and indoor != yes
        and !drinking_water and !drinking_water:legal and amenity != drinking_water
        and (!seasonal or seasonal = no)
    """
    override val changesetComment = "Specify whether water is drinkable"
    override val wikiLink = "Key:drinking_water"
    override val icon = R.drawable.ic_quest_drinking_water
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_drinking_water_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes with
             (
                 man_made = water_tap
                 or man_made = water_well
                 or natural = spring
                 or amenity = drinking_water
             )
             and access !~ private|no
        """)

    override fun createForm() = AddDrinkingWaterForm()

    override fun applyAnswerTo(answer: DrinkingWater, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["drinking_water"] = answer.osmValue
        answer.osmLegalValue?.let { tags["drinking_water:legal"] = it }
    }
}

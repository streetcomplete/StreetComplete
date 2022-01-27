package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

class AddDrinkingWater : OsmFilterQuestType<DrinkingWater>() {

    override val elementFilter = """
        nodes with (
          man_made = water_tap
          or man_made = water_well
          or natural = spring
        )
        and access !~ private|no and indoor != yes
        and !drinking_water and !drinking_water:legal and amenity != drinking_water
    """

    override val changesetComment = "Add whether water is drinkable"
    override val wikiLink = "Key:drinking_water"
    override val icon = R.drawable.ic_quest_drinking_water
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_drinking_water_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>) =
        arrayOf(featureName.value.toString())

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes with
             man_made = water_tap
             or man_made = water_well
             or natural = spring
             or amenity = drinking_water
        """)

    override fun createForm() = AddDrinkingWaterForm()

    override fun applyAnswerTo(answer: DrinkingWater, tags: Tags, timestampEdited: Long) {
        tags["drinking_water"] = answer.osmValue
        answer.osmLegalValue?.let { tags["drinking_water:legal"] = it }
    }
}

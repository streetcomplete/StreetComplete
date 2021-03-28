package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

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

    override val commitMessage = "Add whether water is drinkable"
    override val wikiLink = "Key:drinking_water"
    override val icon = R.drawable.ic_quest_drinking_water

    override fun getTitle(tags: Map<String, String>) = R.string.quest_drinking_water_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>) =
        arrayOf(featureName.value.toString())

    override fun createForm() = AddDrinkingWaterForm()

    override fun applyAnswerTo(answer: DrinkingWater, changes: StringMapChangesBuilder) {
        changes.addOrModify("drinking_water", answer.osmValue)
        answer.osmLegalValue?.let { changes.addOrModify("drinking_water:legal", it) }
    }
}

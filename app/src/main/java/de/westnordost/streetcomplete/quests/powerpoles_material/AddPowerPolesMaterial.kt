package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

class AddPowerPolesMaterial : OsmFilterQuestType<PowerPolesMaterial>() {

    override val elementFilter = "nodes with power = pole and !material"
    override val commitMessage = "Add powerpoles material type"
    override val wikiLink = "Tag:power=pole"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_powerPolesMaterial_title

    override fun createForm() = AddPowerPolesMaterialForm()

    override fun applyAnswerTo(answer: PowerPolesMaterial, changes: StringMapChangesBuilder) {
        changes.add("material", answer.osmValue)
    }
}

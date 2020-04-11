package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddPowerPolesMaterial(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes with power = pole and !material"
    override val commitMessage = "Add powerpoles material type"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_powerPolesMaterial_title

    override fun createForm() = AddPowerPolesMaterialForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("material", answer)
    }
}

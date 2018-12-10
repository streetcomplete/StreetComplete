package de.westnordost.streetcomplete.quests.powerpoles_material

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddPowerPolesMaterial(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes with power=pole and !material"
    override val commitMessage = "Add powerpoles material type"
    override val icon = R.drawable.ic_quest_power

	override fun getTitle(tags: Map<String, String>) = R.string.quest_powerPolesMaterial_title

	override fun createForm() = AddPowerPolesMaterialForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(AddPowerPolesMaterialForm.OSM_VALUES)
        if (values != null && values.size == 1) {
            changes.add("material", values[0])
        }
    }

}

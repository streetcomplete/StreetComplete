package de.westnordost.streetcomplete.quests.building_underground

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddIsBuildingUnderground(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "ways, relations with building and !location and layer~-[0-9]+"
    override val commitMessage = "Determine whatever building is fully underground"
    override val icon = R.drawable.ic_quest_building_underground

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        return if (hasName)
            R.string.quest_building_underground_name_title
        else
            R.string.quest_building_underground_title
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val location = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "underground" else "surface"
        changes.add("location", location)
    }
}

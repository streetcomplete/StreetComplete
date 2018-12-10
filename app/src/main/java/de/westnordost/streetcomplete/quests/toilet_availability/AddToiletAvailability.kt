package de.westnordost.streetcomplete.quests.toilet_availability

import android.os.Bundle

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletAvailability @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override// only for malls, big stores and rest areas because users should not need to go inside a non-public
    // place to solve the quest. (Considering malls and department stores public enough)
    val tagFilters: String
        get() = "nodes, ways with ( (shop ~ mall|department_store and name) or (highway ~ services|rest_area) ) and !toilets"

    override val commitMessage: String
        get() = "Add toilet availability"
    override val icon: Int
        get() = R.drawable.ic_quest_toilets

    override fun createForm(): AbstractQuestAnswerFragment {
        return YesNoQuestAnswerFragment()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("toilets", yesno)
    }

    override fun getTitle(tags: Map<String, String>): Int {
        val isRestArea = "rest_area" == tags["highway"] || "services" == tags["highway"]

        return if (isRestArea)
            R.string.quest_toiletAvailability_rest_area_title
        else
            R.string.quest_toiletAvailability_name_title
    }
}

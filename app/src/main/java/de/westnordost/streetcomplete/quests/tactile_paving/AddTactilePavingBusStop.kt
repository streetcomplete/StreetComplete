package de.westnordost.streetcomplete.quests.tactile_paving

import android.os.Bundle

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTactilePavingBusStop @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = "nodes with (public_transport=platform or (highway=bus_stop and public_transport!=stop_position)) and !tactile_paving"

    override val commitMessage: String
        get() = "Add tactile pavings on bus stops"
    override val icon: Int
        get() = R.drawable.ic_quest_blind_bus

    override// See overview here: https://ent8r.github.io/blacklistr/?java=tactile_paving/AddTactilePavingCrosswalk.java
    // #750
    val enabledForCountries: Countries
        get() = AddTactilePavingCrosswalk.ENBABLED_FOR_COUNTRIES

    override fun createForm(): AbstractQuestAnswerFragment {
        return TactilePavingForm()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("tactile_paving", yesno)
    }

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        return if (hasName)
            R.string.quest_tactilePaving_title_name_bus
        else
            R.string.quest_tactilePaving_title_bus
    }
}

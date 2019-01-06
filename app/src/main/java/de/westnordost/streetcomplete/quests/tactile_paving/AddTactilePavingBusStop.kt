package de.westnordost.streetcomplete.quests.tactile_paving

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTactilePavingBusStop(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes with
        (public_transport=platform or (highway=bus_stop and public_transport!=stop_position))
        and !tactile_paving
    """
    override val commitMessage = "Add tactile pavings on bus stops"
    override val icon = R.drawable.ic_quest_blind_bus

    // See overview here: https://ent8r.github.io/blacklistr/?java=tactile_paving/AddTactilePavingCrosswalk.kt
    // #750
    override val enabledForCountries = AddTactilePavingCrosswalk.ENBABLED_FOR_COUNTRIES

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_tactilePaving_title_name_bus
        else
            R.string.quest_tactilePaving_title_bus

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("tactile_paving", yesno)
    }
}

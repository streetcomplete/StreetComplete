package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddTactilePavingBusStop(o: OverpassMapDataDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes, ways with
        ((public_transport = platform and (bus = yes or trolleybus = yes or tram = yes)) 
        or 
        (highway = bus_stop and public_transport != stop_position))
        and !tactile_paving
    """
    override val commitMessage = "Add tactile pavings on bus stops"
    override val icon = R.drawable.ic_quest_blind_bus

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=tactile_paving/AddTactilePavingCrosswalk.kt
    // #750
    override val enabledForCountries = AddTactilePavingCrosswalk.ENABLED_FOR_COUNTRIES

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isTram = tags["tram"] == "yes"
        return if (isTram) {
            if (hasName) R.string.quest_tactilePaving_title_name_tram
            else         R.string.quest_tactilePaving_title_tram
        } else {
            if (hasName) R.string.quest_tactilePaving_title_name_bus
            else         R.string.quest_tactilePaving_title_bus
        }
    }

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("tactile_paving", if (answer) "yes" else "no")
    }
}

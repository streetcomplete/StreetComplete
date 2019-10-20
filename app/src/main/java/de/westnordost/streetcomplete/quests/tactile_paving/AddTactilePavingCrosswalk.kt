package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddTactilePavingCrosswalk(o: OverpassMapDataDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = "nodes with highway = crossing and !tactile_paving and foot != no"
    override val commitMessage = "Add tactile pavings on crosswalks"
    override val icon = R.drawable.ic_quest_blind_pedestrian_crossing

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=tactile_paving/AddTactilePavingCrosswalk.kt
    // #750
    override val enabledForCountries = ENABLED_FOR_COUNTRIES

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactilePaving_title_crosswalk

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("tactile_paving", if (answer) "yes" else "no")
    }

    companion object {
        internal val ENABLED_FOR_COUNTRIES = Countries.noneExcept(
            // Europe
            "NO","SE",
            "GB","IE","NL","BE","FR","ES",
            "DE","PL","CZ","SK","HU","AT","CH",
            "LV","LT","EE","RU",
            // America
            "US","CA","AR",
            // Asia
            "HK","SG","KR","JP",
            // Oceania
            "AU","NZ"
        )
    }
}

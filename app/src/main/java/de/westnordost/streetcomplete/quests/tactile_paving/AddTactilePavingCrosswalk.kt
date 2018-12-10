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

class AddTactilePavingCrosswalk @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = "nodes with highway=crossing and !tactile_paving and foot!=no"

    override val commitMessage: String
        get() = "Add tactile pavings on crosswalks"
    override val icon: Int
        get() = R.drawable.ic_quest_blind_pedestrian_crossing

    override// See overview here: https://ent8r.github.io/blacklistr/?java=tactile_paving/AddTactilePavingCrosswalk.java
    // #750
    val enabledForCountries: Countries
        get() = ENBABLED_FOR_COUNTRIES

    override fun createForm(): AbstractQuestAnswerFragment {
        return TactilePavingForm()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("tactile_paving", yesno)
    }

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_tactilePaving_title_crosswalk
    }

    companion object {

        internal val ENBABLED_FOR_COUNTRIES = Countries.noneExcept(
            arrayOf(
                // Europe
                "NO",
                "SE",
                "GB",
                "IE",
                "NL",
                "BE",
                "FR",
                "ES",
                "DE",
                "PL",
                "CZ",
                "SK",
                "HU",
                "AT",
                "CH",
                "LV",
                "LT",
                "EE",
                "RU",
                // America
                "US",
                "CA",
                "AR",
                // Asia
                "HK",
                "SG",
                "KR",
                "JP",
                // Oceania
                "AU",
                "NZ"
            )
        )
    }
}

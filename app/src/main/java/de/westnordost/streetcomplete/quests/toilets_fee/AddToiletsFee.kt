package de.westnordost.streetcomplete.quests.toilets_fee

import android.os.Bundle

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletsFee @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = "nodes, ways with amenity = toilets and access !~ private|customers and !fee"

    override val commitMessage: String
        get() = "Add toilets fee"
    override val icon: Int
        get() = R.drawable.ic_quest_toilet_fee

    override fun createForm(): AbstractQuestAnswerFragment {
        return YesNoQuestAnswerFragment()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("fee", yesno)
    }

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_toiletsFee_title
    }
}

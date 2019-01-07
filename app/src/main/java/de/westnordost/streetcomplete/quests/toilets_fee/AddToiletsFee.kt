package de.westnordost.streetcomplete.quests.toilets_fee

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletsFee(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways with amenity = toilets and access !~ private|customers and !fee"
    override val commitMessage = "Add toilets fee"
    override val icon = R.drawable.ic_quest_toilet_fee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_toiletsFee_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("fee", yesno)
    }
}

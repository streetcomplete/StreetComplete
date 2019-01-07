package de.westnordost.streetcomplete.quests.bike_parking_cover

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBikeParkingCover(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes, ways with amenity=bicycle_parking and access !~ private|no and !covered
        and bicycle_parking !~ shed|lockers|building
    """
    override val commitMessage = "Add bicycle parkings cover"
    override val icon = R.drawable.ic_quest_bicycle_parking_cover

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_bicycleParkingCoveredStatus_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("covered", yesno)
    }
}

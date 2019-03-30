package de.westnordost.streetcomplete.quests.bike_parking_cover

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBikeParkingCover(o: OverpassMapDataDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes, ways with amenity=bicycle_parking and access !~ private|no
        and bicycle_parking !~ shed|lockers|building
    """
    //maybe use something more generic, rather than assuming that single tag is always added?
    override val addedKey = "covered"
    override val addedValues = {true: "yes", false: "no"}
    override val commitMessage = "Add bicycle parkings cover"
    override val icon = R.drawable.ic_quest_bicycle_parking_cover

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_bicycleParkingCoveredStatus_title

    override fun createForm() = YesNoQuestAnswerFragment()
    override fun createRepeatedSurveyForm() = YesNoRepeatedQuestAnswerFragment(addedKey, addedValues)
}

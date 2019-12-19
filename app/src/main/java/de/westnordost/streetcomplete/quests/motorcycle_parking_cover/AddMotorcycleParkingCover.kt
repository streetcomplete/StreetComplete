package de.westnordost.streetcomplete.quests.motorcycle_parking_cover

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddMotorcycleParkingCover(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes, ways with amenity = motorcycle_parking
        and access !~ private|no
        and !covered
        and motorcycle_parking !~ shed|garage_boxes|building
    """
    override val commitMessage = "Add motorcycle parkings cover"
    override val icon = R.drawable.ic_quest_motorcycle_parking_cover

    override fun getTitle(tags: Map<String, String>) = R.string.quest_motorcycleParkingCoveredStatus_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("covered", if (answer) "yes" else "no")
    }
}

package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddMotorcycleParkingCapacity(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<Int>(o) {

    override val tagFilters = """
        nodes, ways with amenity = motorcycle_parking 
         and access !~ private|no
         and (!capacity or capacity older today -${r * 4} years)
    """
    override val commitMessage = "Add motorcycle parking capacities"
    override val wikiLink = "Tag:amenity=motorcycle_parking"
    override val icon = R.drawable.ic_quest_motorcycle_parking_capacity

    override fun getTitle(tags: Map<String, String>) = R.string.quest_motorcycleParkingCapacity_title

    override fun createForm() = AddMotorcycleParkingCapacityForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("capacity", answer.toString())
    }
}

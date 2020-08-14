package de.westnordost.streetcomplete.quests.bike_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddBikeParkingCapacity(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<Int>(o) {

    override val tagFilters = """
        nodes, ways with amenity = bicycle_parking
         and access !~ private|no
         and (
           !capacity
           or bicycle_parking ~ stands|wall_loops and capacity older today -${r * 4} years
         )
    """
    /* Bike capacity may change more often for stands and wheelbenders as adding or
       removing a few of them is minor work
     */

    override val commitMessage = "Add bicycle parking capacities"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking_capacity

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bikeParkingCapacity_title

    override fun createForm() = AddBikeParkingCapacityForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("capacity", answer.toString())
    }
}

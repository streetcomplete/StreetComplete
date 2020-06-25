package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi

class AddParkingType(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes, ways, relations with amenity = parking and !parking"
    override val commitMessage = "Add parking type"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = R.drawable.ic_quest_parking

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parkingType_title

    override fun createForm() = AddParkingTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("parking", answer)
    }
}

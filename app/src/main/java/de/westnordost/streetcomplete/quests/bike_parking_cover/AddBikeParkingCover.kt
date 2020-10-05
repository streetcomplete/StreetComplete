package de.westnordost.streetcomplete.quests.bike_parking_cover

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBikeParkingCover(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {
    override val tagFilters = """
        nodes, ways with 
         amenity = bicycle_parking
         and access !~ private|no
         and !covered
         and bicycle_parking !~ shed|lockers|building
    """
    override val commitMessage = "Add bicycle parkings cover"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking_cover

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_bicycleParkingCoveredStatus_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("covered", answer.toYesNo())
    }
}

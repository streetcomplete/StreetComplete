package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddFerryAccessPedestrian(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = "ways, relations with route = ferry and !foot"
    override val commitMessage = "Specify ferry access for pedestrians"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.ic_quest_ferry_pedestrian
    override val hasMarkersAtEnds = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        return if (hasName)
            R.string.quest_ferry_pedestrian_name_title
        else
            R.string.quest_ferry_pedestrian_title
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("foot", answer.toYesNo())
    }
}

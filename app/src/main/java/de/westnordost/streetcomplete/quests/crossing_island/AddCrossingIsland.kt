package de.westnordost.streetcomplete.quests.crossing_island

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddCrossingIsland(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes, ways with
          crossing
          and crossing != "island"
          and !crossing:island
    """
    override val commitMessage = "Add whether pedestrian crossing has an island"
    override val wikiLink = "Key:crossing:island"
    override val icon = R.drawable.ic_quest_pedestrian_crossing_island

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_pedestrian_crossing_island

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("crossing:island", if(answer) "yes" else "no")
    }
}

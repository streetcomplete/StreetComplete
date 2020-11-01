package de.westnordost.streetcomplete.quests.kerb_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.incline.AddInclineForm

class AddKerbType : OsmFilterQuestType<String>() {

    // TODO sst: adapt query or use different super class
    override val elementFilter = """
        nodes with highway = crossing
          and foot != no
          and (
            !crossing
            or crossing ~ island|unknown|yes
            or (
              crossing ~ traffic_signals|uncontrolled|zebra|marked|unmarked
              and crossing older today -8 years
            )
          )
    """

    override val commitMessage = "Add kerb"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.ic_placeholder_quest
    override val isSplitWayEnabled = false

    override fun getTitle(tags: Map<String, String>) = when { // TODO sst: change title
        tags["highway"] == "bridleway" -> R.string.quest_pathSurface_title_bridleway
        tags["highway"] == "steps"     -> R.string.quest_pathSurface_title_steps
        else                           -> R.string.quest_pathSurface_title
        // rest is rather similar, can be called simply "path"
    }

    override fun createForm(): AddInclineForm = AddInclineForm() // TODO sst: change form

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("kerb", answer)
    }
}

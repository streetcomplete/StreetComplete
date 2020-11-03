package de.westnordost.streetcomplete.quests.incline;

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

class AddIncline : OsmFilterQuestType<String>() {

    override val elementFilter = """
        ways with highway ~ path|footway
        and access !~ private|no
        and (!conveying or conveying = no) and (!indoor or indoor = no)
        and (!incline or incline = no)
    """

    override val commitMessage = "Add incline"
    override val wikiLink = "Key:incline"
    override val icon = R.drawable.ic_quest_incline
    override val isSplitWayEnabled = false

    override fun getTitle(tags: Map<String, String>) = R.string.quest_path_incline

    override fun createForm(): AddInclineForm = AddInclineForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("incline", answer)
    }
}

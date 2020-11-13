package de.westnordost.streetcomplete.quests.width

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

class AddWidth : OsmFilterQuestType<WidthAnswer>() {

    override val elementFilter = """
        ways with highway ~ path|footway
        and access !~ private|no
        and (!conveying or conveying = no) and (!indoor or indoor = no)
        and (!width or width = no)
    """

    override val commitMessage = "Add width"
    override val wikiLink = "Key:width"
    override val icon = R.drawable.ic_quest_street_width
    override val isSplitWayEnabled = false

    override fun getTitle(tags: Map<String, String>) = R.string.quest_path_width_title

    override fun createForm(): AddWidthForm = AddWidthForm()

    override fun applyAnswerTo(answer: WidthAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is SimpleWidthAnswer -> {
                changes.updateWithCheckDate("width", answer.value)
                changes.deleteIfExists("source:width")
            }
            is SidewalkWidthAnswer -> {
                if (answer.leftSidewalkValue != null) {
                    changes.updateWithCheckDate("sidewalk:left:width", answer.leftSidewalkValue!!)
                    changes.deleteIfExists("source:sidewalk:left:width")
                }
                if (answer.rightSidewalkValue != null) {
                    changes.updateWithCheckDate("sidewalk:right:width", answer.rightSidewalkValue!!)
                    changes.deleteIfExists("source:sidewalk:right:width")
                }
            }
        }
    }
}

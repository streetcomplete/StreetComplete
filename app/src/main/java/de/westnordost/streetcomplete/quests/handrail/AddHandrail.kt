package de.westnordost.streetcomplete.quests.handrail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddHandrail : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and (
           !handrail and !handrail:center and !handrail:left and !handrail:right
           or handrail = no and handrail older today -4 years
           or handrail older today -8 years
           or older today -8 years
         )
    """

    override val commitMessage = "Add whether steps have a handrail"
    override val wikiLink = "Key:handrail"
    override val icon = R.drawable.ic_quest_steps_handrail

    override fun getTitle(tags: Map<String, String>) = R.string.quest_handrail_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("handrail", answer.toYesNo())
        if (!answer) {
            changes.deleteIfExists("handrail:left")
            changes.deleteIfExists("handrail:right")
            changes.deleteIfExists("handrail:center")
        }
    }
}

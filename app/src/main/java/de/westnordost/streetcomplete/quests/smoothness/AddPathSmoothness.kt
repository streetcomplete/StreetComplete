package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

class AddPathSmoothness : OsmFilterQuestType<String>() {

    override val elementFilter = """
        ways with highway ~ path|footway
        and segregated != yes
        and access !~ private|no
        and (!conveying or conveying = no) and (!indoor or indoor = no)
        and (
          !smoothness
          or smoothness ~ ${ANYTHING_UNPAVED.joinToString("|")} and smoothness older today -4 years
          or smoothness older today -8 years
        )
    """

    override val commitMessage = "Add path smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_smoothness
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_path_smoothness_title

    override fun createForm(): AddPathSmoothnessForm = AddPathSmoothnessForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("surface", answer)
    }
}

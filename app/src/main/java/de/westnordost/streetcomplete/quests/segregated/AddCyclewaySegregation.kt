package de.westnordost.streetcomplete.quests.segregated

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_PAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo

class AddCyclewaySegregation : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with
        (
          (highway = path and bicycle = designated and foot = designated)
          or (highway = footway and bicycle = designated)
          or (highway = cycleway and foot ~ designated|yes)
        )
        and surface ~ ${ANYTHING_PAVED.joinToString("|")}
        and area != yes
        and (!segregated or segregated older today -8 years)
    """

    override val commitMessage = "Add segregated status for combined footway with cycleway"
    override val wikiLink = "Key:segregated"
    override val icon = R.drawable.ic_quest_path_segregation

    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_segregated_title

    override fun createForm() = AddCyclewaySegregationForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("segregated", answer.toYesNo())
    }
}

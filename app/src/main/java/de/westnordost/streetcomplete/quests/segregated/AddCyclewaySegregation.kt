package de.westnordost.streetcomplete.quests.segregated

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment

class AddCyclewaySegregation(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        ways with
        (
         (highway = path and bicycle = designated and foot = designated)
         or (highway = footway and bicycle = designated)
         or (highway = cycleway and foot ~ designated|yes)
        )
        and !segregated and area != yes and surface ~ ${OsmTaggings.ANYTHING_PAVED.joinToString("|")}
    """

    override val commitMessage = "Add segregated status for combined footway with cycleway"
    override val icon = R.drawable.ic_quest_path_segregation

    override fun getTitle(tags: Map<String, String>) = R.string.quest_segregated_title

    override fun createForm() = AddCyclewaySegregationForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("segregated", answer.getStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES)!![0])
    }
}

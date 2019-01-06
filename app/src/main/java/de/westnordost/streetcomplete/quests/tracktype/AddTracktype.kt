package de.westnordost.streetcomplete.quests.tracktype

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment

class AddTracktype(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        ways with highway=track and !tracktype
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "Add tracktype"
    override val icon = R.drawable.ic_quest_tractor

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tracktype_title

    override fun createForm() = AddTracktypeForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("tracktype", answer.getStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES)!![0])
    }
}

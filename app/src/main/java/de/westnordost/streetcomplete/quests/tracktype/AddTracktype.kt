package de.westnordost.streetcomplete.quests.tracktype

import android.os.Bundle

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

class AddTracktype @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = "ways with highway=track and !tracktype" + " and (access !~ private|no or (foot and foot !~ private|no))"

    override val commitMessage: String
        get() = "Add tracktype"
    override val icon: Int
        get() = R.drawable.ic_quest_tractor

    override fun createForm(): AbstractQuestAnswerFragment {
        return AddTracktypeForm()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(AddTracktypeForm.OSM_VALUES)
        if (values != null && values.size == 1) {
            changes.add("tracktype", values[0])
        }
    }

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_tracktype_title
    }
}

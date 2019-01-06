package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment

class AddReligionToWaysideShrine(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways, relations with historic=wayside_shrine and !religion and (access !~ private|no)"
    override val commitMessage = "Add religion for wayside shrine"
    override val icon = R.drawable.ic_quest_religion

    override fun getTitle(tags: Map<String, String>) = R.string.quest_religion_for_wayside_shrine_title

    override fun createForm() = AddReligionToPlaceOfWorshipForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("religion", answer.getStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES)!![0])
    }
}

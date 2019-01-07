package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment

class AddReligionToPlaceOfWorship(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways, relations with amenity=place_of_worship and !religion and name"
    override val commitMessage = "Add religion for place of worship"
    override val icon = R.drawable.ic_quest_religion

    override fun getTitle(tags: Map<String, String>) = R.string.quest_religion_for_place_of_worship_title

    override fun createForm() = AddReligionToPlaceOfWorshipForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("religion", answer.getStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES)!![0])
    }
}

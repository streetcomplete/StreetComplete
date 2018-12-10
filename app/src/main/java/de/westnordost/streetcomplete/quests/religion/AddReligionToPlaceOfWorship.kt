package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddReligionToPlaceOfWorship(o: OverpassMapDataDao) : AbstractAddReligionToQuestType(o) {

    override val tagFilters = "nodes, ways, relations with amenity=place_of_worship and !religion and name"
    override val commitMessage = "Add religion for place of worship"
	override val icon = R.drawable.ic_quest_religion

    override fun getTitle(tags: Map<String, String>) = R.string.quest_religion_for_place_of_worship_title
}

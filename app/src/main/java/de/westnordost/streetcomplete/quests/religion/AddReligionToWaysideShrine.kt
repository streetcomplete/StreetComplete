package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddReligionToWaysideShrine(o: OverpassMapDataDao) : AbstractAddReligionToQuestType(o) {

    override val tagFilters = "nodes, ways, relations with historic=wayside_shrine and !religion and (access !~ private|no)"
    override val commitMessage = "Add religion for wayside shrine"
	override val icon = R.drawable.ic_quest_religion

    override fun getTitle(tags: Map<String, String>) = R.string.quest_religion_for_wayside_shrine_title
}

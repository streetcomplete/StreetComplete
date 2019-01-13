package de.westnordost.streetcomplete.quests.bus_stop_shelter

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBusStopShelter(o: OverpassMapDataDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes with (
        (public_transport=platform and (bus=yes or trolleybus=yes or tram=yes))
        or
        (highway=bus_stop and public_transport!=stop_position)
        ) and !shelter
    """
    override val commitMessage = "Add bus stop shelter"
    override val icon = R.drawable.ic_quest_bus_stop_shelter

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isTram = tags["tram"] == "yes"
        return if (isTram) {
            if (hasName)
                R.string.quest_busStopShelter_tram_name_title
            else
                R.string.quest_busStopShelter_tram_title
        } else {
            if (hasName)
                R.string.quest_busStopShelter_name_title
            else
                R.string.quest_busStopShelter_title
        }
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("shelter", if (answer) "yes" else "no")
    }
}

package de.westnordost.streetcomplete.quests.localized_name

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddBusStopName(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes with
        ((public_transport = platform and (bus = yes or trolleybus = yes or tram = yes))
        or
        (highway = bus_stop and public_transport != stop_position))
        and !name and noname != yes
    """

    override val commitMessage = "Determine bus/tram stop names"
    override val icon = R.drawable.ic_quest_bus_stop_name

    override fun getTitle(tags: Map<String, String>) =
        if (tags["tram"] == "yes")
            R.string.quest_tramStopName_title
        else
            R.string.quest_busStopName_title

    override fun createForm() = AddBusStopNameForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        if (answer.getBoolean(AddLocalizedNameForm.NO_NAME)) {
            changes.add("noname", "yes")
        } else {
            val nameByLanguage = answer.toNameByLanguage()
            for ((key, value) in nameByLanguage) {
                if (key.isEmpty()) {
                    changes.add("name", value)
                } else {
                    changes.add("name:$key", value)
                }
            }
        }
    }
}

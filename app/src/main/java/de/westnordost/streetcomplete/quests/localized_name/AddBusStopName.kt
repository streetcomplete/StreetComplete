package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddBusStopName(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<BusStopNameAnswer>(o) {

    override val tagFilters = """
        nodes with
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
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

    override fun applyAnswerTo(answer: BusStopNameAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoBusStopName -> {
                changes.add("noname", "yes")
            }
            is BusStopName -> {
                for ((languageCode, name) in answer.localizedNames) {
                    if (languageCode.isEmpty()) {
                        changes.addOrModify("name", name)
                    } else {
                        changes.addOrModify("name:$languageCode", name)
                    }
                }
            }
        }
    }
}

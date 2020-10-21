package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept

class AddBusStopName : OsmFilterQuestType<BusStopNameAnswer>() {

    override val elementFilter = """
        nodes with
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and !name and noname != yes and name:signed != no
    """

    override val enabledInCountries = AllCountriesExcept("US", "CA")
    override val commitMessage = "Determine bus/tram stop names"
    override val wikiLink = "Tag:public_transport=platform"
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
                changes.add("name:signed", "no")
            }
            is BusStopName -> {
                for ((languageTag, name) in answer.localizedNames) {
                    val key = when (languageTag) {
                        "" -> "name"
                        "international" -> "int_name"
                        else -> "name:$languageTag"
                    }
                    changes.addOrModify(key, name)
                }
            }
        }
    }
}

package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN

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
    override val changesetComment = "Determine bus/tram stop names"
    override val wikiLink = "Tag:public_transport=platform"
    override val icon = R.drawable.ic_quest_bus_stop_name

    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["tram"] == "yes")
            R.string.quest_tramStopName_title
        else
            R.string.quest_busStopName_title

    override fun createForm() = AddBusStopNameForm()

    override fun applyAnswerTo(answer: BusStopNameAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoBusStopName -> {
                tags["name:signed"] = "no"
            }
            is BusStopName -> {
                for ((languageTag, name) in answer.localizedNames) {
                    val key = when (languageTag) {
                        "" -> "name"
                        "international" -> "int_name"
                        else -> "name:$languageTag"
                    }
                    tags[key] = name
                }
            }
        }
    }
}

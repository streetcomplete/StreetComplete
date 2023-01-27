package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.applyTo

class AddBusStopName : OsmFilterQuestType<BusStopNameAnswer>() {

    override val elementFilter = """
        nodes with
        (
          public_transport = platform
          or (highway = bus_stop and public_transport != stop_position)
        )
        and !name and noname != yes and name:signed != no
    """

    override val enabledInCountries = AllCountriesExcept("US", "CA")
    override val changesetComment = "Determine public transport stop names"
    override val wikiLink = "Tag:public_transport=platform"
    override val icon = R.drawable.ic_quest_bus_stop_name
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_busStopName_title2

    override fun createForm() = AddBusStopNameForm()

    override fun applyAnswerTo(answer: BusStopNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoBusStopName -> {
                tags["name:signed"] = "no"
            }
            is BusStopName -> {
                answer.localizedNames.applyTo(tags)
            }
        }
    }
}

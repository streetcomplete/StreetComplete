package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.localized_name.applyTo

class AddBusStopName : OsmFilterQuestType<BusStopNameAnswer>(), AndroidQuest {

    // this filter needs to be kept somewhat in sync with the filter in AddBusStopNameForm https://github.com/streetcomplete/StreetComplete/issues/6390#issuecomment-3057235984
    override val elementFilter = """
        nodes, ways, relations with
        (
          public_transport = platform and bus = yes
          or highway = bus_stop and public_transport != stop_position
          or railway ~ halt|station|tram_stop
        )
        and access !~ no|private
        and !name and noname != yes and name:signed != no
    """

    override val enabledInCountries = AllCountriesExcept(
        "US", // https://github.com/streetcomplete/StreetComplete/issues/2126
        "CA", // https://github.com/streetcomplete/StreetComplete/issues/2126
        "SE" // https://github.com/streetcomplete/StreetComplete/issues/6390#issuecomment-3057235984
    )
    override val changesetComment = "Determine public transport stop names"
    override val wikiLink = "Tag:public_transport=platform"
    override val icon = R.drawable.quest_bus_stop_name
    override val achievements = listOf(PEDESTRIAN)

    override val hint = R.string.quest_stopName_hint

    override fun getTitle(tags: Map<String, String>) = R.string.quest_busStopName_title2

    override fun createForm() = AddBusStopNameForm()

    override fun applyAnswerTo(answer: BusStopNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            BusStopNameAnswer.NoName -> {
                tags["name:signed"] = "no"
            }
            is BusStopName -> {
                answer.localizedNames.applyTo(tags)
            }
        }
    }
}

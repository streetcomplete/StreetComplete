package de.westnordost.streetcomplete.quests.bus_stop_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN

class AddBusStopRef : OsmFilterQuestType<BusStopRefAnswer>() {

    override val elementFilter = """
        nodes with
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and !ref and noref != yes and ref:signed != no
    """

    override val enabledInCountries = NoCountriesExcept("US", "CA", "JE")
    override val changesetComment = "Determine bus/tram stop ref"
    override val wikiLink = "Tag:public_transport=platform"
    override val icon = R.drawable.ic_quest_bus_stop_name

    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["tram"] == "yes")
            R.string.quest_tramStopRef_title
        else
            R.string.quest_busStopRef_title

    override fun createForm() = AddBusStopRefForm()

    override fun applyAnswerTo(answer: BusStopRefAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoBusStopRef -> tags["ref:signed"] = "no"
            is BusStopRef ->   tags["ref"] = answer.ref
        }
    }
}

package de.westnordost.streetcomplete.quests.bus_stop_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept

class AddBusStopRef(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<BusStopRefAnswer>(o) {

    override val tagFilters = """
        nodes with
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and !ref and noref != yes and ref:signed != no
    """

    override val enabledInCountries = NoCountriesExcept("US", "CA")
    override val commitMessage = "Determine bus/tram stop ref"
    override val wikiLink = "Tag:public_transport=platform"
    override val icon = R.drawable.ic_quest_bus_stop_name

    override fun getTitle(tags: Map<String, String>) =
        if (tags["tram"] == "yes")
            R.string.quest_tramStopRef_title
        else
            R.string.quest_busStopRef_title

    override fun createForm() = AddBusStopRefForm()

    override fun applyAnswerTo(answer: BusStopRefAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoBusStopRef -> changes.add("ref:signed", "no")
            is BusStopRef ->   changes.add("ref", answer.ref)
        }
    }
}

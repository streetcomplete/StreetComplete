package de.westnordost.streetcomplete.quests.bus_stop_timetable_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAnyKey
import de.westnordost.streetcomplete.quests.bus_stop_timetable_type.BusStopTimetable.*

class AddBusStopTimetable : OsmFilterQuestType<BusStopTimetable>() {

    override val elementFilter = """
        nodes with
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and physically_present != no and naptan:BusStopType != HAR
        and (
            (!departures_board or departures_board older today -4 years)
            or
            (passenger_information_display != yes or passenger_information_display older today -4 years)
        )
    """

    override val commitMessage = "Add timetable type at bus stops"
    override val wikiLink = "Key:departures_board"
    override val icon = R.drawable.ic_quest_bus_stop_name

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsAnyKey("name", "ref")
        val isTram = tags["tram"] == "yes"
        return when {
            isTram && hasName ->    R.string.quest_busStopTimetable_tram_name_title
            isTram ->               R.string.quest_busStopTimetable_tram_title
            hasName ->              R.string.quest_busStopTimetable_name_title
            else ->                 R.string.quest_busStopTimetable_title
        }
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> =
        arrayOfNotNull(tags["name"] ?: tags["ref"])

    override fun createForm() = AddBusStopTimetableForm()

    override fun applyAnswerTo(answer: BusStopTimetable, changes: StringMapChangesBuilder) {
        when (answer) {
            PRINTED -> {
                changes.updateWithCheckDate("departures_board", "timetable")
                changes.updateWithCheckDate("passenger_information_display", "no")
            }
            DELAY -> {
                changes.updateWithCheckDate("departures_board", "delay")
                changes.updateWithCheckDate("passenger_information_display", "no")
            }
            REALTIME -> {
                changes.updateWithCheckDate("departures_board", "realtime")
                changes.updateWithCheckDate("passenger_information_display", "yes")
            }
            NONE -> {
                changes.updateWithCheckDate("departures_board", "no")
                changes.updateWithCheckDate("passenger_information_display", "no")
            }
        }
    }
}

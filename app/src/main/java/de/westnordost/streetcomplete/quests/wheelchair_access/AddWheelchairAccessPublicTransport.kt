package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddWheelchairAccessPublicTransport(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        nodes, ways, relations with (amenity = bus_station or railway ~ station|subway_entrance)
        and (
          !wheelchair
          or wheelchair != yes and wheelchair older today -${r * 4} years
          or wheelchair older today -${r * 8} years
        )
    """
    override val commitMessage = "Add wheelchair access to public transport platforms"
    override val wikiLink = "Key:wheelchair"
    override val icon = R.drawable.ic_quest_wheelchair

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val type: String = tags["amenity"] ?: tags["railway"] ?: ""

        return if (hasName) {
            when (type) {
                "bus_station"     -> R.string.quest_wheelchairAccess_bus_station_name_title
                "station"         -> R.string.quest_wheelchairAccess_railway_station_name_title
                "subway_entrance" -> R.string.quest_wheelchairAccess_subway_entrance_name_title
                else              -> R.string.quest_wheelchairAccess_location_name_title
            }
        } else {
            when (type) {
                "bus_station"     -> R.string.quest_wheelchairAccess_bus_station_title
                "station"         -> R.string.quest_wheelchairAccess_railway_station_title
                "subway_entrance" -> R.string.quest_wheelchairAccess_subway_entrance_title
                else              -> R.string.quest_wheelchairAccess_location_title
            }
        }
    }

    override fun createForm() = AddWheelchairAccessPublicTransportForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("wheelchair", answer)
    }
}

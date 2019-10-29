package de.westnordost.streetcomplete.quests.internet_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddInternetAccess(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        nodes, ways, relations with (
            amenity ~ library|restaurant|fast_food|cafe|pub|bar|biergarten|theatre|bus_station|community_centre|nightclub or 
            tourism ~ hotel|guest_house|hostel|motel|camp_site|museum or 
            leisure ~ marina|sports_centre|fitness_centre or
            shop ~ hairdresser|car_repair|department_store|supermarket|beauty|mall or
            public_transport ~ station
        )
        and !internet_access and !wifi and name
    """
    override val commitMessage = "Add internet access"
    override val icon = R.drawable.ic_quest_wifi
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_internet_access_name_title

    override fun createForm() = AddInternetAccessForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("internet_access", answer)
    }
}

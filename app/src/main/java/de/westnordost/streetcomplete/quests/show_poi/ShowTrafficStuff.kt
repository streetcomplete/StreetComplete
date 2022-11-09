package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class ShowTrafficStuff : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
         barrier and barrier !~ wall|fence|retaining_wall|hedge
         or traffic_calming
         or crossing
         or entrance
         or public_transport
         or highway ~ crossing|stop|give_way|elevator
         or amenity ~ taxi|parking|parking_entrance|motorcycle_parking
         or type = restriction
         """

    override val changesetComment = "Change crossing"
    override val wikiLink = "Key:traffic_calming"
    override val icon = R.drawable.ic_quest_poi_traffic
    override val dotColor = "deepskyblue"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_traffic

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_traffic_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        val args = if ((!tags["crossing"].isNullOrBlank() && !tags["traffic_calming"].isNullOrBlank())
                        || tags["type"] == "restriction"
                        || tags["highway"] == "elevator")
            tags.entries.toString()
        else
            ""
        return arrayOf(args)
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(filter)

    override fun createForm() = ShowTrafficStuffAnswerForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        if (answer)
            tags["traffic_calming"] = "table"
    }
}

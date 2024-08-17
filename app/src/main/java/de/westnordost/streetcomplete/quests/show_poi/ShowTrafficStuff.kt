package de.westnordost.streetcomplete.quests.show_poi

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.getLabelOrElementSelectionDialog
import de.westnordost.streetcomplete.quests.getLabelSources

class ShowTrafficStuff : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways with
         barrier and barrier !~ wall|fence|retaining_wall|hedge
         or traffic_calming
         or traffic_sign
         or crossing
         or entrance
         or public_transport
         or highway ~ crossing|stop|give_way|elevator|traffic_signals|turning_circle
         or amenity ~ taxi|parking|parking_entrance|motorcycle_parking
         """

    override val changesetComment = "Adjust traffic related elements"
    override val wikiLink = "Key:traffic_calming"
    override val icon = R.drawable.ic_quest_poi_traffic
    override val dotColor = "deepskyblue"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_traffic
    override val dotLabelSources = getLabelSources( "", this, prefs)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_poi_traffic_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(filter)

    override fun createForm() = ShowTrafficStuffAnswerForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer)
            tags["traffic_calming"] = "table"
    }

    override fun getQuestSettingsDialog(context: Context) = getLabelOrElementSelectionDialog(context, this, prefs)
}

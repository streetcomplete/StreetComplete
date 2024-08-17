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

class ShowRecycling : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          amenity ~ recycling|waste_basket|waste_disposal|waste_transfer_station|sanitary_dump_station
    """
    override val changesetComment = "Adjust recycling related elements"
    override val wikiLink = "Key:amenity=recycling"
    override val icon = R.drawable.ic_quest_poi_recycling
    override val dotColor = "green"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_recycling
    override val dotLabelSources = getLabelSources( "", this, prefs)

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_recycling_title

    override fun createForm() = ShowRecyclingAnswerForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer) {
            tags["amenity"] = "vending_machine"
            tags["vending"] = "excrement_bags"
            tags["bin"] = "yes"
        }
    }

    override fun getQuestSettingsDialog(context: Context) = getLabelOrElementSelectionDialog(context, this, prefs)
}

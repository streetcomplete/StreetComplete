package de.westnordost.streetcomplete.quests.show_poi

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment
import de.westnordost.streetcomplete.quests.getLabelOrElementSelectionDialog
import de.westnordost.streetcomplete.quests.getLabelSources

class ShowMachine : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways with
          amenity ~ vending_machine|atm|telephone|charging_station|device_charging_station|photo_booth
          or atm = yes and (amenity or shop)
    """
    override val changesetComment = "Adjust vending machine or similar"
    override val wikiLink = "Tag:amenity=vending_machine"
    override val icon = R.drawable.ic_quest_poi_machine
    override val dotColor = "blue"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_machine
    override val dotLabelSources = getLabelSources("vending", this, prefs)

    override fun getTitle(tags: Map<String, String>) =
        if (!tags["atm"].isNullOrEmpty() && tags["atm"] != "no")
            R.string.quest_poi_has_atm_title
        else if (tags["amenity"].equals("vending_machine"))
            R.string.quest_poi_vending_title
        else
            R.string.quest_poi_machine_title

    override fun createForm() = ShowMachineAnswerForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    override fun getQuestSettingsDialog(context: Context) = getLabelOrElementSelectionDialog(context, this, prefs)
}

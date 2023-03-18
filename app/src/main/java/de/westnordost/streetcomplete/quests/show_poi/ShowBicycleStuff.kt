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

class ShowBicycleStuff : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          amenity ~ bicycle_parking|bicycle_rental|bicycle_repair_station|compressed_air
    """
    override val changesetComment = "Adjust bicycle related elements"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.ic_quest_poi_bicycle
    override val dotColor = "mediumorchid"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_bike

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_cycling_title

    override fun createForm() = NoAnswerFragment()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    override fun getQuestSettingsDialog(context: Context) = getLabelOrElementSelectionDialog(context, this, prefs)
}

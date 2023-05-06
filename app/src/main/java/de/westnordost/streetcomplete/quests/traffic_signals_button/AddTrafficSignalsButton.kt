package de.westnordost.streetcomplete.quests.traffic_signals_button

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isCrossingWithTrafficSignals
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddTrafficSignalsButton : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes with
          crossing = traffic_signals
          and highway ~ crossing|traffic_signals
          and foot != no
          and !button_operated
    """
    override val changesetComment = "Specify whether traffic signals have a button for pedestrians"
    override val wikiLink = "Tag:highway=traffic_signals"
    override val icon = R.drawable.ic_quest_traffic_lights
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_button_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter { it.isCrossingWithTrafficSignals() }.asSequence()

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["button_operated"] = answer.toYesNo()
    }
}

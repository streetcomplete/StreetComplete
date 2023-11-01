package de.westnordost.streetcomplete.quests.via_ferrata_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddViaFerrataScale : OsmFilterQuestType<ViaFerrataScale>() {

    override val elementFilter = """
        ways with
          highway = via_ferrata
          and !via_ferrata_scale
    """
    override val changesetComment = "Specify Via Ferrata Grade Scale"
    override val wikiLink = "Key:via_ferrata_scale"
    override val icon = R.drawable.ic_quest_via_ferrata_scale
    override val defaultDisabledMessage = R.string.default_disabled_msg_viaFerrataScale

    override fun getTitle(tags: Map<String, String>) = R.string.quest_viaFerrataScale_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("ways with highway = via_ferrata")

    override fun createForm() = AddViaFerrataScaleForm()

    override fun applyAnswerTo(answer: ViaFerrataScale, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["via_ferrata_scale"] = answer.osmValue
    }
}

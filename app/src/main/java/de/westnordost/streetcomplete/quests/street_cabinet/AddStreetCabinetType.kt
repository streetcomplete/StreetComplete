package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddStreetCabinetType : OsmFilterQuestType<StreetCabinetType>() {

    override val elementFilter = """
        nodes, ways with
          man_made = street_cabinet
          and !street_cabinet
          and !utility
    """
    override val changesetComment = "Add street cabinet type"
    override val wikiLink = "Tag:man_made=street_cabinet"
    override val icon = R.drawable.ic_quest_street_cabinet
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_street_cabinet_type_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        val operator = tags["operator"]?.let { " ($it)" } ?: ""
        return arrayOf(operator)
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes, ways with
             (
                 man_made = street_cabinet
                 or building = service
             )
        """)

    override fun createForm() = AddStreetCabinetTypeForm()

    override fun applyAnswerTo(answer: StreetCabinetType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags[answer.osmKey] = answer.osmValue
    }
}

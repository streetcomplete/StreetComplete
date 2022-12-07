package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowSeating : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        amenity ~ bench|lounger|table
        or leisure ~ picnic_table|bleachers
        or tourism = picnic_site
    """
    override val changesetComment = "Adjust benches and similar"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = R.drawable.ic_quest_poi_seating
    override val dotColor = "chocolate"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_bench

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_seating_title

    override fun createForm() = NoAnswerFragment()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
    }
}

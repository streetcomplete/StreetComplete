package de.westnordost.streetcomplete.quests.swimming_pool_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddSwimmingPoolAvailability : OsmFilterQuestType<SwimmingPoolAvailability>() {

    override val elementFilter = """
        nodes, ways with
         (
           leisure = resort
           or (leisure = sports_hall and sport = swimming)
           or tourism ~ camp_site|hotel
         )
         and !swimming_pool
    """
    override val changesetComment = "Survey whether places have a swimming pool"
    override val wikiLink = "Key:swimming_pool"
    override val icon = R.drawable.ic_quest_swimming_pool
    override val isReplacePlaceEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_swimmingPoolAvailability_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
                nodes, ways with
                (
                   leisure ~ resort|swimming_pool
                   or (leisure = sports_hall and sport = swimming)
                   or tourism ~ camp_site|hotel
                 )
            """)

    override fun createForm() = AddSwimmingPoolAvailabilityForm()

    override fun applyAnswerTo(answer: SwimmingPoolAvailability, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("swimming_pool", answer.osmValue)
    }
}

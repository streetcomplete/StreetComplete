package de.westnordost.streetcomplete.quests.brewery

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace

class AddBrewery : OsmFilterQuestType<BreweryAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity ~ bar|biergarten|pub|restaurant|nightclub
          and drink:beer != no
          and (
            brewery ~ yes|no
            or !brewery
            or brewery older today -6 years
          )
    """
    override val changesetComment = "Add brewery"
    override val wikiLink = "Key:brewery"
    override val icon = R.drawable.ic_quest_brewery
    override val isReplacePlaceEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_brewery_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlace() }

    override fun createForm() = AddBreweryForm()

    override fun applyAnswerTo(answer: BreweryAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoBeerAnswer -> {
                tags["drink:beer"] = "no"
                if (tags["brewery"] != "no") // don't remove brewery=no
                    tags.remove("brewery")
            }
            is ManyBeersAnswer -> tags["brewery"] = "various"
            is BreweryStringAnswer -> tags["brewery"] = answer.brewery
        }
    }
}

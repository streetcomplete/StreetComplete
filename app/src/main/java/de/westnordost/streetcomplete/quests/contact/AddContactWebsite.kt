package de.westnordost.streetcomplete.quests.contact

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace

class AddContactWebsite : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or """.trimIndent() +
        PLACES_FOR_CONTACT_QUESTS +
        "\n) and !website and !contact:website and !contact:facebook and !contact:instagram and !brand and (name or operator)"

    override val changesetComment = "Add website"
    override val wikiLink = "Key:website"
    override val icon = R.drawable.ic_quest_website
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_contact_website

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlace() }

    override fun createForm() = AddContactWebsiteForm()

    override val isReplacePlaceEnabled: Boolean = true

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["website"] = answer
    }
}

package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags

class AddAddressStreet : OsmElementQuestType<AddressStreetAnswer> {

    private val filter by lazy { """
        nodes, ways, relations with
          (addr:housenumber or addr:housename) and !addr:street and !addr:place and !addr:block_number
          or addr:streetnumber and !addr:street
    """.toElementFilterExpression() }

    // #2112 - exclude indirect addr:street
    private val excludedWaysFilter by lazy { """
        ways with
          addr:street and addr:interpolation
    """.toElementFilterExpression() }

    override val changesetComment = "Add street/place names to address"
    override val icon = R.drawable.ic_quest_housenumber_street
    override val wikiLink = "Key:addr"
    // In Japan, housenumbers usually have block numbers, not streets
    override val enabledInCountries = AllCountriesExcept("JP")
    override val achievements = listOf(POSTMAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_street_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(excludedWayNodeIds) { it.nodeIds }

        val associatedStreetRelations = mapData.relations.filter {
            val type = it.tags["type"]
            type == "associatedStreet" || type == "street"
        }

        val addressesWithoutStreet = mapData.filter { address ->
            filter.matches(address)
            && associatedStreetRelations.none { it.contains(address.type, address.id) }
            && address.id !in excludedWayNodeIds
        }

        return addressesWithoutStreet
    }

    /* cannot be determined because of the associated street relations */
    override fun isApplicableTo(element: Element): Boolean? =
        if (!filter.matches(element)) false else null

    override fun createForm() = AddAddressStreetForm()

    override fun applyAnswerTo(answer: AddressStreetAnswer, tags: Tags, timestampEdited: Long) {
        val key = when (answer) {
            is StreetName -> "addr:street"
            is PlaceName -> "addr:place"
        }
        tags[key] = answer.name
    }
}

private fun Relation.contains(elementType: ElementType, elementId: Long): Boolean {
    return members.any { it.type == elementType && it.ref == elementId }
}

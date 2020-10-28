package de.westnordost.streetcomplete.quests.address

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionEntry
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.road_name.data.toRoadNameByLanguage

class AddAddressStreet(
        private val roadNameSuggestionsDao: RoadNameSuggestionsDao
) : OsmElementQuestType<AddressStreetAnswer> {

    private val filter by lazy { """
        nodes, ways, relations with
          addr:housenumber and !addr:street and !addr:place and !addr:block_number
          or addr:streetnumber and !addr:street
    """.toElementFilterExpression() }

    private val roadsWithNamesFilter by lazy { """
        ways with
          highway ~ ${ALL_ROADS.joinToString("|")}
          and name
    """.toElementFilterExpression()}

    override val commitMessage = "Add street/place names to address"
    override val icon = R.drawable.ic_quest_housenumber_street
    // In Japan, housenumbers usually have block numbers, not streets
    override val enabledInCountries = AllCountriesExcept("JP")

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_street_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val housenumber = tags["addr:streetnumber"] ?: tags["addr:housenumber"]
        return if (housenumber != null) arrayOf(housenumber) else arrayOf()
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val associatedStreetRelations = mapData.relations.filter {
            val type = it.tags?.get("type")
            type == "associatedStreet" || type == "street"
        }

        val addressesWithoutStreet = mapData.filter { address ->
            filter.matches(address) &&
            associatedStreetRelations.none { it.contains(address.type, address.id) }
        }

        if (addressesWithoutStreet.isNotEmpty()) {
            val roadsWithNames = mapData.ways
                .filter { roadsWithNamesFilter.matches(it) }
                .mapNotNull {
                    val geometry = mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry
                    val roadNamesByLanguage = it.tags?.toRoadNameByLanguage()
                    if (geometry != null && roadNamesByLanguage != null) {
                        RoadNameSuggestionEntry(it.id, roadNamesByLanguage, geometry.polylines.first())
                    } else null
                }
            roadNameSuggestionsDao.putRoads(roadsWithNames)
        }
        return addressesWithoutStreet
    }

    override fun createForm() = AddAddressStreetForm()

    /* cannot be determined because of the associated street relations */
    override fun isApplicableTo(element: Element): Boolean? = null

    override fun applyAnswerTo(answer: AddressStreetAnswer, changes: StringMapChangesBuilder) {
        val key = when(answer) {
            is StreetName -> "addr:street"
            is PlaceName -> "addr:place"
        }
        changes.add(key, answer.name)
    }

    override fun cleanMetadata() {
        roadNameSuggestionsDao.cleanUp()
    }
}

private fun Relation.contains(elementType: Element.Type, elementId: Long) : Boolean {
    return members.any { it.type == elementType && it.ref == elementId }
}
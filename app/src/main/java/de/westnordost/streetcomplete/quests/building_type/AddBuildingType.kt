package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.address.containsAnyNode
import de.westnordost.streetcomplete.quests.address.containsWay
import de.westnordost.streetcomplete.quests.address.nonBuildingAreasWithAddressFilter
import de.westnordost.streetcomplete.quests.address.nonMultipolygonRelationsWithAddressFilter
import de.westnordost.streetcomplete.quests.address.notABuildingFilter
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.math.LatLonRaster
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddBuildingType : OsmElementQuestType<BuildingType> {

    override val changesetComment = "Add building types"
    override val wikiLink = "Key:building"
    override val icon = R.drawable.ic_quest_building
    override val achievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_buildingType_title

    override fun createForm() = AddBuildingTypeForm()

    override fun isApplicableTo(element: Element): Boolean? =
        if (!buildingFilter.matches(element) || hasAddressFilter.matches(element)) false else null

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val buildings = mapData.filter { buildingFilter.matches(it) }
        return getBuildingsWithoutAddress(buildings, mapData)
    }

    override fun applyAnswerTo(answer: BuildingType, tags: Tags, timestampEdited: Long) {
        applyBuildingAnswer(answer, tags, timestampEdited)
    }
}

fun applyBuildingAnswer(answer: BuildingType, tags: Tags, timestampEdited: Long) {
    if (answer.osmKey == "man_made") {
        tags.remove("building")
        tags["man_made"] = answer.osmValue
    } else if (answer.osmKey == "demolished:building") {
        tags.remove("building")
        tags[answer.osmKey] = answer.osmValue
    } else if (answer.osmValue == "transformer_tower") {
        tags["building"] = answer.osmValue
        tags["power"] = "substation"
        tags["substation"] = "minor_distribution"
    } else if (answer.osmKey != "building") {
        tags[answer.osmKey] = answer.osmValue
        if (answer == BuildingType.ABANDONED) {
            tags.remove("disused")
        }
        if (answer == BuildingType.RUINS && tags["disused"] == "no") {
            tags.remove("disused")
        }
        if (answer == BuildingType.RUINS && tags["abandoned"] == "no") {
            tags.remove("abandoned")
        }
    } else {
        tags["building"] = answer.osmValue
    }
}

// in the case of man_made, historic, military, aeroway and power, these tags already contain
// information about the purpose of the building, so no need to force asking it
// or question would be confusing as there is no matching reply in available answers
// same goes (more or less) for tourism, amenity, leisure. See #1854, #1891, #3233
val buildingFilter by lazy { """
        ways, relations with (building = yes or building = unclassified)
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !amenity
         and !leisure
         and !aeroway
         and !railway
         and !description
         and location != underground
         and abandoned != yes
         and abandoned != building
         and abandoned:building != yes
         and ruins != yes and ruined != yes
    """.toElementFilterExpression() }

private val hasAddressFilter by lazy { """
    nodes, ways, relations with ~"addr:.*"
""".toElementFilterExpression() }

// copied and slightly modified from AddHousenumber quest
fun getBuildingsWithoutAddress(_buildings: List<Element>, mapData: MapDataWithGeometry): List<Element> {
    val bbox = mapData.boundingBox ?: return listOf()

    val addressNodesById = mapData.nodes.filter { hasAddressFilter.matches(it) }.associateBy { it.id }
    val addressNodeIds = addressNodesById.keys

    /** filter: only buildings with no address that usually should have an address
     *          ...that do not have an address node on their outline */

    val buildings = _buildings.filter { !hasAddressFilter.matches(it) && !it.containsAnyNode(addressNodeIds, mapData)
    }.toMutableList()

    if (buildings.isEmpty()) return listOf()

    /** exclude buildings which are included in relations that have an address */

    val relationsWithAddress = mapData.relations.filter { nonMultipolygonRelationsWithAddressFilter.matches(it) }

    buildings.removeAll { building ->
        relationsWithAddress.any { it.containsWay(building.id) }
    }

    if (buildings.isEmpty()) return listOf()

    /** exclude buildings that intersect with the bounding box because it is not possible to
    ascertain for these if there is an address node within the building - it could be outside
    the bounding box */

    val buildingGeometriesById = buildings.associate {
        it.id to mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry
    }

    buildings.removeAll { building ->
        val buildingBounds = buildingGeometriesById[building.id]?.getBounds()
        (buildingBounds == null || !buildingBounds.isCompletelyInside(bbox))
    }

    if (buildings.isEmpty()) return listOf()

    /** exclude buildings that contain an address node somewhere within their area */

    val addressPositions = LatLonRaster(bbox, 0.0005)
    for (node in addressNodesById.values) {
        addressPositions.insert(node.position)
    }

    buildings.removeAll { building ->
        val buildingGeometry = buildingGeometriesById[building.id]
        if (buildingGeometry != null) {
            val nearbyAddresses = addressPositions.getAll(buildingGeometry.getBounds())
            nearbyAddresses.any { it.isInMultipolygon(buildingGeometry.polygons) }
        } else true
    }

    if (buildings.isEmpty()) return listOf()

    /** exclude buildings that are contained in an area that has an address tagged on itself
     *  or on a vertex on its outline */

    val areasWithAddressesOnOutline = mapData
        .filter { notABuildingFilter.matches(it) && it.isArea() && it.containsAnyNode(addressNodeIds, mapData) }
        .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }

    val areasWithAddresses = mapData
        .filter { nonBuildingAreasWithAddressFilter.matches(it) }
        .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }

    val buildingsByCenterPosition: Map<LatLon?, Element> = buildings.associateBy { buildingGeometriesById[it.id]?.center }

    val buildingPositions = LatLonRaster(bbox, 0.0005)
    for (buildingCenterPosition in buildingsByCenterPosition.keys) {
        if (buildingCenterPosition != null) buildingPositions.insert(buildingCenterPosition)
    }

    for (areaWithAddress in areasWithAddresses + areasWithAddressesOnOutline) {
        val nearbyBuildings = buildingPositions.getAll(areaWithAddress.getBounds())
        val buildingPositionsInArea = nearbyBuildings.filter { it.isInMultipolygon(areaWithAddress.polygons) }
        val buildingsInArea = buildingPositionsInArea.mapNotNull { buildingsByCenterPosition[it] }

        buildings.removeAll(buildingsInArea)
    }

    return buildings
}

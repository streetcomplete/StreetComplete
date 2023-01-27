package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.address.applyTo
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.math.LatLonRaster
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddHousenumber : OsmElementQuestType<HouseNumberAnswer> {

    override val changesetComment = "Survey housenumbers"
    override val wikiLink = "Key:addr"
    override val icon = R.drawable.ic_quest_housenumber
    override val achievements = listOf(POSTMAN)
    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=address/AddHousenumber.kt
    override val enabledInCountries = AllCountriesExcept(
        "LU", // https://github.com/streetcomplete/StreetComplete/pull/1943
        "LV", // https://github.com/streetcomplete/StreetComplete/issues/4597
              // https://lists.openstreetmap.org/pipermail/talk-lv/2022-January/006357.html
              // https://wiki.openstreetmap.org/wiki/Automated_edits/Latvia-bot
        "NL", // https://forum.openstreetmap.org/viewtopic.php?id=60356
        "DK", // https://lists.openstreetmap.org/pipermail/talk-dk/2017-November/004898.html
        "NO", // https://forum.openstreetmap.org/viewtopic.php?id=60357
        "CZ", // https://lists.openstreetmap.org/pipermail/talk-cz/2017-November/017901.html
        "IT", // https://lists.openstreetmap.org/pipermail/talk-it/2018-July/063712.html
        "FR", // https://github.com/streetcomplete/StreetComplete/issues/2427#issuecomment-751860679 https://t.me/osmfr/26320
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val bbox = mapData.boundingBox ?: return listOf()

        val addressNodesById = mapData.nodes.filter { nodesWithAddressFilter.matches(it) }.associateBy { it.id }
        val addressNodeIds = addressNodesById.keys

        /** filter: only buildings with no address that usually should have an address
         *          ...that do not have an address node on their outline */

        val buildings = mapData.filter {
            buildingsWithMissingAddressFilter.matches(it)
            && !it.containsAnyNode(addressNodeIds, mapData)
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
            val buildingsInArea = buildingPositionsInArea.mapNotNull { buildingsByCenterPosition[it] }.toSet()

            buildings.removeAll(buildingsInArea)
        }

        return buildings
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!buildingsWithMissingAddressFilter.matches(element)) false else null

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes, ways, relations with
            (addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber)
            and !name and !brand and !operator and !ref
        """.toElementFilterExpression())

    override fun createForm() = AddHousenumberForm()

    override fun applyAnswerTo(answer: HouseNumberAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is AddressNumberOrName -> {
                if (answer.number == null && answer.name == null) {
                    tags["nohousenumber"] = "yes"
                } else {
                    answer.number?.applyTo(tags)
                    if (answer.name != null) {
                        tags["addr:housename"] = answer.name
                    }
                }
            }
            WrongBuildingType -> {
                tags["building"] = "yes"
            }
        }
    }
}

private val notABuildingFilter by lazy { """
    ways, relations with !building"
""".toElementFilterExpression() }

private val nonBuildingAreasWithAddressFilter by lazy { """
    ways, relations with
      (addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber)
      and !building
""".toElementFilterExpression() }

private val nonMultipolygonRelationsWithAddressFilter by lazy { """
    relations with
      type != multipolygon
      and (addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber)
""".toElementFilterExpression() }

private val nodesWithAddressFilter by lazy { """
   nodes with
     addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber
""".toElementFilterExpression() }

private val buildingsWithMissingAddressFilter by lazy { """
    ways, relations with
      building ~ ${buildingTypesThatShouldHaveAddresses.joinToString("|")}
      and location != underground
      and ruins != yes
      and abandoned != yes
      and !addr:housenumber
      and !addr:housename
      and !addr:conscriptionnumber
      and !addr:streetnumber
      and !noaddress
      and !nohousenumber
""".toElementFilterExpression() }

private val buildingTypesThatShouldHaveAddresses = listOf(
    "house", "residential", "apartments", "detached", "terrace", "dormitory", "semi",
    "semidetached_house", "farm", "school", "civic", "college", "university", "public", "hospital",
    "kindergarten", "train_station", "hotel", "retail", "shop", "commercial", "office"
)

private fun Element.containsAnyNode(nodeIds: Set<Long>, mapData: MapDataWithGeometry): Boolean =
    when (this) {
        is Way -> this.nodeIds.any { it in nodeIds }
        is Relation -> containsAnyNode(nodeIds, mapData)
        else -> false
    }

/** return whether any way contained in this relation contains any of the nodes with the given ids */
private fun Relation.containsAnyNode(nodeIds: Set<Long>, mapData: MapDataWithGeometry): Boolean =
    members
        .filter { it.type == ElementType.WAY }
        .any { member ->
            val way = mapData.getWay(member.ref)
            way?.nodeIds?.any { it in nodeIds } ?: false
        }

/** return whether any of the ways with the given ids are contained in this relation */
private fun Relation.containsWay(wayId: Long): Boolean =
    members.any { it.type == ElementType.WAY && wayId == it.ref }

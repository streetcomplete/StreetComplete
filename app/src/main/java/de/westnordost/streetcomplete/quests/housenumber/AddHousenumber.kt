package de.westnordost.streetcomplete.quests.housenumber

import android.util.Log

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.DEFAULT_MAX_QUESTS
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.osm.tql.toOverpassBboxFilter
import de.westnordost.streetcomplete.util.FlattenIterable
import de.westnordost.streetcomplete.util.LatLonRaster
import de.westnordost.streetcomplete.util.SphericalEarthMath

class AddHousenumber(private val overpass: OverpassMapDataDao) : OsmElementQuestType<HousenumberAnswer> {

    override val commitMessage = "Add housenumbers"
    override val icon = R.drawable.ic_quest_housenumber

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=housenumber/AddHousenumber.kt
    override val enabledForCountries = Countries.allExcept(
        "NL", // https://forum.openstreetmap.org/viewtopic.php?id=60356
        "DK", // https://lists.openstreetmap.org/pipermail/talk-dk/2017-November/004898.html
        "NO", // https://forum.openstreetmap.org/viewtopic.php?id=60357
        "CZ", // https://lists.openstreetmap.org/pipermail/talk-cz/2017-November/017901.html
        "IT"  // https://lists.openstreetmap.org/pipermail/talk-it/2018-July/063712.html
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_title

    override fun isApplicableTo(element: Element) = null

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        var ms = System.currentTimeMillis()

        val buildings = downloadBuildingsWithoutAddresses(bbox) ?: return false
        // empty result: We are done
        if (buildings.isEmpty()) return true

        val addrAreas = downloadAreasWithAddresses(bbox) ?: return false

        val extendedBbox = getBoundingBoxThatIncludes(buildings.values)

        val addrPositions = downloadFreeFloatingPositionsWithAddresses(extendedBbox) ?: return false

        Log.d("AddHousenumber", "Downloaded ${buildings.size} buildings with no address, " +
            "${addrAreas.size} areas with address and ${addrPositions.size} address nodes " +
            "in ${System.currentTimeMillis() - ms}ms"
        )
        ms = System.currentTimeMillis()

        val buildingPositions = LatLonRaster(extendedBbox, 0.0005)
        for (buildingCenter in buildings.keys) {
            buildingPositions.insert(buildingCenter)
        }

        // exclude buildings that are contained in an area with a housenumber
        for (addrArea in addrAreas) {
            for (buildingPos in buildingPositions.getAll(addrArea.bounds)) {
                if (SphericalEarthMath.isInMultipolygon(buildingPos, addrArea.polygons)) {
                    buildings.remove(buildingPos)
                }
            }
        }

        var createdQuests = 0
        // only buildings with no housenumber-nodes inside them
        for (building in buildings.values) {
            // even though we could continue here, limit the max amount of quests created to the
            // default maximum to avoid performance problems
            if (createdQuests++ >= DEFAULT_MAX_QUESTS) break

            val addrContainedInBuilding = getPositionContainedInBuilding(building.geometry, addrPositions)
            if (addrContainedInBuilding != null) {
                addrPositions.remove(addrContainedInBuilding)
                continue
            }

            handler.handle(building.element, building.geometry)
        }

        Log.d("AddHousenumber", "Processing data took ${System.currentTimeMillis() - ms}ms")

        return true
    }

    private fun downloadBuildingsWithoutAddresses(bbox: BoundingBox): MutableMap<LatLon, ElementWithGeometry>? {
        val buildingsByCenterPoint = mutableMapOf<LatLon, ElementWithGeometry>()
        val query = getBuildingsWithoutAddressesOverpassQuery(bbox)
        val success = overpass.getAndHandleQuota(query) { element, geometry ->
            if (geometry?.polygons != null && geometry.center != null) {
                buildingsByCenterPoint[geometry.center] = ElementWithGeometry(element, geometry)
            }
        }
        return if (success) buildingsByCenterPoint else null
    }

    private fun downloadFreeFloatingPositionsWithAddresses(bbox: BoundingBox): LatLonRaster? {
        val grid = LatLonRaster(bbox, 0.0005)
        val query = getFreeFloatingAddressesOverpassQuery(bbox)
        val success = overpass.getAndHandleQuota(query) { _, geometry ->
            if (geometry != null) grid.insert(geometry.center)
        }
        return if (success) grid else null
    }

    private fun downloadAreasWithAddresses(bbox: BoundingBox): List<ElementGeometry>? {
        val areas = mutableListOf<ElementGeometry>()
        val query = getNonBuildingAreasWithAddresses(bbox)
        val success = overpass.getAndHandleQuota(query) { _, geometry ->
            if (geometry?.polygons != null) areas.add(geometry)
        }
        return if (success) areas else null
    }

    /** Query that returns all areas that are not buildings but have addresses  */
    private fun getNonBuildingAreasWithAddresses(bbox: BoundingBox): String {
        val globalBbox = bbox.toGlobalOverpassBBox()
        return """
            $globalBbox
            (
              way[!building] $ANY_ADDRESS_FILTER;
              rel[!building] $ANY_ADDRESS_FILTER;
            );
            out geom;
            """.trimIndent()
    }

    /** Query that returns all buildings that neither have an address node on their outline, nor
     * on itself  */
    private fun getBuildingsWithoutAddressesOverpassQuery(bbox: BoundingBox): String {
        val bboxFilter = bbox.toOverpassBboxFilter()
        return """
            (
              way$BUILDINGS_WITHOUT_ADDRESS_FILTER$bboxFilter;
              rel$BUILDINGS_WITHOUT_ADDRESS_FILTER$bboxFilter;
            ) -> .buildings;
            .buildings > -> .building_nodes;
            node.building_nodes$ANY_ADDRESS_FILTER; < -> .buildings_with_addr_nodes;
            (.buildings; - .buildings_with_addr_nodes;);
            out meta geom;
            """.trimIndent()
    }

    /** Query that returns all address nodes that are not part of any building outline  */
    private fun getFreeFloatingAddressesOverpassQuery(bbox: BoundingBox): String {
        val globalBbox = bbox.toGlobalOverpassBBox()
        return """
            $globalBbox
            (
              node$ANY_ADDRESS_FILTER;
               - ((way[building]; relation[building];);>;);
            );
            out skel;
            """.trimIndent()
    }


    private fun getPositionContainedInBuilding(building: ElementGeometry, positions: LatLonRaster): LatLon? {
        val buildingPolygons = building.polygons ?: return null

        for (pos in positions.getAll(building.bounds)) {
            if (SphericalEarthMath.isInMultipolygon(pos, buildingPolygons)) return pos
        }
        return null
    }

    private fun getBoundingBoxThatIncludes(buildings: Iterable<ElementWithGeometry>): BoundingBox {
        // see #885: The area in which the app should search for address nodes (and areas) must be
        // adjusted to the bounding box of all the buildings found. The found buildings may in parts
        // not be within the specified bounding box. But in exactly that part, there may be an
        // address
        val allThePoints = FlattenIterable(LatLon::class.java)
        for (building in buildings) {
            allThePoints.add(building.geometry.polygons)
        }
        return SphericalEarthMath.enclosingBoundingBox(allThePoints)
    }

    override fun createForm() = AddHousenumberForm()

    override fun applyAnswerTo(answer: HousenumberAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoHouseNumber -> changes.add("nohousenumber", "yes")
            is HouseNumber   -> changes.add("addr:housenumber", answer.number)
            is HouseName     -> changes.add("addr:housename", answer.name)
            is ConscriptionNumber -> {
                changes.add("addr:conscriptionnumber", answer.number)
                if (answer.streetNumber != null) {
                    changes.add("addr:streetnumber", answer.streetNumber)
                    changes.add("addr:housenumber", answer.streetNumber)
                } else {
                    changes.add("addr:housenumber", answer.number)
                }
            }
            is HouseAndBlockNumber -> {
                changes.add("addr:housenumber", answer.houseNumber)
                changes.add("addr:block_number", answer.blockNumber)
            }
        }
    }

    private data class ElementWithGeometry(val element: Element, val geometry: ElementGeometry)

    companion object {
        private const val ANY_ADDRESS_FILTER =
            "[~'^addr:(housenumber|housename|conscriptionnumber|streetnumber)$'~'.']"

        private const val NO_ADDRESS_FILTER =
            "[!'addr:housenumber'][!'addr:housename'][!'addr:conscriptionnumber'][!'addr:streetnumber'][!noaddress][!nohousenumber]"

        private const val BUILDINGS_WITHOUT_ADDRESS_FILTER =
            "['building'~'^(house|residential|apartments|detached|terrace|dormitory|semi|semidetached_house|farm|" +
            "school|civic|college|university|public|hospital|kindergarten|train_station|hotel|" +
            "retail|commercial)$'][location!=underground][ruins!=yes]" + NO_ADDRESS_FILTER
    }
}

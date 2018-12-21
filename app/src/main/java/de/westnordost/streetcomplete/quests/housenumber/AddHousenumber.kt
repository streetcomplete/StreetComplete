package de.westnordost.streetcomplete.quests.housenumber

import android.os.Bundle
import android.text.TextUtils
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
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil
import de.westnordost.streetcomplete.util.FlattenIterable
import de.westnordost.streetcomplete.util.LatLonRaster
import de.westnordost.streetcomplete.util.SphericalEarthMath
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddHousenumber(private val overpass: OverpassMapDataDao) : OsmElementQuestType {

    override val commitMessage = "Add housenumbers"
    override val icon = R.drawable.ic_quest_housenumber

    // See overview here: https://ent8r.github.io/blacklistr/?java=housenumber/AddHousenumber.java
    override val enabledForCountries = Countries.allExcept(arrayOf(
        "NL", // https://forum.openstreetmap.org/viewtopic.php?id=60356
        "DK", // https://lists.openstreetmap.org/pipermail/talk-dk/2017-November/004898.html
        "NO", // https://forum.openstreetmap.org/viewtopic.php?id=60357
        "CZ", // https://lists.openstreetmap.org/pipermail/talk-cz/2017-November/017901.html
        "IT"  // https://lists.openstreetmap.org/pipermail/talk-it/2018-July/063712.html
    ))

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
            if (createdQuests++ >= OverpassQLUtil.DEFAULT_MAX_QUESTS) break

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
        val buildingsByCenterPoint = HashMap<LatLon, ElementWithGeometry>()
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
        val areas = ArrayList<ElementGeometry>()
        val query = getNonBuildingAreasWithAddresses(bbox)
        val success = overpass.getAndHandleQuota(query) { _, geometry ->
            if (geometry?.polygons != null) areas.add(geometry)
        }
        return if (success) areas else null
    }

    /** Query that returns all areas that are not buildings but have addresses  */
    private fun getNonBuildingAreasWithAddresses(bbox: BoundingBox): String {
        return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
            "(way[!building]" + ANY_ADDRESS_FILTER + ";rel[!building]" + ANY_ADDRESS_FILTER + ";);" +
            "out geom;"
    }

    /** Query that returns all buildings that neither have an address node on their outline, nor
     * on itself  */
    private fun getBuildingsWithoutAddressesOverpassQuery(bbox: BoundingBox): String {
        val bboxFilter = OverpassQLUtil.getOverpassBboxFilter(bbox)
        return "(" +
            "  way" + BUILDINGS_WITHOUT_ADDRESS_FILTER + bboxFilter + ";" +
            "  rel" + BUILDINGS_WITHOUT_ADDRESS_FILTER + bboxFilter + ";" +
            ") -> .buildings;" +
            ".buildings > -> .building_nodes;" +
            "node.building_nodes" + ANY_ADDRESS_FILTER + ";< -> .buildings_with_addr_nodes;" +
            // all buildings without housenumber minus ways that contain building nodes with addresses
            "(.buildings; - .buildings_with_addr_nodes;);" +
            // not using OverpassQLUtil.getQuestPrintStatement here because buildings will get filtered out further here
            "out meta geom;"
    }

    /** Query that returns all address nodes that are not part of any building outline  */
    private fun getFreeFloatingAddressesOverpassQuery(bbox: BoundingBox): String {
        return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
            "(" +
            "  node" + ANY_ADDRESS_FILTER + ";" +
            "  - ((way[building];relation[building];);>;);" +
            ");" +
            "out skel;"
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

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val noAddress = answer.getBoolean(AddHousenumberForm.NO_ADDRESS)
        var housenumber = answer.getString(AddHousenumberForm.HOUSENUMBER)
        val housename = answer.getString(AddHousenumberForm.HOUSENAME)
        val conscriptionnumber = answer.getString(AddHousenumberForm.CONSCRIPTIONNUMBER)
        val streetnumber = answer.getString(AddHousenumberForm.STREETNUMBER)

        if (noAddress) {
            changes.add("noaddress", "yes")
        } else if (conscriptionnumber != null) {
            changes.add("addr:conscriptionnumber", conscriptionnumber)
            if (!TextUtils.isEmpty(streetnumber)) changes.add("addr:streetnumber", streetnumber!!)

            housenumber = streetnumber
            if (TextUtils.isEmpty(housenumber)) housenumber = conscriptionnumber
            changes.add("addr:housenumber", housenumber!!)
        } else if (housenumber != null) {
            changes.add("addr:housenumber", housenumber)
        } else if (housename != null) {
            changes.add("addr:housename", housename)
        }
    }

    private data class ElementWithGeometry(val element: Element, val geometry: ElementGeometry)

    companion object {
        private val ANY_ADDRESS_FILTER =
            "[~'^addr:(housenumber|housename|conscriptionnumber|streetnumber)$'~'.']"

        private val NO_ADDRESS_FILTER =
            "[!'addr:housenumber'][!'addr:housename'][!'addr:conscriptionnumber'][!'addr:streetnumber'][!noaddress]"

        private val BUILDINGS_WITHOUT_ADDRESS_FILTER =
            "['building'~'^(house|residential|apartments|detached|terrace|dormitory|semi|semidetached_house|farm|" +
            "school|civic|college|university|public|hospital|kindergarten|train_station|hotel|" +
            "retail|commercial)$'][location!=underground]" + NO_ADDRESS_FILTER
    }
}

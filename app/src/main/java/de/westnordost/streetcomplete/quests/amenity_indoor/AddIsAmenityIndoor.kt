package de.westnordost.streetcomplete.quests.amenity_indoor

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.math.LatLonRaster
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddIsAmenityIndoor(private val getFeature: (Element) -> Feature?) :
    OsmElementQuestType<IsAmenityIndoorAnswer> {

    private val nodesFilter by lazy { """
        nodes with
          (
            emergency ~ defibrillator|fire_extinguisher|fire_hose
            or amenity ~ atm|telephone|parcel_locker|luggage_locker|locker|post_box|public_bookcase|give_box|ticket_validator|vending_machine
            or amenity = clock and display != sundial
          )
          and access !~ private|no
          and !indoor and !location and !level and !level:ref
          and covered != yes
    """.toElementFilterExpression() }

    /* small POIs that tend to be always attached to walls (and where the location is very useful
     * for verifiability). For these, the question shall always be asked, even when not within a
     * building outline. */
    private val nodesOnWalls by lazy { """
        nodes with emergency ~ defibrillator|fire_extinguisher|fire_hose
    """.toElementFilterExpression() }

    /* We only want survey nodes within building outlines.
     * Roofs do not count as inside a building */
    private val buildingFilter by lazy { """
        ways, relations with building and building != roof
    """.toElementFilterExpression() }

    override val changesetComment = "Determine whether amenities are inside buildings"
    override val wikiLink = "Key:indoor"
    override val icon = R.drawable.ic_quest_building_inside
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_is_amenity_inside_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val bbox = mapData.boundingBox ?: return listOf()
        val nodes = mapData.nodes.filter { nodesFilter.matches(it) && getFeature(it) != null }
        val buildings = mapData.filter { buildingFilter.matches(it) }.toMutableList()

        val buildingGeometriesById = buildings.associate {
            it.id to mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry
        }

        val nodesPositions = LatLonRaster(bbox, 0.0005)
        for (node in nodes) {
            nodesPositions.insert(node.position)
        }

        buildings.removeAll { building ->
            val buildingBounds = buildingGeometriesById[building.id]?.getBounds()
            (buildingBounds == null || !buildingBounds.isCompletelyInside(bbox) || nodesPositions.getAll(buildingBounds).count() == 0)
        }

        // Reduce all matching nodes to nodes within building outlines or small pois on walls
        val result = nodes.filter {
            nodesOnWalls.matches(it) ||
            buildings.any { building ->
                val buildingGeometry = buildingGeometriesById[building.id]

                if (buildingGeometry != null && buildingGeometry.getBounds().contains(it.position)) {
                    it.position.isInMultipolygon(buildingGeometry.polygons)
                } else {
                    false
                }
            }
        }

        return result
    }

    override fun isApplicableTo(element: Element) =
        if (nodesFilter.matches(element) && getFeature(element) != null) {
            if (nodesOnWalls.matches(element)) true else null
        } else {
            false
        }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        /* put markers for objects that are exactly the same as for which this quest is asking for
           e.g. it's a ticket validator? -> display other ticket validators. Etc. */
        val feature = getFeature(element) ?: return emptySequence()
        return getMapData().filter { it.tags.containsAll(feature.tags) }.asSequence()
    }

    override fun createForm() = IsAmenityIndoorForm()

    override fun applyAnswerTo(answer: IsAmenityIndoorAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            IsAmenityIndoorAnswer.INDOOR -> tags["indoor"] = "yes"
            IsAmenityIndoorAnswer.OUTDOOR -> tags["indoor"] = "no"
            IsAmenityIndoorAnswer.COVERED -> tags["covered"] = "yes"
        }
    }
}

package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfacesWithNote
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.SurfaceMissingWithNote
import de.westnordost.streetcomplete.osm.surface.associatedKeysToBeRemovedOnChange
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

class RoadSurfaceOverlay : Overlay {

    private val parentQuest = AddRoadSurface()
    override val title = R.string.overlay_road_surface
    override val icon = parentQuest.icon
    override val changesetComment = parentQuest.changesetComment
    override val wikiLink: String = parentQuest.wikiLink
    override val achievements = parentQuest.achievements
    override val hidesQuestTypes = setOf(parentQuest::class.simpleName!!, AddPathSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + Surface.surfaceReplacements.keys
        return mapData
           .filter( """ways, relations with
               highway ~ ${(ALL_ROADS).joinToString("|")}
               and (!surface or surface ~ ${handledSurfaces.joinToString("|") })
               """)
           .filter { element -> tagsHaveOnlyAllowedSurfaceKeys(element.tags) }.map { it to getStyle(it) }
    }

    private fun tagsHaveOnlyAllowedSurfaceKeys(tags: Map<String, String>): Boolean {
        return tags.keys.none {
            "surface" in it && it !in allowedTagWithSurfaceInKey
        }
    }
    // https://taginfo.openstreetmap.org/search?q=surface
    val supportedSurfaceKeys = listOf(
        // supported in this overlay, but not in all overlays
        "sidewalk:both:surface", "sidewalk:right:surface", "sidewalk:left:surface", "sidewalk:surface",

        // this is not a valid tag on road and therefore not supported here
        // "footway:surface", "cycleway:surface",

        // supported in all surface overlays
        "surface",
        "surface:note"
    ) + associatedKeysToBeRemovedOnChange("surface")

    private val allowedTagWithSurfaceInKey = supportedSurfaceKeys + listOf(
        "proposed:surface", // does not matter
    )

    override fun createForm(element: Element?) =
        if (element != null && element.tags["highway"] in ALL_ROADS) RoadSurfaceOverlayForm()
        else null
}

private fun getStyle(element: Element): Style {
    val surfaceStatus = createSurfaceStatus(element.tags)
    val badSurfaces = listOf(null, PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA)
    var dominatingSurface: Surface? = null
    var noteProvided: String? = null
    var keyOfDominatingSurface: String? = null // TODO likely replace by translated value or skip it
    when (surfaceStatus) {
        is SingleSurfaceWithNote -> {
            // TODO special styling needed I guess...
            // as it should not get pinking "no data"...
            // use dashes?
            dominatingSurface = surfaceStatus.surface
            keyOfDominatingSurface = "surface"
            noteProvided = surfaceStatus.note
        }
        is SingleSurface -> {
            dominatingSurface = surfaceStatus.surface
            keyOfDominatingSurface = "surface"
        }
        is CyclewayFootwaySurfaces -> {
            throw Exception("this should be impossible and excluded via supportedSurfaceKeys not including cycleway:surface and footway:surface")
        }
        is SurfaceMissing -> {
            // no action needed
        }
        is SurfaceMissingWithNote -> {
            noteProvided = surfaceStatus.note
        }
        is CyclewayFootwaySurfacesWithNote -> {
            throw Exception("this should be impossible and excluded via supportedSurfaceKeys not including cycleway:surface:note and footway:surface:note")
        }
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in badSurfaces
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element)) || element.tags["leisure"] == "playground"
    val color = if (isNotSetButThatsOkay) {
        Color.INVISIBLE
    } else if (isNotSet && noteProvided != null) {
        Color.BLACK
    } else {
        dominatingSurface.color
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color), null, null)

    // label for debugging
    //val label = element.tags[keyOfDominatingSurface]
    //return if (element.tags["area"] == "yes") PolygonStyle(color, label) else PolylineStyle(color, null, null, label)
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"

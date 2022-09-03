package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.SingleSurface
import de.westnordost.streetcomplete.osm.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.Surface.ARTIFICIAL_TURF
import de.westnordost.streetcomplete.osm.Surface.ASPHALT
import de.westnordost.streetcomplete.osm.Surface.CLAY
import de.westnordost.streetcomplete.osm.Surface.COMPACTED
import de.westnordost.streetcomplete.osm.Surface.CONCRETE
import de.westnordost.streetcomplete.osm.Surface.CONCRETE_LANES
import de.westnordost.streetcomplete.osm.Surface.CONCRETE_PLATES
import de.westnordost.streetcomplete.osm.Surface.DIRT
import de.westnordost.streetcomplete.osm.Surface.FINE_GRAVEL
import de.westnordost.streetcomplete.osm.Surface.GRASS
import de.westnordost.streetcomplete.osm.Surface.GRASS_PAVER
import de.westnordost.streetcomplete.osm.Surface.GRAVEL
import de.westnordost.streetcomplete.osm.Surface.GROUND_AREA
import de.westnordost.streetcomplete.osm.Surface.GROUND_ROAD
import de.westnordost.streetcomplete.osm.Surface.METAL
import de.westnordost.streetcomplete.osm.Surface.PAVED_AREA
import de.westnordost.streetcomplete.osm.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.osm.Surface.PAVING_STONES
import de.westnordost.streetcomplete.osm.Surface.PEBBLES
import de.westnordost.streetcomplete.osm.Surface.ROCK
import de.westnordost.streetcomplete.osm.Surface.SAND
import de.westnordost.streetcomplete.osm.Surface.SETT
import de.westnordost.streetcomplete.osm.Surface.TARTAN
import de.westnordost.streetcomplete.osm.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.osm.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.osm.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.osm.Surface.WOOD
import de.westnordost.streetcomplete.osm.Surface.WOODCHIPS
import de.westnordost.streetcomplete.osm.SurfaceMissing
import de.westnordost.streetcomplete.osm.createSurfaceStatus
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

class PathSurfaceOverlay : Overlay {

    private val parentQuest = AddPathSurface()
    override val title = R.string.overlay_path_surface
    override val icon = parentQuest.icon
    override val changesetComment = parentQuest.changesetComment
    override val wikiLink: String = parentQuest.wikiLink
    override val achievements = parentQuest.achievements
    override val hidesQuestTypes = setOf(parentQuest::class.simpleName!!, AddPathSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + Surface.surfaceReplacements.keys
        return mapData
           .filter( """ways, relations with
               highway ~ ${(ALL_PATHS).joinToString("|")}
               and (!surface or surface ~ ${handledSurfaces.joinToString("|") })
               and (segregated = yes or (!cycleway:surface and !footway:surface))
               """)
           .filter { element -> tagsHaveOnlyAllowedSurfaceKeys(element.tags) }.map { it to getStyle(it) }
    }

    private fun tagsHaveOnlyAllowedSurfaceKeys(tags: Map<String, String>): Boolean {
        return tags.keys.none {
            "surface" in it && it !in allowedTagWithSurfaceInKey
        }
    }
    // https://taginfo.openstreetmap.org/search?q=surface
    // Maybe should be supportedd?
    // sidewalk:both:surface
    // sidewalk:right:surface
    // sidewalk:left:surface
    // sidewalk:surface

    // https://taginfo.openstreetmap.org/search?q=surface
    val supportedSurfaceKeys = listOf("surface", "footway:surface", "cycleway:surface",
        "check_date:surface", "check_date:footway:surface", "check_date:cycleway:surface", // verify that it is supported TODO
        "source:surface", "source:footway:surface", "source:cycleway:surface", // verify that it is removed on change TODO
        "surface:colour", //  12K - remove on change? Ignore support? TODO
        "surface:note" // "note:surface" is not supported. TODO: actually support
    )

    private val allowedTagWithSurfaceInKey = supportedSurfaceKeys + listOf(
        "proposed:surface", // does not matter
    )

    override fun createForm(element: Element) = UniversalSurfaceOverlayForm()
}

private fun getStyle(element: Element): Style {
    val surfaceStatus = createSurfaceStatus(element.tags)
    val badSurfaces = listOf(null, PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA)
    var dominatingSurface: Surface? = null
    var keyOfDominatingSurface: String? = null // TODO likely replace by translated value or skip it
    when (surfaceStatus) {
        is SingleSurfaceWithNote -> {
            // TODO special styling needed I guess...
            // as it should not get pinking "no data"...
            // use dashes?
            dominatingSurface = surfaceStatus.surface
            keyOfDominatingSurface = "surface"
        }
        is SingleSurface -> {
            dominatingSurface = surfaceStatus.surface
            keyOfDominatingSurface = "surface"
        }
        is CyclewayFootwaySurfaces -> if (surfaceStatus.footway in badSurfaces) {
            dominatingSurface = surfaceStatus.footway
            keyOfDominatingSurface = "footway:surface"
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = surfaceStatus.cycleway
            keyOfDominatingSurface = "cycleway:surface"
        }
        is SurfaceMissing -> {
            // no action needed
        }
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSetButThatsOkay = dominatingSurface in badSurfaces && (isIndoor(element.tags) || isPrivateOnFoot(element)) || element.tags["leisure"] == "playground"
    val color = if (isNotSetButThatsOkay) Color.INVISIBLE else dominatingSurface.color
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(color, null, null)

    // label for debugging
    //val label = element.tags[keyOfDominatingSurface]
    //return if (element.tags["area"] == "yes") PolygonStyle(color, label) else PolylineStyle(color, null, null, label)
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"

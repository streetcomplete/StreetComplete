package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfacesWithNote
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.SurfaceMissingWithNote
import de.westnordost.streetcomplete.osm.surface.UNDERSPECIFED_SURFACES
import de.westnordost.streetcomplete.osm.surface.keysToBeRemovedOnSurfaceChange
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR

class RoadSurfaceOverlay : Overlay {

    override val title = R.string.overlay_road_surface
    override val icon = R.drawable.ic_quest_street_surface
    override val changesetComment = "Specify road surfaces"
    override val wikiLink: String = "Key:surface"
    override val achievements = listOf(CAR, BICYCLIST)
    override val hidesQuestTypes = setOf(AddRoadSurface::class.simpleName!!, AddPathSurface::class.simpleName!!)

    private val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + INVALID_SURFACES

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        return mapData
            .filter("""
                ways, relations with
                  highway ~ ${(ALL_ROADS).joinToString("|")}
                  and (!surface or surface ~ ${handledSurfaces.joinToString("|")})
                  and (!surface:note or surface)
            """)
            .filter { element -> tagsHaveOnlyAllowedSurfaceKeys(element.tags) }
            .map { it to getStyle(it) }
    }

    // https://taginfo.openstreetmap.org/search?q=surface
    private val supportedSurfaceKeys = listOf(
        // supported in this overlay, but not in all overlays
        "sidewalk:both:surface", "sidewalk:right:surface", "sidewalk:left:surface", "sidewalk:surface",

        // this is not a valid tag on road and therefore not supported here
        // "footway:surface", "cycleway:surface",

        // supported in both surface overlays
        "surface", "surface:note"
    ) + keysToBeRemovedOnSurfaceChange("")

    private val allowedTagWithSurfaceInKey = supportedSurfaceKeys + listOf(
        "proposed:surface", // does not matter
    )

    private fun tagsHaveOnlyAllowedSurfaceKeys(tags: Map<String, String>): Boolean {
        return tags.keys.none {
            "surface" in it && it !in allowedTagWithSurfaceInKey
        }
    }

    override fun createForm(element: Element?) =
        if (element != null && element.tags["highway"] in ALL_ROADS) RoadSurfaceOverlayForm()
        else null
}

private fun getStyle(element: Element): Style {
    val surfaceStatus = createSurfaceStatus(element.tags)
    var dominatingSurface: Surface? = null
    var noteProvided: String? = null
    when (surfaceStatus) {
        is SingleSurfaceWithNote -> {
            dominatingSurface = surfaceStatus.surface
            noteProvided = surfaceStatus.note
        }
        is SingleSurface -> {
            dominatingSurface = surfaceStatus.surface
        }
        is SurfaceMissing -> {
            // no action needed
        }
        is SurfaceMissingWithNote -> {
            noteProvided = surfaceStatus.note
        }
        is CyclewayFootwaySurfaces, is CyclewayFootwaySurfacesWithNote -> {
            throw Exception("this should be impossible and excluded via supportedSurfaceKeys not including cycleway:surface and footway:surface")
        }
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in UNDERSPECIFED_SURFACES
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element)) || element.tags["leisure"] == "playground"
    val color = if (isNotSetButThatsOkay) {
        Color.INVISIBLE
    } else if (isNotSet && noteProvided != null) {
        Color.BLACK
    } else {
        dominatingSurface.color
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color), null, null)
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"

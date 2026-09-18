package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node.MoveNodeMapOverlay
import de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way.SplitWayMapOverlay
import org.maplibre.compose.overlay.MapOverlayScope

/** What the form shown in the bottom sheet places on the map. Composed in the map's overlay; the
 *  counterpart of [MainBottomSheet]. */
@Composable
fun MapOverlayScope.MainBottomSheetMapOverlay(
    shownBottomSheet: ShownBottomSheet,
    formState: BottomSheetFormState,
    mapPosition: LatLon,
) {
    val element: Element?
    val geometry: ElementGeometry
    when (shownBottomSheet) {
        is ShownBottomSheet.OsmQuest -> {
            element = shownBottomSheet.element
            geometry = shownBottomSheet.quest.geometry
        }
        is ShownBottomSheet.Overlay -> {
            element = shownBottomSheet.element
            geometry = shownBottomSheet.geometry ?: ElementPointGeometry(mapPosition)
        }
        else -> return
    }
    when (formState.subForm) {
        BottomSheetSubForm.Main -> formState.overlayForm?.run { MapOverlay(geometry) }
        BottomSheetSubForm.LeaveNote -> {}
        BottomSheetSubForm.SplitWay -> SplitWayMapOverlay(mapPosition, geometry as ElementPolylinesGeometry, formState.snipAnimation)
        BottomSheetSubForm.MoveNode -> MoveNodeMapOverlay(element as Node, mapPosition)
    }
}

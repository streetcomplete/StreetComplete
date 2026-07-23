package de.westnordost.streetcomplete.screens.main.bottom_sheet.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerPixel
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.koinInject

@Composable
fun OverlayFormContainer(
    onDismiss: () -> Unit,
    onEdit: (action: ElementEditAction) -> Unit,
    onLeaveNote: () -> Unit,
    onSplitWay: () -> Unit,
    onMoveNode: () -> Unit,
    overlay: Overlay,
    element: Element?,
    geometry: ElementGeometry?,
    mapRotation: Float,
    mapTilt: Float,
    mapPosition: LatLon,
    mapMetersPerPixel: Double,
    onPinPosition: (icon: DrawableResource, position: LatLon?) -> Unit,
    countryBoundaries: CountryBoundaries = koinInject(),
    countryInfos: CountryInfos = koinInject(),
) {
    val geometry = geometry ?: ElementPointGeometry(mapPosition)
    val countryInfo = remember { countryInfos.get(countryBoundaries, geometry.center) }

    CompositionLocalProvider(
        LocalElement provides element,
        LocalMapRotation provides mapRotation,
        LocalMapTilt provides mapTilt,
        LocalMapMetersPerPixel provides mapMetersPerPixel,
    ) {
        overlay.Form(
            on = { action ->
                when (action) {
                    Action.Dismiss -> onDismiss()
                    Action.LeaveNote -> onLeaveNote()
                    Action.SplitWay -> onSplitWay()
                    Action.MoveNode -> onMoveNode()
                    is Edit -> onEdit(action.value)
                }
            },
            element = element,
            geometry = geometry,
            countryInfo = countryInfo,
            onPinPosition = onPinPosition
        )
    }
}

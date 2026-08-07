package de.westnordost.streetcomplete.screens.main.bottom_sheet.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node.MoveNodeForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.LeaveNoteInsteadForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way.SplitWayForm
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.util.ReplaceBottomSheetTransitionSpec
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.koinInject

/** Container in which all overlay forms are housed.
 *
 *  Takes care of showing the forms for the "other answers" (leave note, split way, move node),
 *  animates between the overlay form and those.
 *
 *  @param onSetMapMarkers is called when the form shown wishes to show markers on the map. E.g. the
 *         split way form shows markers.
 *
 *  @param onSetPinPosition is called when the form wishes to display the overlay pin at a custom
 *         location, e.g. in order to snap it to a way (see e.g. AddressOverlay)
 *  */
@Composable
fun OverlayFormContainer(
    onDismiss: () -> Unit,
    onEdit: (action: ElementEditAction) -> Unit,
    onLeaveNote: (noteText: String, noteImagePaths: List<String>) -> Unit,
    overlay: Overlay,
    element: Element?,
    geometry: ElementGeometry?,
    geometryOffsetInWindow: Offset?,
    mapRotation: Float,
    mapTilt: Float,
    mapPosition: LatLon,
    mapMetersPerDp: Double,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
    onSetPinPosition: (icon: DrawableResource, position: LatLon?) -> Unit,
    modifier: Modifier = Modifier,
    countryBoundaries: CountryBoundaries = koinInject(),
    countryInfos: CountryInfos = koinInject(),
) {
    val geometry = geometry ?: ElementPointGeometry(mapPosition)
    val countryInfo = remember { countryInfos.get(countryBoundaries, geometry.center) }
    var state by rememberSerializable { mutableStateOf<OverlayFormState>(OverlayFormState.Overlay) }

    // markers shown are per-form
    LaunchedEffect(state) { onSetMapMarkers(emptyList()) }

    fun onAction(action: OverlayAction) {
        when (action) {
            Action.Dismiss -> onDismiss()
            Action.LeaveNote -> state = OverlayFormState.LeaveNote
            Action.SplitWay -> state = OverlayFormState.SplitWay
            Action.MoveNode -> state = OverlayFormState.MoveNode
            is Edit -> onEdit(action.value)
        }
    }

    CompositionLocalProvider(
        LocalElement provides element,
        LocalMapRotation provides mapRotation,
        LocalMapTilt provides mapTilt,
        LocalMapMetersPerDp provides mapMetersPerDp,
        LocalMapMarkersCallback provides onSetMapMarkers
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = ReplaceBottomSheetTransitionSpec,
            modifier = modifier,
        ) { currentState ->
            when (currentState) {
                OverlayFormState.Overlay -> {
                    overlay.Form(
                        on = ::onAction,
                        element = element,
                        geometry = geometry,
                        countryInfo = countryInfo,
                        onSetPinPosition = onSetPinPosition
                    )
                }
                OverlayFormState.LeaveNote -> {
                    LeaveNoteInsteadForm(
                        onLeaveNote = { text, noteImagePaths ->
                            onLeaveNote(text, noteImagePaths)
                        },
                        onDismiss = onDismiss,
                        editType = overlay,
                        element = element,
                    )
                }
                OverlayFormState.SplitWay -> {
                    SplitWayForm(
                        onConfirmed = { onEdit(SplitWayAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        way = element as Way,
                        wayGeometry = geometry as ElementPolylinesGeometry,
                    )
                }
                OverlayFormState.MoveNode -> {
                    MoveNodeForm(
                        onConfirmed = { onEdit(MoveNodeAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        nodeOffsetInWindow = geometryOffsetInWindow,
                        node = element as Node,
                        elementEditType = overlay,
                    )
                }
            }
        }
    }
}

@Serializable
private enum class OverlayFormState {
    Overlay,
    LeaveNote,
    SplitWay,
    MoveNode
}

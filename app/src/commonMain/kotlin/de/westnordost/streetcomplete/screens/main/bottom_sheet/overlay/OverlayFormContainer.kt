package de.westnordost.streetcomplete.screens.main.bottom_sheet.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import de.westnordost.streetcomplete.data.overlays.ShownOverlayForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.BottomSheetFormState
import de.westnordost.streetcomplete.screens.main.bottom_sheet.BottomSheetSubForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node.MoveNodeForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.LeaveNoteInsteadForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way.SplitWayForm
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.LocalLastMapClick
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.util.ReplaceBottomSheetTransitionSpec
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import org.koin.compose.koinInject

/** Container in which all overlay forms are housed.
 *
 *  Takes care of showing the forms for the "other answers" (leave note, split way, move node),
 *  animates between the overlay form and those.
 *
 *  @param onSetMapMarkers is called when the form shown wishes to show markers on the map. E.g. the
 *         split way form shows markers.
 *
 *  @param formState which form is shown, shared with what it places on the map
 *  */
@Composable
fun OverlayFormContainer(
    onDismiss: () -> Unit,
    onEdit: (action: ElementEditAction) -> Unit,
    onLeaveNote: (noteText: String, noteImagePaths: List<String>) -> Unit,
    overlay: Overlay,
    element: Element?,
    geometry: ElementGeometry?,
    mapRotation: Float,
    mapTilt: Float,
    mapPosition: LatLon,
    mapMetersPerDp: Double,
    onSetMapMarkers: (Iterable<Marker>?) -> Unit,
    formState: BottomSheetFormState,
    /** The overlay's form for [element], see [Overlay.rememberForm] */
    form: ShownOverlayForm,
    lastMapClick: MapClick?,
    modifier: Modifier = Modifier,
    countryBoundaries: CountryBoundaries = koinInject(),
    countryInfos: CountryInfos = koinInject(),
) {
    val geometry = geometry ?: ElementPointGeometry(mapPosition)
    val countryInfo = remember { countryInfos.get(countryBoundaries, geometry.center) }
    fun showForm(form: BottomSheetSubForm) {
        formState.subForm = form
        onSetMapMarkers(null)
    }

    fun onAction(action: OverlayAction) {
        when (action) {
            Action.Dismiss -> onDismiss()
            Action.LeaveNote -> showForm(BottomSheetSubForm.LeaveNote)
            Action.SplitWay -> showForm(BottomSheetSubForm.SplitWay)
            Action.MoveNode -> showForm(BottomSheetSubForm.MoveNode)
            is Edit -> onEdit(action.value)
        }
    }

    AnimatedContent(
        targetState = formState.subForm,
        transitionSpec = ReplaceBottomSheetTransitionSpec,
        modifier = modifier,
    ) { currentState ->
        CompositionLocalProvider(
            LocalElement provides element,
            LocalMapRotation provides mapRotation,
            LocalMapTilt provides mapTilt,
            LocalMapMetersPerDp provides mapMetersPerDp,
            LocalLastMapClick provides lastMapClick,
            // AnimatedContent keeps the outgoing form alive until its transition ends.
            LocalMapMarkersCallback provides { if (currentState == formState.subForm) onSetMapMarkers(it) },
        ) {
            when (currentState) {
                BottomSheetSubForm.Main -> {
                    with(form) { Content(::onAction, geometry, countryInfo) }
                }
                BottomSheetSubForm.LeaveNote -> {
                    LeaveNoteInsteadForm(
                        onLeaveNote = { text, noteImagePaths ->
                            onLeaveNote(text, noteImagePaths)
                        },
                        onDismiss = onDismiss,
                        editType = overlay,
                        element = element,
                    )
                }
                BottomSheetSubForm.SplitWay -> {
                    SplitWayForm(
                        onConfirmed = { onEdit(SplitWayAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        way = element as Way,
                        wayGeometry = geometry as ElementPolylinesGeometry,
                        snipAnimation = formState.snipAnimation,
                    )
                }
                BottomSheetSubForm.MoveNode -> {
                    MoveNodeForm(
                        onConfirmed = { onEdit(MoveNodeAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        node = element as Node,
                    )
                }
            }
        }
    }
}


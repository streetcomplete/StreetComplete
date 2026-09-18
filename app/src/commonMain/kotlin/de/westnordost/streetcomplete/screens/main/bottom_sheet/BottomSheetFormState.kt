package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.overlays.ShownOverlayForm
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import kotlinx.serialization.Serializable

/** Which form the bottom sheet shows for the selected object: the quest's or overlay's own form,
 *  or one of the alternatives reachable from it */
@Serializable
enum class BottomSheetSubForm { Main, LeaveNote, SplitWay, MoveNode }

/** The state of the bottom sheet's form that is read both in the sheet and on the map: which
 *  form is shown and, for what the forms place on the map, their state. See [MainBottomSheet]
 *  and [MainBottomSheetMapOverlay]. */
@Stable
class BottomSheetFormState internal constructor(
    subForm: MutableState<BottomSheetSubForm>,
    /** The overlay's form, if an overlay element or a new element is selected */
    val overlayForm: ShownOverlayForm?,
) {
    var subForm by subForm

    /** Progress of the scissors' snip when splitting a way */
    val snipAnimation = Animatable(0f)
}

@Composable
fun rememberBottomSheetFormState(shownBottomSheet: ShownBottomSheet?): BottomSheetFormState {
    val subForm = rememberSerializable { mutableStateOf(BottomSheetSubForm.Main) }
    val overlayForm = (shownBottomSheet as? ShownBottomSheet.Overlay)?.let {
        it.overlay.rememberForm(it.element)
    }
    return remember(overlayForm) { BottomSheetFormState(subForm, overlayForm) }
}

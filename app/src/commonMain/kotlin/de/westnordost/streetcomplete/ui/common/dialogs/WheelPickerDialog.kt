package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.ui.common.WheelPicker
import de.westnordost.streetcomplete.ui.common.rememberWheelPickerState
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import org.jetbrains.compose.resources.stringResource

/** Dialog to pick from a number of values */
@Composable fun <I> WheelPickerDialog(
    onDismissRequest: () -> Unit,
    selectableValues: List<I>,
    onSelected: (I) -> Unit,
    itemContent: @Composable (I) -> Unit,
    modifier: Modifier = Modifier,
    selectedInitialValue: I? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButtonText: String = stringResource(Res.string.ok),
    cancelButtonText: String = stringResource(Res.string.cancel),
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties()
) {
    val selectedInitialIndex = remember {
        selectedInitialValue?.let { selectableValues.indexOf(it) }?.takeIf { it >= 0 }
    }
    val wheelPickerState = rememberWheelPickerState(selectedItemIndex = selectedInitialIndex ?: 0)
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = { onSelected(selectableValues[wheelPickerState.selectedItemIndex]) },
        modifier = modifier,
        title = title,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                text?.invoke()
                ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
                    WheelPicker(
                        items = selectableValues,
                        modifier = Modifier.fillMaxWidth(),
                        state = wheelPickerState,
                        content = itemContent
                    )
                }
            }
        },
        confirmButtonText = confirmButtonText,
        cancelButtonText = cancelButtonText,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        properties = properties,
    )
}

package de.westnordost.streetcomplete.ui.common.auto_complete_text

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import kotlin.math.max

/** A TextField that suggests auto-completion based on the supplied [suggestions] */
@Composable
fun AutoCompleteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<String> = emptyList(),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = TextFieldDefaults.TextFieldShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val verticalMarginInPx = with(density) { MenuVerticalMargin.roundToPx() }

    var isFocused by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    var width by remember { mutableIntStateOf(0) }
    var maxMenuHeight by remember { mutableIntStateOf(0) }
    val coordinates = remember { Ref<LayoutCoordinates>() }

    LaunchedEffect(windowInfo.containerSize) {
        val layoutCoordinates = coordinates.value
        if (layoutCoordinates != null) {
            maxMenuHeight = getMaxMenuHeight(
                windowBounds = windowInfo.containerSize.toIntRect(),
                coordinates = layoutCoordinates,
            ) - verticalMarginInPx
        }
    }

    Box(modifier = modifier
        .onGloballyPositioned { layoutCoordinates ->
            width = layoutCoordinates.size.width
            coordinates.value = layoutCoordinates
            maxMenuHeight = getMaxMenuHeight(
                windowBounds = windowInfo.containerSize.toIntRect(),
                coordinates = layoutCoordinates,
            ) - verticalMarginInPx
        }
    ) {
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (!isExpanded) isExpanded = true
            },
            modifier = Modifier.onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
        )

        val expanded = isFocused && isExpanded && suggestions.isNotEmpty()

        val expandedStates = remember { MutableTransitionState(false) }
        expandedStates.targetState = expanded

        if (expandedStates.currentState || expandedStates.targetState) {
            val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
            val popupPositionProvider =
                DropdownMenuPositionProvider(DpOffset.Zero, density) { parentBounds, menuBounds ->
                    transformOriginState.value = calculateTransformOrigin(parentBounds, menuBounds)
                }

            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = { isExpanded = false },
            ) {
                DropdownMenuContent(
                    expandedStates = expandedStates,
                    transformOriginState =  transformOriginState,
                    scrollState = rememberScrollState(),
                    modifier = modifier
                        .width(width.pxToDp())
                        .heightIn(max = maxMenuHeight.pxToDp()),
                ) {
                    for (suggestion in suggestions.filter { it.startsWith(value.text, ignoreCase = true) }) {
                        DropdownMenuItem(onClick = {
                            onValueChange(value.copy(
                                text = suggestion,
                                selection = TextRange(suggestion.length)
                            ))
                            isExpanded = false
                        }) {
                            Text(suggestion)
                        }
                    }
                }
            }
        }
    }
}

private fun getMaxMenuHeight(windowBounds: IntRect, coordinates: LayoutCoordinates): Int {
    val heightAbove = coordinates.boundsInWindow().top - windowBounds.top
    val heightBelow = windowBounds.bottom - windowBounds.top - coordinates.boundsInWindow().bottom
    return max(heightAbove, heightBelow).toInt()
}

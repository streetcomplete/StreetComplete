package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.PopupProperties
import kotlin.text.startsWith

/** A TextField that suggests auto-completion based on the supplied [suggestions] */
@OptIn(ExperimentalMaterialApi::class)
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
    var isFocused by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
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
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { /* closes automatically when text field is not focused */ },
            properties = PopupProperties(focusable = false),
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

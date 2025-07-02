package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.material.TextFieldDefaults.outlinedTextFieldPadding
import androidx.compose.material.TextFieldDefaults.textFieldWithLabelPadding
import androidx.compose.material.TextFieldDefaults.textFieldWithoutLabelPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.ui.ktx.calculateTextMaxFontSize
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class TextFieldStyle { Filled, Outlined }

/**
 * Same as [androidx.compose.material.TextField], with the following enhancements:
 *
 * - whether it is outlined or filled can be set with the [style] parameter, making it less
 *   cumbersome to create specialized [TextField2]s independent of styling.
 *
 * - if [autoFitFontSize] is true and it is a single line text field, the text will be shrunk
 *   to fit the given maximum width. (The limitation to single line text fields is because
 *   I didn't manage to make it work on multi-line text fields)
 *
 * The other code has been copied from the current state of [androidx.compose.material.TextField] /
 * [androidx.compose.material.OutlinedTextField] in June 2025
 **/
@Composable
fun TextField2(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextFieldStyle = TextFieldStyle.Filled,
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
    shape: Shape = style.shape,
    colors: TextFieldColors = style.colors,
    contentPadding: PaddingValues = style.getContentPadding(label != null),
    autoFitFontSize: Boolean = false,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse { colors.textColor(enabled).value }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    AutoFitFontSize(
        autoSize = autoFitFontSize,
        value = value,
        textStyle = mergedTextStyle,
        contentPadding = contentPadding,
        maxLines = maxLines,
        modifier = modifier,
    ) { scaledTextStyle ->
        @OptIn(ExperimentalMaterialApi::class)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.textFieldDefaults(
                style = style,
                enabled = enabled,
                label = label,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                density = LocalDensity.current,
            ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = scaledTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.cursorColor(isError).value),
            decorationBox = style.getDecorationBox(
                value = value,
                enabled = enabled,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                interactionSource = interactionSource,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding
            ),
        )
    }
}

@Composable
fun TextField2(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    style: TextFieldStyle = TextFieldStyle.Filled,
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
    shape: Shape = style.shape,
    colors: TextFieldColors = style.colors,
    contentPadding: PaddingValues = style.getContentPadding(label != null),
    autoFitFontSize: Boolean = false,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse { colors.textColor(enabled).value }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    AutoFitFontSize(
        autoSize = autoFitFontSize,
        value = value.text,
        textStyle = mergedTextStyle,
        contentPadding = contentPadding,
        maxLines = maxLines,
        modifier = modifier,
    ) { scaledTextStyle ->
        @OptIn(ExperimentalMaterialApi::class)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.textFieldDefaults(
                style = style,
                enabled = enabled,
                label = label,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                density = LocalDensity.current,
            ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = scaledTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.cursorColor(isError).value),
            decorationBox = style.getDecorationBox(
                value = value.text,
                enabled = enabled,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                interactionSource = interactionSource,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding
            ),
        )
    }
}

@Composable
private fun AutoFitFontSize(
    autoSize: Boolean,
    value: String,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    maxLines: Int,
    modifier: Modifier = Modifier,
    content: @Composable (textStyle: TextStyle) -> Unit,
) {
    if (autoSize) {
        BoxWithConstraints(modifier) {
            val fontSize = calculateTextMaxFontSize(
                text = value,
                textStyle = textStyle,
                contentPadding = contentPadding,
                maxLines = maxLines
            )
            content(textStyle.copy(fontSize = fontSize))
        }
    } else {
        Box(modifier) {
            content(textStyle)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun Modifier.textFieldDefaults(
    style: TextFieldStyle,
    enabled: Boolean,
    label: @Composable (() -> Unit)?,
    isError: Boolean,
    interactionSource: MutableInteractionSource,
    colors: TextFieldColors,
    density: Density,
): Modifier =
    when (style) {
        TextFieldStyle.Filled -> indicatorLine(enabled, isError, interactionSource, colors)
        TextFieldStyle.Outlined -> then(
            if (label != null) {
                Modifier
                    // Merge semantics at the beginning of the modifier chain to ensure
                    // padding is considered part of the text field.
                    .semantics(mergeDescendants = true) {}
                    .padding(top = with(density) { OutlinedTextFieldTopPadding.toDp() })
            } else {
                Modifier
            }
        )
    }
        .defaultErrorSemantics(isError, "Error")
        .defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
            minHeight = TextFieldDefaults.MinHeight
        )

private val OutlinedTextFieldTopPadding = 8.sp

private fun Modifier.defaultErrorSemantics(
    isError: Boolean,
    defaultErrorMessage: String,
): Modifier = if (isError) semantics { error(defaultErrorMessage) } else this

val TextFieldStyle.shape : Shape @Composable @ReadOnlyComposable
get() = when (this) {
        TextFieldStyle.Filled -> TextFieldDefaults.TextFieldShape
        TextFieldStyle.Outlined -> TextFieldDefaults.OutlinedTextFieldShape
    }

val TextFieldStyle.colors : TextFieldColors @Composable
get() = when (this) {
        TextFieldStyle.Filled -> TextFieldDefaults.textFieldColors()
        TextFieldStyle.Outlined -> TextFieldDefaults.outlinedTextFieldColors()
    }

@OptIn(ExperimentalMaterialApi::class)
fun TextFieldStyle.getContentPadding(hasLabel: Boolean): PaddingValues = when (this) {
    TextFieldStyle.Filled -> {
        if (!hasLabel) textFieldWithoutLabelPadding()
        else textFieldWithLabelPadding()
    }
    TextFieldStyle.Outlined -> {
        outlinedTextFieldPadding()
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun TextFieldStyle.getDecorationBox(
    value: String,
    enabled: Boolean,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    isError: Boolean,
    visualTransformation: VisualTransformation,
    singleLine: Boolean,
    interactionSource: MutableInteractionSource,
    shape: Shape,
    colors: TextFieldColors,
    contentPadding: PaddingValues,
) : @Composable (innerTextField: @Composable () -> Unit) -> Unit = { innerTextField ->
    when (this) {
        TextFieldStyle.Filled ->
            TextFieldDefaults.TextFieldDecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                isError = isError,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding
            )
        TextFieldStyle.Outlined ->
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                isError = isError,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding,
                border = {
                    TextFieldDefaults.BorderBox(
                        enabled = enabled,
                        isError = isError,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = shape
                    )
                },
            )
    }
}

@Preview @Composable
private fun AutoFitFontSizeTextFieldPreview() {
    var text by remember { mutableStateOf("") }
    AppTheme {
        TextField2(
            value = text,
            onValueChange = { text = it },
            textStyle = MaterialTheme.typography.h3,
            modifier = Modifier.width(150.dp),
            singleLine = true,
            autoFitFontSize = true,
        )
    }
}

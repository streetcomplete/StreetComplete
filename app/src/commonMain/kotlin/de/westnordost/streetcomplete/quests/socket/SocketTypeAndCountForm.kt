package de.westnordost.streetcomplete.quests.socket

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.StepperButton
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val NarrowLayoutMaxWidth = 360.dp

@Composable
fun SocketTypeAndCountForm(
    socketTypes: List<SocketType>,
    counts: Map<SocketType, Int?>,
    onCountsChanged: (Map<SocketType, Int?>) -> Unit,
    modifier: Modifier = Modifier,
    unresolvedYesTypes: Set<SocketType> = emptySet(),
    domesticIcons: List<DrawableResource> = emptyList(),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        socketTypes.forEach { type ->
            val count = counts[type]
            val icons = if (type == SocketType.DOMESTIC && domesticIcons.isNotEmpty()) {
                domesticIcons
            } else {
                listOf(type.icon)
            }
            SocketRow(
                type = type,
                icons = icons,
                count = count,
                needsResolution = type in unresolvedYesTypes && count == null,
                onCountChange = { onCountsChanged(counts + (type to it)) },
            )
        }
    }
}

@Composable
private fun SocketRow(
    type: SocketType,
    icons: List<DrawableResource>,
    count: Int?,
    needsResolution: Boolean,
    onCountChange: (Int?) -> Unit,
) {
    val title = stringResource(type.title)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val narrow = maxWidth < NarrowLayoutMaxWidth
        if (narrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SocketIdentification(
                    type = type,
                    icons = icons,
                    title = title,
                    needsResolution = needsResolution,
                    modifier = Modifier.fillMaxWidth()
                )
                SocketCountControls(
                    typeTitle = title,
                    count = count,
                    onCountChange = onCountChange,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocketIdentification(
                    type = type,
                    icons = icons,
                    title = title,
                    needsResolution = needsResolution,
                    modifier = Modifier.weight(1f)
                )
                SocketCountControls(
                    typeTitle = title,
                    count = count,
                    onCountChange = onCountChange,
                )
            }
        }
    }
}

@Composable
private fun SocketIdentification(
    type: SocketType,
    icons: List<DrawableResource>,
    title: String,
    needsResolution: Boolean,
    modifier: Modifier = Modifier,
) {
    val iconSize = if (icons.size > 1) 36.dp else 48.dp
    // Monochrome black connector/domestic drawings: tint with content color for dark-mode contrast.
    val iconTint = LocalContentColor.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icons.forEach { icon ->
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                colorFilter = ColorFilter.tint(iconTint),
            )
        }
        if (type.euLabels.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                type.euLabels.forEach { label ->
                    EuConnectorLabel(
                        drawable = label,
                        needsLightBackdrop = type.hasBlackEuLabels
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                style = MaterialTheme.typography.body2,
            )
            if (needsResolution) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = stringResource(Res.string.quest_generic_hasFeature_yes),
                        style = MaterialTheme.typography.caption,
                    )
                }
            }
        }
    }
}

@Composable
private fun SocketCountControls(
    typeTitle: String,
    count: Int?,
    onCountChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countInputDescription = stringResource(
        Res.string.quest_charging_station_socket_count_input,
        typeTitle
    )
    val increaseDescription = stringResource(
        Res.string.quest_charging_station_socket_increase,
        typeTitle
    )
    val decreaseDescription = stringResource(
        Res.string.quest_charging_station_socket_decrease,
        typeTitle
    )
    val stepperMax = socketCountStepperMax(count)
    val value = count ?: 0

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TextField2(
            value = formatSocketCountInput(count),
            onValueChange = { text ->
                when (val parsed = parseSocketCountInput(text)) {
                    ParseSocketCountResult.Cleared -> onCountChange(null)
                    ParseSocketCountResult.Invalid -> Unit // keep previous; no silent clamp
                    is ParseSocketCountResult.Value -> onCountChange(parsed.count)
                }
            },
            modifier = Modifier
                .widthIn(min = 72.dp, max = 112.dp)
                .semantics { contentDescription = countInputDescription },
            singleLine = true,
            style = TextFieldStyle.Outlined,
            textStyle = MaterialTheme.typography.h6.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        StepperButton(
            onIncrease = { onCountChange(increaseSocketCount(count)) },
            onDecrease = { onCountChange(decreaseSocketCount(count)) },
            increaseEnabled = value < stepperMax,
            decreaseEnabled = count == null || value > 0,
            increaseContent = {
                Icon(
                    painter = painterResource(Res.drawable.ic_up_24),
                    contentDescription = increaseDescription,
                )
            },
            decreaseContent = {
                Icon(
                    painter = painterResource(Res.drawable.ic_down_24),
                    contentDescription = decreaseDescription,
                )
            },
        )
    }
}

@Composable
private fun EuConnectorLabel(
    drawable: DrawableResource,
    needsLightBackdrop: Boolean,
    modifier: Modifier = Modifier
) {
    val darkTheme = !MaterialTheme.colors.isLight
    // Official black EU marks need a light plate in dark mode; do not tint/invert the artwork.
    val labelModifier = if (needsLightBackdrop && darkTheme) {
        modifier
            .background(Color(0xFFE8E8E8), RoundedCornerShape(4.dp))
            .padding(2.dp)
            .size(28.dp)
    } else {
        modifier.size(28.dp)
    }
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = labelModifier
    )
}

@PreviewLightDark
@Preview(name = "Narrow 320dp", widthDp = 320)
@PreviewFontScale
@Composable
private fun SocketTypeAndCountFormPreview() {
    AppTheme {
        var counts by remember {
            mutableStateOf<Map<SocketType, Int?>>(
                mapOf(
                    SocketType.TYPE2 to 2,
                    SocketType.TYPE2_CABLE to 0,
                    SocketType.TYPE2_COMBO to 1,
                    SocketType.CHADEMO to 123,
                    SocketType.DOMESTIC to null
                )
            )
        }
        SocketTypeAndCountForm(
            socketTypes = SocketType.entries,
            counts = counts,
            onCountsChanged = { counts = it },
            unresolvedYesTypes = setOf(SocketType.DOMESTIC),
            domesticIcons = listOf(
                Res.drawable.socket_domestic,
                Res.drawable.socket_domestic_typec,
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@PreviewLightDark
@Preview(name = "Combo CHAdeMO narrow", widthDp = 320)
@Composable
private fun SocketTypeComboChademoPreview() {
    AppTheme {
        SocketTypeAndCountForm(
            socketTypes = listOf(SocketType.TYPE2_COMBO, SocketType.CHADEMO),
            counts = mapOf(SocketType.TYPE2_COMBO to 2, SocketType.CHADEMO to 1),
            onCountsChanged = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

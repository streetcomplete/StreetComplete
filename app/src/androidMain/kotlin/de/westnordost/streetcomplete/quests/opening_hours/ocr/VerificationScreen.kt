package de.westnordost.streetcomplete.quests.opening_hours.ocr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.WheelPicker
import de.westnordost.streetcomplete.ui.common.rememberWheelPickerState

private data class EditableHours(
    val dayGroup: DayGroup,
    val openHour: Int,
    val openMinute: Int,
    val closeHour: Int,
    val closeMinute: Int,
    val isAm: Boolean, // for open time
    val isClosePm: Boolean, // for close time
    val isClosed: Boolean = false
)

@Composable
fun VerificationScreen(
    state: OcrFlowState,
    onStateChange: (OcrFlowState) -> Unit,
    onApply: (OcrOpeningHoursResult) -> Unit,
    onBack: () -> Unit
) {
    var is12HourMode by rememberSaveable { mutableStateOf(state.is12HourMode) }

    // Initialize editable hours from annotations
    val editableHours = remember {
        mutableStateListOf<EditableHours>().apply {
            addAll(state.dayGroups.map { dayGroup ->
                val annotation = state.annotations.find { it.dayGroup == dayGroup }
                val ocrProcessor = OcrProcessor()

                // Check if annotation is marked as closed
                val isClosed = annotation?.isClosed == true

                // Parse OCR results (only if not closed)
                val openParsed = if (!isClosed) {
                    annotation?.openTimeRaw?.let {
                        ocrProcessor.parseTimeNumbers(it, isAm = true, is12HourMode = true)
                    }
                } else null

                val closeParsed = if (!isClosed) {
                    annotation?.closeTimeRaw?.let {
                        ocrProcessor.parseTimeNumbers(it, isAm = false, is12HourMode = true)
                    }
                } else null

                EditableHours(
                    dayGroup = dayGroup,
                    openHour = openParsed?.first ?: 9,
                    openMinute = openParsed?.second ?: 0,
                    closeHour = closeParsed?.first ?: 17,
                    closeMinute = closeParsed?.second ?: 0,
                    isAm = true,
                    isClosePm = true,
                    isClosed = isClosed
                )
            })
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.quest_openingHours_ocr_verify_title)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onBack) { BackIcon() } },
            actions = {
                // 12hr/24hr toggle
                Button(
                    onClick = { is12HourMode = !is12HourMode },
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(
                        if (is12HourMode) {
                            stringResource(R.string.quest_openingHours_ocr_12hr_mode)
                        } else {
                            stringResource(R.string.quest_openingHours_ocr_24hr_mode)
                        }
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.quest_openingHours_ocr_verify_description),
                style = MaterialTheme.typography.body2
            )

            Spacer(modifier = Modifier.height(16.dp))

            editableHours.forEachIndexed { index, hours ->
                DayHoursCard(
                    hours = hours,
                    is12HourMode = is12HourMode,
                    onHoursChange = { updated ->
                        editableHours[index] = updated
                    }
                )

                if (index < editableHours.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Apply button
        Column(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                )
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    // Convert to verified hours with 24-hour format
                    val verifiedHours = editableHours.map { hours ->
                        val (openH, openM) = if (is12HourMode) {
                            convert12to24(hours.openHour, hours.openMinute, hours.isAm)
                        } else {
                            hours.openHour to hours.openMinute
                        }

                        val (closeH, closeM) = if (is12HourMode) {
                            convert12to24(hours.closeHour, hours.closeMinute, !hours.isClosePm)
                        } else {
                            hours.closeHour to hours.closeMinute
                        }

                        VerifiedHours(
                            dayGroup = hours.dayGroup,
                            openHour = openH,
                            openMinute = openM,
                            closeHour = closeH,
                            closeMinute = closeM,
                            isClosed = hours.isClosed
                        )
                    }

                    onApply(OcrOpeningHoursResult(verifiedHours))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.quest_openingHours_ocr_apply))
            }
        }
    }
}

@Composable
private fun DayHoursCard(
    hours: EditableHours,
    is12HourMode: Boolean,
    onHoursChange: (EditableHours) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hours.dayGroup.toDisplayString(),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hours.isClosed,
                        onCheckedChange = { checked ->
                            onHoursChange(hours.copy(isClosed = checked))
                        }
                    )
                    Text(
                        text = stringResource(R.string.quest_openingHours_ocr_closed),
                        style = MaterialTheme.typography.body2
                    )
                }
            }

            if (!hours.isClosed) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Open time
                TimePickerRow(
                    label = stringResource(R.string.quest_openingHours_ocr_open_label),
                    hour = hours.openHour,
                    minute = hours.openMinute,
                    isAm = hours.isAm,
                    is12HourMode = is12HourMode,
                    onTimeChange = { h, m, am ->
                        onHoursChange(hours.copy(openHour = h, openMinute = m, isAm = am))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Close time
                TimePickerRow(
                    label = stringResource(R.string.quest_openingHours_ocr_close_label),
                    hour = hours.closeHour,
                    minute = hours.closeMinute,
                    isAm = !hours.isClosePm,
                    is12HourMode = is12HourMode,
                    onTimeChange = { h, m, am ->
                        onHoursChange(hours.copy(closeHour = h, closeMinute = m, isClosePm = !am))
                    }
                )
            }
        }
    }
}

@Composable
private fun TimePickerRow(
    label: String,
    hour: Int,
    minute: Int,
    isAm: Boolean,
    is12HourMode: Boolean,
    onTimeChange: (hour: Int, minute: Int, isAm: Boolean) -> Unit
) {
    val hours12 = (1..12).toList()
    val hours24 = (0..23).toList()
    val minutes = listOf(0, 30)

    val displayHour = if (is12HourMode) {
        when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
    } else hour

    val hourState = rememberWheelPickerState(
        selectedItemIndex = if (is12HourMode) {
            hours12.indexOf(displayHour).coerceAtLeast(0)
        } else {
            hours24.indexOf(hour).coerceAtLeast(0)
        }
    )

    val minuteState = rememberWheelPickerState(
        selectedItemIndex = minutes.indexOf(minute).coerceAtLeast(0)
    )

    // Update when wheel changes
    LaunchedEffect(hourState.selectedItemIndex, minuteState.selectedItemIndex) {
        val newHour = if (is12HourMode) {
            hours12.getOrElse(hourState.selectedItemIndex) { 9 }
        } else {
            hours24.getOrElse(hourState.selectedItemIndex) { 9 }
        }
        val newMinute = minutes.getOrElse(minuteState.selectedItemIndex) { 0 }
        onTimeChange(newHour, newMinute, isAm)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.width(60.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Hour picker
        WheelPicker(
            items = if (is12HourMode) hours12 else hours24,
            state = hourState,
            modifier = Modifier.width(60.dp),
            visibleAdjacentItems = 1
        ) { item ->
            Text(
                text = item.toString().padStart(2, '0'),
                style = MaterialTheme.typography.h6
            )
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Minute picker
        WheelPicker(
            items = minutes,
            state = minuteState,
            modifier = Modifier.width(60.dp),
            visibleAdjacentItems = 1
        ) { item ->
            Text(
                text = item.toString().padStart(2, '0'),
                style = MaterialTheme.typography.h6
            )
        }

        // AM/PM toggle (only in 12-hour mode)
        if (is12HourMode) {
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onTimeChange(hour, minute, !isAm) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier.width(60.dp)
            ) {
                Text(if (isAm) "AM" else "PM")
            }
        }
    }
}

private fun convert12to24(hour: Int, minute: Int, isAm: Boolean): Pair<Int, Int> {
    val hour24 = when {
        hour == 12 && isAm -> 0
        hour == 12 && !isAm -> 12
        !isAm -> hour + 12
        else -> hour
    }
    return hour24 to minute
}

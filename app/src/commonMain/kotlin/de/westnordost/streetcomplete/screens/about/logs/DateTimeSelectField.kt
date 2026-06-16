package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ClearIcon
import de.westnordost.streetcomplete.ui.common.DateSelectDialog
import de.westnordost.streetcomplete.ui.common.TimeSelectDialog
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource

@Composable
fun DateTimeSelectField(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
) {
    val dateTimeFormatter = LocalDateTimeFormatter(
        dateStyle = DateTimeFormatStyle.Medium,
        timeStyle = DateTimeFormatStyle.Short
    )

    val initialDateTime = value ?: LocalDateTime.now()
    val initialYear = initialDateTime.date.year

    var showDateTimeSelectionDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // a little hack: Compose text fields swallow the click event, so adding Modifier.clickable
    // will not work making it clickable. But we can listen in to the interaction source and when
    // it is clicked, do something...
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Release -> {
                    showDateTimeSelectionDialog = true
                }
            }
        }
    }

    if (showDateTimeSelectionDialog) {
        if (selectedDate == null) {
            DateSelectDialog(
                onDismissRequest = { if (selectedDate == null) showDateTimeSelectionDialog = false },
                onSelect = { selectedDate = it },
                initialDate = initialDateTime.date,
                years = (initialYear - 1)..(initialYear + 1),
            )
        } else {
            TimeSelectDialog(
                onDismissRequest = { showDateTimeSelectionDialog = false },
                onSelect = { hour, minutes ->
                    val time = LocalTime(hour, minutes)
                    val date = selectedDate ?: return@TimeSelectDialog
                    onValueChange(LocalDateTime(date, time))
                    selectedDate = null
                },
                initialHour = initialDateTime.time.hour,
                initialMinutes = initialDateTime.time.minute
            )
        }
    }

    OutlinedTextField(
        value = value?.let { dateTimeFormatter.format(it) }.orEmpty(),
        onValueChange = { },
        modifier = modifier,
        readOnly = true,
        label = label,
        leadingIcon = { Icon(painterResource(Res.drawable.ic_calendar_month_24), null) },
        trailingIcon = if (value != null) {
            { IconButton(onClick = { onValueChange(null) }) { ClearIcon() } }
        } else {
            null
        },
        interactionSource = interactionSource
    )
}

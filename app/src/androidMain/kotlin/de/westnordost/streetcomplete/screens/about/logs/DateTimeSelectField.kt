package de.westnordost.streetcomplete.screens.about.logs

import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ClearIcon
import de.westnordost.streetcomplete.ui.common.DateSelectDialog
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import kotlin.coroutines.resume

@Composable
fun DateTimeSelectField(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val dateFormatter = LocalDateTimeFormatter(
        dateStyle = DateTimeFormatStyle.Medium,
        timeStyle = DateTimeFormatStyle.Short
    )
    val context = LocalContext.current

    var showDateDialog by remember { mutableStateOf(false) }
    val initialDateTime = value ?: LocalDateTime.now()

    // a little hack: Compose text fields swallow the click event, so adding Modifier.clickable
    // will not work making it clickable. But we can listen in to the interaction source and when
    // it is clicked, do something...
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Release -> {
                    showDateDialog = true
                }
            }
        }
    }

    if (showDateDialog) {
        DateSelectDialog(
            onDismissRequest = { showDateDialog = false },
            onSelect = { date ->
                scope.launch {
                    val time = context.pickTime(initialDateTime.time)
                    onValueChange(LocalDateTime(date, time))
                }
            },
            initialDate = initialDateTime.date,
        )
    }

    OutlinedTextField(
        value = value?.let { dateFormatter.format(it) }.orEmpty(),
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

// TODO Compose - replace with a TimeSelectDialog using the custom TimePicker composable

private suspend fun Context.pickTime(initialTime: LocalTime): LocalTime =
    suspendCancellableCoroutine { cont ->
        TimePickerDialog(
            this,
            R.style.Theme_Bubble_Dialog_DatePicker,
            { _, hourOfDay, minute ->
                cont.resume(LocalTime(hourOfDay, minute))
            },
            initialTime.hour,
            initialTime.minute,
            true
        ).show()
    }

package de.westnordost.streetcomplete.screens.about.logs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.ClearIcon
import de.westnordost.streetcomplete.util.dateTimeToString
import de.westnordost.streetcomplete.util.ktx.now
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun DateTimeSelectField(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val locale = Locale.getDefault()
    val context = LocalContext.current
    OutlinedTextField(
        value = value?.let { dateTimeToString(locale, it) } ?: "",
        onValueChange = { },
        // TODO Compose modifier.clickable { } does not work, so, need to click on icon
        modifier = modifier,
        readOnly = true,
        label = label,
        leadingIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    onValueChange(context.pickDateTime(value ?: LocalDateTime.now()))
                }
            }) {
                Icon(painterResource(R.drawable.ic_calendar_month_24dp), null)
            }
        },
        trailingIcon = if (value != null) {
            { IconButton(onClick = { onValueChange(null) }) { ClearIcon() } }
        } else {
            null
        }
    )
}

// TODO Compose - Jetpack Compose is still lacking Date and Time Pickers and dialogs

private suspend fun Context.pickDateTime(initialDateTime: LocalDateTime): LocalDateTime {
    val date = pickDate(initialDateTime.date)
    val time = pickTime(initialDateTime.time)

    return LocalDateTime(date, time)
}

private suspend fun Context.pickDate(initialDate: LocalDate): LocalDate =
    // LocalDate works with with month *number* (1-12), while Android date picker dialog works
    // with month *index*, hence the +1 / -1
    suspendCancellableCoroutine { cont ->
        DatePickerDialog(
            this,
            R.style.Theme_Bubble_Dialog_DatePicker,
            { _, year, monthIndex, dayOfMonth ->
                cont.resume(LocalDate(year, monthIndex + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthNumber - 1,
            initialDate.dayOfMonth
        ).show()
    }

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

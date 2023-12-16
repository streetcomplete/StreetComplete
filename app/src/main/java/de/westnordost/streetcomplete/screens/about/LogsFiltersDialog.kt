package de.westnordost.streetcomplete.screens.about

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogLevel
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.databinding.DialogLogsFiltersBinding
import de.westnordost.streetcomplete.util.dateTimeToString
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.view.dialogs.TimePickerDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.util.Locale
import kotlin.coroutines.resume

class LogsFiltersDialog(
    private val context: Context,
    initialFilters: LogsFilters,
    onApplyButtonClick: (filters: LogsFilters) -> Unit
) : AlertDialog(context) {

    private val filters = initialFilters.copy()
    private val binding = DialogLogsFiltersBinding.inflate(LayoutInflater.from(context))
    private val locale = Locale.getDefault()

    init {
        setView(binding.root)

        setButton(BUTTON_POSITIVE, context.getString(R.string.action_filter)) { _, _ ->
            onApplyButtonClick(filters)
            dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.action_reset)) { _, _ ->
            onApplyButtonClick(LogsFilters())
            cancel()
        }

        createLogLevelsChips()

        binding.messageContainsEditText.setText(filters.messageContains)
        binding.messageContainsEditText.doAfterTextChanged {
            filters.messageContains = binding.messageContainsEditText.nonBlankTextOrNull
        }

        updateNewerThanInput()
        binding.newerThanEditText.setOnClickListener {
            lifecycleScope.launch {
                filters.timestampNewerThan = pickDateTime(
                    filters.timestampNewerThan ?: LocalDateTime.now()
                )
                updateNewerThanInput()
            }
        }
        binding.newerThanEditTextLayout.setEndIconOnClickListener {
            filters.timestampNewerThan = null
            updateNewerThanInput()
        }

        updateOlderThanInput()
        binding.olderThanEditText.setOnClickListener {
            lifecycleScope.launch {
                filters.timestampOlderThan = pickDateTime(
                    filters.timestampOlderThan ?: LocalDateTime.now()
                )
                updateOlderThanInput()
            }
        }
        binding.olderThanEditTextLayout.setEndIconOnClickListener {
            filters.timestampOlderThan = null
            updateOlderThanInput()
        }
    }

    private fun createLogLevelsChips() {
        LogLevel.values().forEach { level ->
            val chip = createLogLevelChip(context, level)

            chip.isChecked = filters.levels.contains(level)
            chip.isChipIconVisible = !chip.isChecked

            chip.setOnClickListener {
                chip.isChipIconVisible = !chip.isChecked

                when (filters.levels.contains(level)) {
                    true -> filters.levels.remove(level)
                    false -> filters.levels.add(level)
                }
            }

            binding.levelChipGroup.addView(chip)
        }
    }

    private fun updateNewerThanInput() {
        binding.newerThanEditTextLayout.isEndIconVisible = (filters.timestampNewerThan != null)
        binding.newerThanEditText.setText(filters.timestampNewerThan?.let {  dateTimeToString(locale, it) } ?: "")
    }

    private fun updateOlderThanInput() {
        binding.olderThanEditTextLayout.isEndIconVisible = (filters.timestampOlderThan != null)
        binding.olderThanEditText.setText(filters.timestampOlderThan?.let { dateTimeToString(locale, it) } ?: "")
    }

    private suspend fun pickDateTime(initialDateTime: LocalDateTime): LocalDateTime {
        val date = pickDate(initialDateTime.date)
        val time = pickTime(initialDateTime.time)

        return LocalDateTime(date, time)
    }

    private suspend fun pickDate(initialDate: LocalDate): LocalDate =
        suspendCancellableCoroutine { cont ->
            DatePickerDialog(
                context,
                R.style.Theme_Bubble_Dialog_DatePicker,
                { _, year, month, dayOfMonth ->
                    cont.resume(LocalDate(year, month, dayOfMonth))
                },
                initialDate.year,
                initialDate.monthNumber,
                initialDate.dayOfMonth
            ).show()
        }

    private suspend fun pickTime(initialTime: LocalTime): LocalTime =
        suspendCancellableCoroutine { cont ->
            TimePickerDialog(
                context,
                initialTime.hour,
                initialTime.minute,
                true
            ) { hour, minute ->
                cont.resume(LocalTime(hour, minute))
            }.show()
        }
}

private fun createLogLevelChip(context: Context, level: LogLevel) = Chip(context).apply {
    val drawable = ChipDrawable.createFromAttributes(
        context,
        null,
        0,
        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter
    )

    setChipDrawable(drawable)

    checkedIcon = context.getDrawable(R.drawable.ic_check_circle_24dp)?.mutate()
    checkedIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, level.colorId))

    chipIcon = context.getDrawable(R.drawable.ic_circle_outline_24dp)?.mutate()
    chipIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, level.colorId))

    text = level.name
    TextViewCompat.setTextAppearance(this, level.styleResId)
}

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
    onApplyButtonClick: (filters: LogsFilters?) -> Unit
) : AlertDialog(context) {

    private var levels: MutableSet<LogLevel> = initialFilters.levels.toMutableSet()
    private var messageContains: String? = initialFilters.messageContains
    private var timestampNewerThan: LocalDateTime? = initialFilters.timestampNewerThan
    private var timestampOlderThan: LocalDateTime? = initialFilters.timestampOlderThan

    private val binding = DialogLogsFiltersBinding.inflate(LayoutInflater.from(context))
    private val locale = Locale.getDefault()

    init {
        setView(binding.root)

        setButton(BUTTON_POSITIVE, context.getString(R.string.action_filter)) { _, _ ->
            onApplyButtonClick(LogsFilters(
                levels, messageContains, timestampNewerThan, timestampOlderThan
            ))
            dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.action_reset)) { _, _ ->
            onApplyButtonClick(null)
            cancel()
        }

        createLogLevelsChips()

        binding.messageContainsEditText.setText(messageContains)
        binding.messageContainsEditText.doAfterTextChanged {
            messageContains = binding.messageContainsEditText.nonBlankTextOrNull
        }

        updateNewerThanInput()
        binding.newerThanEditText.setOnClickListener {
            lifecycleScope.launch {
                timestampNewerThan = pickDateTime(
                    timestampNewerThan ?: LocalDateTime.now()
                )
                updateNewerThanInput()
            }
        }
        binding.newerThanEditTextLayout.setEndIconOnClickListener {
            timestampNewerThan = null
            updateNewerThanInput()
        }

        updateOlderThanInput()
        binding.olderThanEditText.setOnClickListener {
            lifecycleScope.launch {
                timestampOlderThan = pickDateTime(
                    timestampOlderThan ?: LocalDateTime.now()
                )
                updateOlderThanInput()
            }
        }
        binding.olderThanEditTextLayout.setEndIconOnClickListener {
            timestampOlderThan = null
            updateOlderThanInput()
        }
    }

    private fun createLogLevelsChips() {
        LogLevel.entries.forEach { level ->
            val chip = createLogLevelChip(context, level)

            chip.isChecked = levels.contains(level)
            chip.isChipIconVisible = !chip.isChecked

            chip.setOnClickListener {
                chip.isChipIconVisible = !chip.isChecked

                when (levels.contains(level)) {
                    true -> levels.remove(level)
                    false -> levels.add(level)
                }
            }

            binding.levelChipGroup.addView(chip)
        }
    }

    private fun updateNewerThanInput() {
        binding.newerThanEditTextLayout.isEndIconVisible = timestampNewerThan != null
        binding.newerThanEditText.setText(timestampNewerThan?.let { dateTimeToString(locale, it) } ?: "")
    }

    private fun updateOlderThanInput() {
        binding.olderThanEditTextLayout.isEndIconVisible = timestampOlderThan != null
        binding.olderThanEditText.setText(timestampOlderThan?.let { dateTimeToString(locale, it) } ?: "")
    }

    private suspend fun pickDateTime(initialDateTime: LocalDateTime): LocalDateTime {
        val date = pickDate(initialDateTime.date)
        val time = pickTime(initialDateTime.time)

        return LocalDateTime(date, time)
    }

    private suspend fun pickDate(initialDate: LocalDate): LocalDate =
        // LocalDate works with with month *number* (1-12), while Android date picker dialog works
        // with month *index*, hence the +1 / -1
        suspendCancellableCoroutine { cont ->
            DatePickerDialog(
                context,
                R.style.Theme_Bubble_Dialog_DatePicker,
                { _, year, monthIndex, dayOfMonth ->
                    cont.resume(LocalDate(year, monthIndex + 1, dayOfMonth))
                },
                initialDate.year,
                initialDate.monthNumber - 1,
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

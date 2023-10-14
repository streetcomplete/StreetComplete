package de.westnordost.streetcomplete.screens.about

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogLevel
import de.westnordost.streetcomplete.data.logs.styleResId
import de.westnordost.streetcomplete.databinding.DialogLogsFiltersBinding
import de.westnordost.streetcomplete.databinding.RowLogsFiltersCheckboxBinding
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.view.ListAdapter
import de.westnordost.streetcomplete.view.dialogs.TimePickerDialog
import kotlinx.datetime.LocalDateTime

class LogsFiltersDialog(
    context: Context,
    initialFilters: LogsFilters,
    onApplyButtonClick: (filters: LogsFilters) -> Unit
) : AlertDialog(context) {

    private val filters = initialFilters.copy()
    private val binding = DialogLogsFiltersBinding.inflate(LayoutInflater.from(context))

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

        val allLevels = LogLevel.values().toList()
        binding.levelList.adapter = LevelsFilterAdapter(
            allLevels,
            filters.levels
        ) {
            when (filters.levels.contains(it)) {
                true -> filters.levels.remove(it)
                false -> filters.levels.add(it)
            }
        }

        binding.messageContainsEditText.setText(filters.messageContains)
        updateMessageContainsClearButton()
        binding.messageContainsEditText.doAfterTextChanged {
            filters.messageContains = binding.messageContainsEditText.nonBlankTextOrNull
            updateMessageContainsClearButton()
        }
        binding.messageContainsClearButton.setOnClickListener {
            filters.messageContains = null
            binding.messageContainsEditText.setText(null)
            updateMessageContainsClearButton()
        }

        updateNewerThanInput()
        binding.newerThanTextDate.setOnClickListener {
            pickLocalDateTime(filters.timestampNewerThan ?: LocalDateTime.now()) {
                filters.timestampNewerThan = it
                updateNewerThanInput()
            }
        }
        binding.newerThanClearButton.setOnClickListener {
            filters.timestampNewerThan = null
            updateNewerThanInput()
        }

        updateOlderThanInput()
        binding.olderThanTextDate.setOnClickListener {
            pickLocalDateTime(filters.timestampOlderThan ?: LocalDateTime.now()) {
                filters.timestampOlderThan = it
                updateOlderThanInput()
            }
        }
        binding.olderThanClearButton.setOnClickListener {
            filters.timestampOlderThan = null
            updateOlderThanInput()
        }
    }
    private fun updateMessageContainsClearButton() {
        binding.messageContainsClearButton.visibility = if (filters.messageContains == null) View.GONE else View.VISIBLE
    }

    private fun updateNewerThanInput() {
        binding.newerThanClearButton.visibility = if (filters.timestampNewerThan == null) View.GONE else View.VISIBLE
        binding.newerThanTextDate.setText(filters.timestampNewerThan?.toString() ?: "")
    }

    private fun updateOlderThanInput() {
        binding.olderThanClearButton.visibility = if (filters.timestampOlderThan == null) View.GONE else View.VISIBLE
        binding.olderThanTextDate.setText(filters.timestampOlderThan?.toString() ?: "")
    }

    private fun pickLocalDateTime(
        initialDateTime: LocalDateTime,
        callback: (dateTime: LocalDateTime) -> Unit
    ) {
        DatePickerDialog(
            context,
            R.style.Theme_Bubble_Dialog_DatePicker,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    initialDateTime.hour,
                    initialDateTime.minute,
                    true
                ) { hour, minute ->
                    val dateTime = LocalDateTime(year, month, dayOfMonth, hour, minute)
                    callback(dateTime)
                }.show()
            },
            initialDateTime.year,
            initialDateTime.monthNumber,
            initialDateTime.dayOfMonth
        ).show()
    }
}

class LevelsFilterAdapter(
    levels: List<LogLevel>,
    private val selectedLevels: Set<LogLevel>,
    private val onLevelToggle: (LogLevel) -> Unit
) : ListAdapter<LogLevel>(levels) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            RowLogsFiltersCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    inner class ViewHolder(val binding: RowLogsFiltersCheckboxBinding) : ListAdapter.ViewHolder<LogLevel>(binding) {
        override fun onBind(with: LogLevel) {
            binding.checkBox.text = with.name
            TextViewCompat.setTextAppearance(binding.checkBox, with.styleResId)
            binding.checkBox.isChecked = selectedLevels.contains(with)
            binding.checkBox.setOnCheckedChangeListener { _, _ ->
                onLevelToggle(with)
            }
        }
    }
}

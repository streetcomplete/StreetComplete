package de.westnordost.streetcomplete.quests.opening_hours.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestTimesMonthRowBinding
import de.westnordost.streetcomplete.databinding.QuestTimesOffdayRowBinding
import de.westnordost.streetcomplete.databinding.QuestTimesWeekdayRowBinding
import de.westnordost.streetcomplete.osm.opening_hours.model.Months
import de.westnordost.streetcomplete.osm.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHours
import de.westnordost.streetcomplete.quests.opening_hours.MonthsPickerDialog
import de.westnordost.streetcomplete.quests.opening_hours.TimeRangePickerDialog
import de.westnordost.streetcomplete.quests.opening_hours.WeekdaysPickerDialog
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
sealed class OpeningHoursRow
@Serializable
data class OpeningMonthsRow(var months: Months) : OpeningHoursRow()
@Serializable
data class OpeningWeekdaysRow(var weekdays: Weekdays, var timeRange: TimeRange) : OpeningHoursRow()
@Serializable
data class OffDaysRow(var weekdays: Weekdays) : OpeningHoursRow()

class OpeningHoursAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var rows: MutableList<OpeningHoursRow> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isEnabled = true
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    /** Set to change which weekdays are pre-checked in the weekday-select dialog */
    var firstDayOfWorkweek: String = "Mo"
    /** Set to change which weekdays are pre-checked in the weekday-select dialog */
    var regularShoppingDays: Int = 6
    /** Locale of the weekday names etc. */
    var locale: Locale = Locale.getDefault()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MONTHS   -> MonthsViewHolder(QuestTimesMonthRowBinding.inflate(inflater, parent, false))
            WEEKDAYS -> WeekdayViewHolder(QuestTimesWeekdayRowBinding.inflate(inflater, parent, false))
            OFFDAYS  -> OffDaysViewHolder(QuestTimesOffdayRowBinding.inflate(inflater, parent, false))
            else     -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]

        when (holder) {
            is MonthsViewHolder -> {
                holder.update(row as OpeningMonthsRow, isEnabled)
            }
            is WeekdayViewHolder -> {
                val prevRow = if (position > 0) rows[position - 1] as? OpeningWeekdaysRow else null
                holder.update(row as OpeningWeekdaysRow, prevRow, isEnabled)
            }
            is OffDaysViewHolder -> {
                holder.update(row as OffDaysRow, isEnabled)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (rows[position]) {
        is OpeningMonthsRow -> MONTHS
        is OpeningWeekdaysRow -> WEEKDAYS
        is OffDaysRow -> OFFDAYS
    }

    override fun getItemCount() = rows.size

    /* ------------------------------------------------------------------------------------------ */

    private fun remove(position: Int) {
        if (!isEnabled) return

        val row = rows[position]
        require(row !is OpeningMonthsRow) { "May only directly remove weekdays, not months" }

        rows.removeAt(position)
        notifyItemRemoved(position)

        val rowHere = if (position < rows.size) rows[position] else null
        val rowAbove = if (position > 0) rows[position - 1] else null

        // this weekday row must be updated because it might be the first one with the same weekdays
        // and thus it is the one that should show the weekdays now
        if (rowHere is OpeningWeekdaysRow) {
            notifyItemChanged(position)
        }
        // if no weekdays left for months: remove months
        if (rowAbove is OpeningMonthsRow && rowHere == null || rowHere is OpeningMonthsRow) {
            rows.removeAt(position - 1)
            notifyItemRemoved(position - 1)
        }
    }

    fun addNewMonths() {
        openSetMonthsRangeDialog(getMonthsSuggestion()) { months ->
            openSetWeekdaysDialog(getWeekdaysSuggestion(true)) { weekdays ->
                openSetTimeRangeDialog(getOpeningHoursSuggestion()) { timeRange ->
                    addMonths(months, weekdays, timeRange)
                }
            }
        }
    }

    private fun addNewMonthsAsFirstRow() {
        openSetMonthsRangeDialog(getMonthsSuggestion()) { months ->
            rows.add(0, OpeningMonthsRow(months))
            notifyItemInserted(0)
        }
    }

    private fun addMonths(months: Months, weekdays: Weekdays, timeRange: TimeRange) {
        val insertIndex = itemCount
        rows.add(OpeningMonthsRow(months))
        rows.add(OpeningWeekdaysRow(weekdays, timeRange))
        notifyItemRangeInserted(insertIndex, 2)
    }

    fun addNewWeekdays() {
        val rowAbove = if (rows.size > 0) rows[rows.size - 1] else null
        val isFirst = rowAbove == null || rowAbove is OpeningMonthsRow
        openSetWeekdaysDialog(getWeekdaysSuggestion(isFirst)) { weekdays ->
            openSetTimeRangeDialog(getOpeningHoursSuggestion()) { timeRange ->
                addWeekdays(weekdays, timeRange)
            }
        }
    }

    fun addNewHours() {
        val rowAbove = if (rows.size > 0) rows[rows.size - 1] else null
        if (rowAbove !is OpeningWeekdaysRow) return
        openSetTimeRangeDialog(getOpeningHoursSuggestion()) { timeRange ->
            addWeekdays(rowAbove.weekdays, timeRange)
        }
    }

    private fun addWeekdays(weekdays: Weekdays, timeRange: TimeRange) {
        val insertIndex = itemCount
        rows.add(OpeningWeekdaysRow(weekdays, timeRange))
        notifyItemInserted(insertIndex)
    }

    fun addNewOffDays() {
        openSetWeekdaysDialog(null) { weekdays -> addOffDays(weekdays) }
    }

    private fun addOffDays(weekdays: Weekdays) {
        if (weekdays.isSelectionEmpty()) return

        val insertIndex = itemCount
        rows.add(OffDaysRow(weekdays))
        notifyItemInserted(insertIndex)
    }

    fun createOpeningHours() = rows.toOpeningHours()

    fun changeToMonthsMode() {
        if (rows.isEmpty()) {
            addNewMonths()
        } else if (rows.first() !is OpeningMonthsRow) {
            addNewMonthsAsFirstRow()
        }
    }

    private fun getOpeningHoursSuggestion() = TYPICAL_OPENING_TIMES

    /* -------------------------------------- months select --------------------------------------*/

    private inner class MonthsViewHolder(
        private val binding: QuestTimesMonthRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun update(row: OpeningMonthsRow, isEnabled: Boolean) {
            binding.monthsLabel.text =
                if (row.months.isSelectionEmpty()) {
                    "(" + context.resources.getString(R.string.quest_openingHours_unspecified_range) + ")"
                } else {
                    row.months.toLocalizedString(locale)
                }
            binding.monthsLabel.setOnClickListener {
                openSetMonthsRangeDialog(row.months) { months ->
                    row.months = months
                    notifyItemChanged(adapterPosition)
                }
            }
            binding.monthsLabel.isClickable = isEnabled
        }
    }

    private fun getMonthsSuggestion(): Months {
        val mentionedMonths = BooleanArray(Months.MONTHS_COUNT)
        for (row in rows) {
            if (row is OpeningMonthsRow) {
                row.months.selection.forEachIndexed { index, b ->
                    if (b) mentionedMonths[index] = true
                }
            }
        }
        val unmentionedMonths = BooleanArray(Months.MONTHS_COUNT)
        mentionedMonths.forEachIndexed { index, b ->
            unmentionedMonths[index] = !b
        }
        return Months(unmentionedMonths)
    }

    private fun openSetMonthsRangeDialog(months: Months, callback: (Months) -> Unit) {
        MonthsPickerDialog.show(context, months, locale, callback)
    }

    /* ------------------------------------ weekdays select --------------------------------------*/

    private inner class WeekdayViewHolder(
        private val binding: QuestTimesWeekdayRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteButton.setOnClickListener {
                val index = adapterPosition
                if (index != RecyclerView.NO_POSITION) remove(index)
            }
        }

        fun update(row: OpeningWeekdaysRow, rowBefore: OpeningWeekdaysRow?, isEnabled: Boolean) {
            binding.weekdaysLabel.text =
                if (rowBefore != null && row.weekdays == rowBefore.weekdays) {
                    ""
                } else if (rowBefore != null && row.weekdays.isSelectionEmpty()) {
                    "(" + context.resources.getString(R.string.quest_openingHours_unspecified_range) + ")"
                } else {
                    row.weekdays.toLocalizedString(context.resources, locale)
                }

            binding.weekdaysLabel.setOnClickListener {
                openSetWeekdaysDialog(row.weekdays) { weekdays ->
                    // rows that had the same weekdays as this one need to be updated
                    val rowsThatNeedUpdate = rows
                        .subList(adapterPosition, rows.size)
                        .takeWhile { (it as? OpeningWeekdaysRow)?.weekdays == row.weekdays }
                        .size
                    row.weekdays = weekdays
                    notifyItemRangeChanged(adapterPosition, rowsThatNeedUpdate)
                }
            }

            binding.hoursLabel.text = row.timeRange.toStringUsing(Locale.getDefault(), "–")
            binding.hoursLabel.setOnClickListener {
                openSetTimeRangeDialog(row.timeRange) { timeRange ->
                    row.timeRange = timeRange
                    notifyItemChanged(adapterPosition)
                }
            }

            binding.deleteButton.isInvisible = !isEnabled
            binding.deleteButton.isClickable = isEnabled
            binding.weekdaysLabel.isClickable = isEnabled
            binding.hoursLabel.isClickable = isEnabled
        }
    }

    /* ------------------------------------ offdays select --------------------------------------*/

    private inner class OffDaysViewHolder(
        private val binding: QuestTimesOffdayRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteButton.setOnClickListener {
                val index = adapterPosition
                if (index != RecyclerView.NO_POSITION) remove(index)
            }
        }

        fun update(row: OffDaysRow, isEnabled: Boolean) {
            binding.weekdaysLabel.text = row.weekdays.toLocalizedString(context.resources, locale)
            binding.weekdaysLabel.setOnClickListener {
                openSetWeekdaysDialog(row.weekdays) { weekdays ->
                    if (!weekdays.isSelectionEmpty()) {
                        row.weekdays = weekdays
                        notifyItemChanged(adapterPosition)
                    }
                }
            }

            binding.deleteButton.isInvisible = !isEnabled
            binding.deleteButton.isClickable = isEnabled
            binding.weekdaysLabel.isClickable = isEnabled
        }
    }

    private fun getWeekdaysSuggestion(isFirst: Boolean): Weekdays {
        if (isFirst) {
            val firstWorkDayIdx = Weekdays.getWeekdayIndex(firstDayOfWorkweek)
            val result = BooleanArray(Weekdays.OSM_ABBR_WEEKDAYS.size)
            for (i in 0 until regularShoppingDays) {
                result[(i + firstWorkDayIdx) % Weekdays.WEEKDAY_COUNT] = true
            }
            return Weekdays(result)
        }
        return Weekdays()
    }

    private fun openSetWeekdaysDialog(weekdays: Weekdays?, callback: (Weekdays) -> Unit) {
        WeekdaysPickerDialog.show(context, weekdays, locale, callback)
    }

    private fun openSetTimeRangeDialog(timeRange: TimeRange, callback: (TimeRange) -> Unit) {
        val startLabel = context.resources.getString(R.string.quest_openingHours_start_time)
        val endLabel = context.resources.getString(R.string.quest_openingHours_end_time)

        TimeRangePickerDialog(context, startLabel, endLabel, timeRange, DateFormat.is24HourFormat(context), callback).show()
    }

    companion object {
        private const val MONTHS = 0
        private const val WEEKDAYS = 1
        private const val OFFDAYS = 2

        /* ------------------------------------- times select ----------------------------------------*/

        // this could go into per-country localization when this page (or any other source)
        // https://en.wikipedia.org/wiki/Shopping_hours contains more information about typical
        // opening hours per country
        private val TYPICAL_OPENING_TIMES = TimeRange(8 * 60, 18 * 60, false)
    }
}

package de.westnordost.streetcomplete.quests.opening_hours.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams

import java.text.DateFormatSymbols
import java.util.Locale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.quests.opening_hours.TimeRangePickerDialog
import de.westnordost.streetcomplete.quests.opening_hours.WeekdaysPickerDialog
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.view.dialogs.RangePickedCallback
import de.westnordost.streetcomplete.view.dialogs.RangePickerDialog

sealed class OpeningHoursRow
data class OpeningMonthsRow(var months: CircularSection): OpeningHoursRow()
data class OpeningWeekdaysRow(var weekdays: Weekdays, var timeRange: TimeRange) : OpeningHoursRow()
data class OffDaysRow(var weekdays: Weekdays): OpeningHoursRow()

class AddOpeningHoursAdapter(
    private val context: Context,
    private val countryInfo: CountryInfo
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var rows: MutableList<OpeningHoursRow> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isEnabled = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MONTHS   -> MonthsViewHolder(inflater.inflate(R.layout.quest_times_month_row, parent, false))
            WEEKDAYS -> WeekdayViewHolder(inflater.inflate(R.layout.quest_times_weekday_row, parent, false))
            else     -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]

        if (holder is MonthsViewHolder) {
            holder.update(row as OpeningMonthsRow, isEnabled)
        } else if (holder is WeekdayViewHolder) {
            val prevRow = if (position > 0) rows[position -1] as? OpeningWeekdaysRow else null
            holder.update(row as OpeningWeekdaysRow, prevRow, isEnabled)
        }
    }

    override fun getItemViewType(position: Int) = when(rows[position]) {
        is OpeningMonthsRow -> MONTHS
        is OpeningWeekdaysRow -> WEEKDAYS
    }

    override fun getItemCount() = rows.size

    /* ------------------------------------------------------------------------------------------ */

    private fun remove(position: Int) {
        if (!isEnabled) return

        val row = rows[position]
        require (row !is OpeningWeekdaysRow) { "May only directly remove weekdays, not months" }

        rows.removeAt(position)
        notifyItemRemoved(position)

        val rowHere = if (position < rows.size) rows[position] else null
        val rowAbove =  if (position > 0) rows[position - 1] else null

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
        openSetMonthsRangeDialog(getMonthsRangeSuggestion()) { startIndex, endIndex ->
            val months = CircularSection(startIndex, endIndex)
            openSetWeekdaysDialog(getWeekdaysSuggestion(true)) { weekdays ->
                openSetTimeRangeDialog(getOpeningHoursSuggestion()) { timeRange ->
                    addMonths(months, weekdays, timeRange)
                }
            }
        }
    }

    fun addNewMonthsAsFirstRow() {
        openSetMonthsRangeDialog(getMonthsRangeSuggestion()) { startIndex, endIndex ->
            val months = CircularSection(startIndex, endIndex)
            rows.add(0, OpeningMonthsRow(months))
            notifyItemInserted(0)
        }
    }

    private fun addMonths(months: CircularSection, weekdays: Weekdays, timeRange: TimeRange) {
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
                addWeekdays(weekdays, timeRange) }
        }
    }

    private fun addWeekdays(weekdays: Weekdays, timeRange: TimeRange) {
        val insertIndex = itemCount
        rows.add(OpeningWeekdaysRow(weekdays, timeRange))
        notifyItemInserted(insertIndex)
    }

    fun createOpeningHours() = rows.toOpeningHoursRules()

    fun changeToMonthsMode() {
        if (rows.isEmpty()) {
            addNewMonths()
        } else {
            addNewMonthsAsFirstRow()
        }
    }

    private fun getOpeningHoursSuggestion() = TYPICAL_OPENING_TIMES

    /* -------------------------------------- months select --------------------------------------*/

    private inner class MonthsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthsLabel: TextView = itemView.findViewById(R.id.monthsLabel)
        private val deleteButton: View = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.isGone = true
        }

        private fun setVisibility(isVisible: Boolean) {
            itemView.isGone = !isVisible
            itemView.updateLayoutParams {
                height = if(isVisible) LinearLayout.LayoutParams.WRAP_CONTENT else 0
                width = if(isVisible) LinearLayout.LayoutParams.MATCH_PARENT else 0
            }
        }

        fun update(row: OpeningMonthsRow, isEnabled: Boolean) {
            val months = row.months
            setVisibility(months != null)
            if (months != null) {
                monthsLabel.text = months.toStringUsing(DateFormatSymbols.getInstance().months, "–")
                monthsLabel.setOnClickListener {
                    openSetMonthsRangeDialog(months) { startIndex, endIndex ->
                        row.months = CircularSection(startIndex, endIndex)
                        notifyItemChanged(adapterPosition)
                    }
                }
                monthsLabel.isEnabled = isEnabled
            }
        }
    }

    private fun getMonthsRangeSuggestion(): CircularSection {
        val months = getUnmentionedMonths()
        return if (months.isEmpty()) {
            CircularSection(0, 11)
        } else months[0]
    }

    private fun getUnmentionedMonths(): List<CircularSection> {
        val allTheMonths = rows.mapNotNull { (it as? OpeningMonthsRow)?.months }
        return NumberSystem(0, 11).complemented(allTheMonths)
    }

    private fun openSetMonthsRangeDialog(months: CircularSection, callback: RangePickedCallback) {
        val monthNames = DateFormatSymbols.getInstance().months
        val title = context.resources.getString(R.string.quest_openingHours_chooseMonthsTitle)
        RangePickerDialog(context, monthNames, months.start, months.end, title, callback).show()
    }

    /* ------------------------------------ weekdays select --------------------------------------*/

    private inner class WeekdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weekdaysLabel: TextView = itemView.findViewById(R.id.weekdaysLabel)
        private val hoursLabel: TextView = itemView.findViewById(R.id.hoursLabel)
        private val deleteButton: View = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.setOnClickListener {
                val index = adapterPosition
                if (index != RecyclerView.NO_POSITION) remove(index)
            }
        }

        fun update(row: OpeningWeekdaysRow, rowBefore: OpeningWeekdaysRow?, isEnabled: Boolean) {
            weekdaysLabel.text =
                if (rowBefore != null && row.weekdays == rowBefore.weekdays) ""
                else row.weekdays?.toLocalizedString(context.resources) ?: ""
            weekdaysLabel.setOnClickListener {
                openSetWeekdaysDialog(row.weekdays) { weekdays ->
                    row.weekdays = weekdays
                    notifyItemChanged(adapterPosition)
                }
            }

            hoursLabel.text = row.timeRange.toStringUsing(Locale.getDefault(), "–")
            hoursLabel.setOnClickListener {
                openSetTimeRangeDialog(row.timeRange) { timeRange ->
                    row.timeRange = timeRange
                    notifyItemChanged(adapterPosition)
                }
            }

            deleteButton.isGone = !isEnabled
            deleteButton.isEnabled = isEnabled
            weekdaysLabel.isEnabled = isEnabled
            hoursLabel.isEnabled = isEnabled
        }
    }

    private fun getWeekdaysSuggestion(isFirst: Boolean): Weekdays {
        if (isFirst) {
            val firstWorkDayIdx = Weekdays.getWeekdayIndex(countryInfo.firstDayOfWorkweek)
            val result = BooleanArray(7)
            for (i in 0 until countryInfo.regularShoppingDays) {
                result[(i + firstWorkDayIdx) % 7] = true
            }
            return Weekdays(result)
        }
        return Weekdays()
    }

    private fun openSetWeekdaysDialog(weekdays: Weekdays?, callback: (Weekdays) -> Unit) {
        WeekdaysPickerDialog.show(context, weekdays, callback)
    }

    private fun openSetTimeRangeDialog(timeRange: TimeRange, callback: (TimeRange) -> Unit) {
        val startLabel = context.resources.getString(R.string.quest_openingHours_start_time)
        val endLabel = context.resources.getString(R.string.quest_openingHours_end_time)

        TimeRangePickerDialog(context, startLabel, endLabel, timeRange, callback).show()
    }

    companion object {
        private const val MONTHS = 0
        private const val WEEKDAYS = 1

        /* ------------------------------------- times select ----------------------------------------*/

        // this could go into per-country localization when this page (or any other source)
        // https://en.wikipedia.org/wiki/Shopping_hours contains more information about typical
        // opening hours per country
        private val TYPICAL_OPENING_TIMES = TimeRange(8 * 60, 18 * 60, false)
    }
}

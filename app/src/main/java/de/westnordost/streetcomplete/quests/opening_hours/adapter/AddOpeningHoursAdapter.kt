package de.westnordost.streetcomplete.quests.opening_hours.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams

import java.text.DateFormatSymbols
import java.util.Locale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection
import de.westnordost.streetcomplete.quests.opening_hours.model.NumberSystem
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningMonths
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.quests.opening_hours.TimeRangePickerDialog
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays
import de.westnordost.streetcomplete.quests.opening_hours.WeekdaysPickerDialog
import de.westnordost.streetcomplete.view.dialogs.RangePickedCallback
import de.westnordost.streetcomplete.view.dialogs.RangePickerDialog

data class OpeningMonthsRow(var months: CircularSection = CircularSection(0, MAX_MONTH_INDEX)) {

    var weekdaysList: MutableList<OpeningWeekdaysRow> = mutableListOf()

    constructor(months: CircularSection, initialWeekdays: OpeningWeekdaysRow) : this(months) {
        weekdaysList.add(initialWeekdays)
    }

    companion object {
        private val MAX_MONTH_INDEX = 11
    }
}

data class OpeningWeekdaysRow(var weekdays: Weekdays, var timeRange: TimeRange)

class AddOpeningHoursAdapter(
    initialMonthsRows: List<OpeningMonthsRow>,
    private val context: Context,
    private val countryInfo: CountryInfo
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var monthsRows: MutableList<OpeningMonthsRow> = initialMonthsRows.toMutableList()
        private set

    var isDisplayMonths = false
        set(displayMonths) {
            field = displayMonths
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
        val p = getHierarchicPosition(position)
        val om = monthsRows[p[0]]

        if (holder is MonthsViewHolder) {
            holder.update(om)
        } else if (holder is WeekdayViewHolder) {
            val ow = om.weekdaysList[p[1]]
            val prevOw = if (p[1] > 0) om.weekdaysList[p[1] - 1] else null
            holder.update(ow, prevOw)
        }
    }

    override fun getItemViewType(position: Int) =
        if (getHierarchicPosition(position).size == 1) MONTHS else WEEKDAYS

    private fun getHierarchicPosition(position: Int): IntArray {
        var count = 0
        for (i in monthsRows.indices) {
            val om = monthsRows[i]
            if (count == position) return intArrayOf(i)
            ++count

            for (j in 0 until om.weekdaysList.size) {
                if (count == position) return intArrayOf(i, j)
                ++count
            }
        }
        throw IllegalArgumentException()
    }

    override fun getItemCount() = monthsRows.sumBy { it.weekdaysList.size } + monthsRows.size

    /* ------------------------------------------------------------------------------------------ */

    private fun remove(position: Int) {
        val p = getHierarchicPosition(position)
        if (p.size != 2) throw IllegalArgumentException("May only directly remove weekdays, not months")

        val weekdays = monthsRows[p[0]].weekdaysList
        weekdays.removeAt(p[1])
        notifyItemRemoved(position)
        // if not last weekday removed -> element after this one may need to be updated
        // because it may need to show the weekdays now
        if (p[1] < weekdays.size) notifyItemChanged(position)
        // if no weekdays left in months: remove/reset months
        if (weekdays.isEmpty()) {
            if (monthsRows.size == 1) {
                monthsRows[0] = OpeningMonthsRow()
                isDisplayMonths = false
                notifyItemChanged(0)
            } else {
                monthsRows.removeAt(p[0])
                notifyItemRemoved(position - 1)
            }
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

    private fun addMonths(months: CircularSection, weekdays: Weekdays, timeRange: TimeRange) {
        val insertIndex = itemCount
        monthsRows.add(OpeningMonthsRow(months, OpeningWeekdaysRow(weekdays, timeRange)))
        notifyItemRangeInserted(insertIndex, 2) // 2 = opening month + opening weekday
    }

    fun addNewWeekdays() {
        val isFirst = monthsRows[monthsRows.size - 1].weekdaysList.isEmpty()
        openSetWeekdaysDialog(getWeekdaysSuggestion(isFirst)) { weekdays ->
            openSetTimeRangeDialog(getOpeningHoursSuggestion()) { timeRange ->
                addWeekdays(weekdays, timeRange) }
        }
    }

    private fun addWeekdays(weekdays: Weekdays, timeRange: TimeRange) {
        val insertIndex = itemCount
        monthsRows[monthsRows.size - 1].weekdaysList.add(OpeningWeekdaysRow(weekdays, timeRange))
        notifyItemInserted(insertIndex)
    }

    fun createOpeningMonths() = monthsRows.toOpeningMonthsList()

    fun changeToMonthsMode() {
        val om = monthsRows[0]
        openSetMonthsRangeDialog(om.months) { startIndex, endIndex ->
            if (om.weekdaysList.isEmpty()) {
                openSetWeekdaysDialog(getWeekdaysSuggestion(true)) { weekdays ->
                    openSetTimeRangeDialog(getOpeningHoursSuggestion()) { timeRange ->
                        changedToMonthsMode(startIndex, endIndex)
                        om.weekdaysList.add(OpeningWeekdaysRow(weekdays, timeRange))
                        notifyItemInserted(1)
                    }
                }
            } else {
                changedToMonthsMode(startIndex, endIndex)
            }
        }
    }

    private fun changedToMonthsMode(startIndex: Int, endIndex: Int) {
        isDisplayMonths = true
        monthsRows[0].months = CircularSection(startIndex, endIndex)
        notifyItemChanged(0)
    }

    private fun getOpeningHoursSuggestion() = TYPICAL_OPENING_TIMES

    /* -------------------------------------- months select --------------------------------------*/

    private inner class MonthsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthsLabel: TextView = itemView.findViewById(R.id.monthsLabel)
        private val deleteButton: View = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.visibility = View.GONE
        }

        private fun setVisibility(isVisible: Boolean) {
            itemView.visibility = if (isVisible) View.VISIBLE else View.GONE
            itemView.updateLayoutParams {
                height = if(isVisible) LinearLayout.LayoutParams.WRAP_CONTENT else 0
                width = if(isVisible) LinearLayout.LayoutParams.MATCH_PARENT else 0
            }
        }

        fun update(row: OpeningMonthsRow) {
            setVisibility(isDisplayMonths)
            monthsLabel.text = row.months.toStringUsing(DateFormatSymbols.getInstance().months, "–")
            monthsLabel.setOnClickListener {
                openSetMonthsRangeDialog(row.months) { startIndex, endIndex ->
                    row.months = CircularSection(startIndex, endIndex)
                    notifyItemChanged(adapterPosition)
                }
            }
        }
    }

    private fun getMonthsRangeSuggestion(): CircularSection {
        val months = getUnmentionedMonths()
        return if (months.isEmpty()) {
            CircularSection(0, OpeningMonths.MAX_MONTH_INDEX)
        } else months[0]
    }

    private fun getUnmentionedMonths(): List<CircularSection> {
        val allTheMonths = monthsRows.map { it.months }
        return NumberSystem(0, OpeningMonths.MAX_MONTH_INDEX).complemented(allTheMonths)
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
                if (index != RecyclerView.NO_POSITION) remove(adapterPosition)
            }
        }

        fun update(row: OpeningWeekdaysRow, rowBefore: OpeningWeekdaysRow?) {
            if (rowBefore != null && row.weekdays == rowBefore.weekdays) {
                weekdaysLabel.text = ""
            } else {
                weekdaysLabel.text = row.weekdays.toLocalizedString(context.resources)
            }

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

    private fun openSetWeekdaysDialog(weekdays: Weekdays, callback: (Weekdays) -> Unit) {
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

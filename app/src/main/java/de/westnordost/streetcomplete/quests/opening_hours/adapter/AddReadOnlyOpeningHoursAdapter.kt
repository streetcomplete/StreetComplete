package de.westnordost.streetcomplete.quests.opening_hours.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays
import java.text.DateFormatSymbols
import java.util.*

class AddReadOnlyOpeningHoursAdapter(
    initialMonthsRows: List<OpeningMonthsRow>,
    private val context: Context,
    private val countryInfo: CountryInfo
) : AddOpeningHoursAdapter(initialMonthsRows, context, countryInfo) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MONTHS   -> MonthsViewHolder(inflater.inflate(R.layout.quest_times_month_row_read_only, parent, false))
            WEEKDAYS -> WeekdayViewHolder(inflater.inflate(R.layout.quest_times_weekday_row_read_only, parent, false))
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

    override fun getItemCount() = monthsRows.sumBy { it.weekdaysList.size } + monthsRows.size

    override fun addNewMonths() {
        throw IllegalStateException()
    }

    override fun addNewWeekdays() {
        throw IllegalStateException()
    }

    override fun addWeekdays(weekdays: Weekdays, timeRange: TimeRange) {
        throw IllegalStateException()
    }

    private inner class MonthsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthsLabel: TextView = itemView.findViewById(R.id.monthsLabel)

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
        }
    }

    private inner class WeekdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weekdaysLabel: TextView = itemView.findViewById(R.id.weekdaysLabel)
        private val hoursLabel: TextView = itemView.findViewById(R.id.hoursLabel)

        fun update(row: OpeningWeekdaysRow, rowBefore: OpeningWeekdaysRow?) {
            if (rowBefore != null && row.weekdays == rowBefore.weekdays) {
                weekdaysLabel.text = ""
            } else {
                weekdaysLabel.text = row.weekdays.toLocalizedString(context.resources)
            }

            hoursLabel.text = row.timeRange.toStringUsing(Locale.getDefault(), "–")
        }
    }

    companion object {
        private const val MONTHS = 0
        private const val WEEKDAYS = 1
    }
}

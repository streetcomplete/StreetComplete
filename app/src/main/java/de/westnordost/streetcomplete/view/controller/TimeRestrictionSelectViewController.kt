package de.westnordost.streetcomplete.view.controller

import android.content.res.Resources
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursRow
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.view.OnAdapterItemSelectedListener
import java.util.Locale

/** Manages inputting a time restriction, either inclusive or exclusive, based on opening hours.
 *
 *  I.e. the user can input...
 *  1. whether it applies all the time, only at specific times or always except at specific times
 *  2. specify the times like opening hours
 *  */
class TimeRestrictionSelectViewController(
    private val timeRestrictionsSelect: Spinner,
    private val timesList: RecyclerView,
    private val addTimesButton: View
) {
    var onInputChanged: (() -> Unit)? = null

    @LayoutRes var timeRestrictionsSelectItemResId: Int = R.layout.spinner_item_centered

    var firstDayOfWorkweek: String
        set(value) { timesAdapter.firstDayOfWorkweek = value }
        get() = timesAdapter.firstDayOfWorkweek

    var regularShoppingDays: Int
        set(value) { timesAdapter.regularShoppingDays = value }
        get() = timesAdapter.regularShoppingDays

    var locale: Locale
        set(value) { timesAdapter.locale = value }
        get() = timesAdapter.locale

    /** which time restrictions are selectable for the user */
    var selectableTimeRestrictions: List<TimeRestriction> = TimeRestriction.values().toList()
        set(value) {
            field = value
            timeRestrictionAdapter.clear()
            timeRestrictionAdapter.addAll(value.map { it.toLocalizedString(timeRestrictionsSelect.context.resources) })
        }

    /** currently selected time restriction */
    var timeRestriction: TimeRestriction
        set(value) { timeRestrictionsSelect.setSelection(selectableTimeRestrictions.indexOf(value)) }
        get() = selectableTimeRestrictions[timeRestrictionsSelect.selectedItemPosition]

    var times: List<OpeningHoursRow>
        set(value) { timesAdapter.rows = value.toMutableList() }
        get() = timesAdapter.rows

    val isComplete: Boolean get() =
        timeRestriction == TimeRestriction.AT_ANY_TIME || times.isNotEmpty()

    private val timesAdapter = OpeningHoursAdapter(timesList.context)

    private val timeRestrictionAdapter = ArrayAdapter(
        timeRestrictionsSelect.context,
        timeRestrictionsSelectItemResId,
        TimeRestriction.values().map { it.toLocalizedString(timeRestrictionsSelect.context.resources) }.toMutableList()
    )

    init {
        timesAdapter.registerAdapterDataObserver(AdapterDataChangedWatcher { onInputChanged?.invoke() })
        timesAdapter.firstDayOfWorkweek = firstDayOfWorkweek
        timesAdapter.regularShoppingDays = regularShoppingDays
        timesAdapter.locale = locale

        timesList.adapter = timesAdapter
        addTimesButton.setOnClickListener { timesAdapter.addNewWeekdays() }

        timeRestrictionsSelect.adapter = timeRestrictionAdapter
        if (timeRestrictionsSelect.selectedItemPosition < 0) timeRestrictionsSelect.setSelection(0)
        updateTimesVisibility()
        timeRestrictionsSelect.onItemSelectedListener = OnAdapterItemSelectedListener {
            updateTimesVisibility()
            onInputChanged?.invoke()
        }
    }

    private fun updateTimesVisibility() {
        timesList.isGone = timeRestriction == TimeRestriction.AT_ANY_TIME
        addTimesButton.isGone = timeRestriction == TimeRestriction.AT_ANY_TIME
    }
}

enum class TimeRestriction { AT_ANY_TIME, ONLY_AT_HOURS, EXCEPT_AT_HOURS }

private fun TimeRestriction.toLocalizedString(resources: Resources) = when (this) {
    TimeRestriction.AT_ANY_TIME -> resources.getString(R.string.at_any_time)
    TimeRestriction.ONLY_AT_HOURS -> resources.getString(R.string.only_at_hours)
    TimeRestriction.EXCEPT_AT_HOURS -> resources.getString(R.string.except_at_hours)
}

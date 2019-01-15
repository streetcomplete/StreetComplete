package de.westnordost.streetcomplete.quests.opening_hours

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TimePicker

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange

class TimeRangePickerDialog(
    context: Context,
    startTimeLabel: CharSequence,
    endTimeLabel: CharSequence,
    timeRange: TimeRange?,
    private val callback: (TimeRange) -> Unit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    private val startPicker: TimePicker
    private val endPicker: TimePicker
    private val endPickerContainer: ViewGroup
    private val viewPager: ViewPager
    private val tabLayout: TabLayout

    private val openEndCheckbox: CheckBox

    private val minutesStart get() = startPicker.currentHour * 60 + startPicker.currentMinute

    private val minutesEnd get() = endPicker.currentHour * 60 + endPicker.currentMinute

    init {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_time_range_picker, null)
        setView(view)

        setButton(
            DialogInterface.BUTTON_POSITIVE,
            context.getString(R.string.quest_openingHours_timeSelect_next),
            null as DialogInterface.OnClickListener?
        )
        setButton(
            DialogInterface.BUTTON_NEGATIVE,
            context.getString(android.R.string.cancel),
            null as DialogInterface.OnClickListener?
        )

        startPicker = inflater.inflate(R.layout.time_range_picker_start_picker, null) as TimePicker
        startPicker.setIs24HourView(true)

        endPickerContainer = inflater.inflate(R.layout.time_range_picker_end_picker, null) as ViewGroup
        openEndCheckbox = endPickerContainer.findViewById(R.id.checkBox)
        endPicker = endPickerContainer.findViewById(R.id.picker)
        endPicker.setIs24HourView(true)
        if (timeRange != null) {
            startPicker.currentHour = timeRange.start / 60
            startPicker.currentMinute = timeRange.start % 60

            endPicker.currentHour = timeRange.end / 60
            endPicker.currentMinute = timeRange.end % 60
            openEndCheckbox.isChecked = timeRange.isOpenEnded
        }

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = CustomAdapter(startTimeLabel, endTimeLabel)

        tabLayout = view.findViewById(R.id.tabLayout)
        tabLayout.setupWithViewPager(viewPager)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab)   { setCurrentTab(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab) { }
            override fun onTabReselected(tab: TabLayout.Tab) { }
        })
    }

    private fun setCurrentTab(position: Int) {
        viewPager.currentItem = position
        val buttonResId = if (position == END_TIME_TAB) android.R.string.ok else R.string.quest_openingHours_timeSelect_next
        getButton(DialogInterface.BUTTON_POSITIVE).setText(buttonResId)
    }

    private inner class CustomAdapter(startTimeLabel: CharSequence, endTimeLabel: CharSequence) :
        PagerAdapter() {

        private val labels: Array<CharSequence> = arrayOf(startTimeLabel, endTimeLabel)

        override fun getCount() = labels.size

        override fun isViewFromObject(view: View, obj: Any) =
            when(obj) {
                START_TIME_TAB -> view === startPicker
                END_TIME_TAB   -> view === endPickerContainer
                else           -> false
            }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            when(position) {
                START_TIME_TAB -> container.removeView(startPicker)
                END_TIME_TAB -> container.removeView(endPickerContainer)
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            when(position) {
                START_TIME_TAB -> container.addView(startPicker)
                END_TIME_TAB -> container.addView(endPickerContainer)
            }
            return position
        }

        override fun getPageTitle(position: Int) = labels[position]
    }

    override fun show() {
        super.show()
        // to override the default OK=dismiss() behavior
        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            when (tabLayout.selectedTabPosition) {
                START_TIME_TAB -> setCurrentTab(END_TIME_TAB)
                END_TIME_TAB   -> applyAndDismiss()
            }
        }
    }

    private fun applyAndDismiss() {
        callback(TimeRange(minutesStart, minutesEnd, openEndCheckbox.isChecked))
        dismiss()
    }

    companion object {
        private const val START_TIME_TAB = 0
        private const val END_TIME_TAB = 1
    }
}

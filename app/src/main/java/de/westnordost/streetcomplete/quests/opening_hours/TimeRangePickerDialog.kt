package de.westnordost.streetcomplete.quests.opening_hours

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TimePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange

class TimeRangePickerDialog(
    context: Context,
    startTimeLabel: CharSequence,
    endTimeLabel: CharSequence,
    timeRange: TimeRange?,
    is24HourView: Boolean,
    private val callback: (TimeRange) -> Unit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    private val startPicker: TimePicker
    private val endPicker: TimePicker
    private val endPickerContainer: ViewGroup
    private val viewPager: ViewPager2
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
        startPicker.setIs24HourView(is24HourView)

        endPickerContainer = inflater.inflate(R.layout.time_range_picker_end_picker, null) as ViewGroup
        openEndCheckbox = endPickerContainer.findViewById(R.id.checkBox)
        endPicker = endPickerContainer.findViewById(R.id.picker)
        endPicker.setIs24HourView(is24HourView)
        if (timeRange != null) {
            startPicker.currentHour = timeRange.start / 60
            startPicker.currentMinute = timeRange.start % 60

            endPicker.currentHour = timeRange.end / 60
            endPicker.currentMinute = timeRange.end % 60
            openEndCheckbox.isChecked = timeRange.isOpenEnded
        }

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = TimeRangePickerAdapter()

        tabLayout = view.findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = if(position == 0) startTimeLabel else endTimeLabel
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab)   { setCurrentTab(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab) { }
            override fun onTabReselected(tab: TabLayout.Tab) { }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    private fun setCurrentTab(position: Int) {
        viewPager.currentItem = position
        val buttonResId = if (position == END_TIME_TAB) android.R.string.ok else R.string.quest_openingHours_timeSelect_next
        getButton(DialogInterface.BUTTON_POSITIVE).setText(buttonResId)
    }

    private inner class TimeRangePickerAdapter : RecyclerView.Adapter<TimeRangePickerAdapter.ViewHolder>() {

        override fun getItemCount() = 2

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            })

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val viewGroup = (holder.itemView as FrameLayout)
                viewGroup.removeAllViews()
                viewGroup.addView(if (position == START_TIME_TAB) startPicker else endPickerContainer)
            }

            inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
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

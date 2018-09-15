package de.westnordost.streetcomplete.quests.opening_hours;

import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TimePicker;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange;

public class TimeRangePickerDialog extends AlertDialog implements View.OnClickListener
{
	private static final int
			START_TIME_TAB = 0,
			END_TIME_TAB = 1;

	private final TimePicker startPicker, endPicker;
	private final ViewGroup endPickerContainer;
	private final ViewPager viewPager;
	private final TabLayout tabLayout;

	private final CheckBox openEndCheckbox;

	private final OnTimeRangeChangeListener listener;

	public interface OnTimeRangeChangeListener
	{
		void onTimeRangeChange(TimeRange timeRange);
	}

	public TimeRangePickerDialog(Context context, OnTimeRangeChangeListener listener,
								 CharSequence startTimeLabel, CharSequence endTimeLabel,
								 TimeRange timeRange)
	{
		super(context, R.style.Theme_Bubble_Dialog);

		this.listener = listener;

		final LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.dialog_time_range_picker, null);
		setView(view);

		setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), (OnClickListener) null);
		setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), (OnClickListener) null);

		startPicker = (TimePicker) inflater.inflate(R.layout.time_range_picker_start_picker, null);
		startPicker.setIs24HourView(true);

		endPickerContainer = (ViewGroup) inflater.inflate(R.layout.time_range_picker_end_picker, null);
		openEndCheckbox = endPickerContainer.findViewById(R.id.checkBox);
		endPicker = endPickerContainer.findViewById(R.id.picker);
		endPicker.setIs24HourView(true);
		if(timeRange != null)
		{
			startPicker.setCurrentHour(timeRange.getStart() / 60);
			startPicker.setCurrentMinute(timeRange.getStart() % 60);

			endPicker.setCurrentHour(timeRange.getEnd() / 60);
			endPicker.setCurrentMinute(timeRange.getEnd() % 60);
			openEndCheckbox.setChecked(timeRange.isOpenEnded);
		}

		viewPager = view.findViewById(R.id.view_pager);
		viewPager.setAdapter(new CustomAdapter(startTimeLabel, endTimeLabel));

		tabLayout = view.findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
		{
			@Override public void onTabSelected(TabLayout.Tab tab)
			{
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override public void onTabUnselected(TabLayout.Tab tab)
			{
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override public void onTabReselected(TabLayout.Tab tab)
			{
				viewPager.setCurrentItem(tab.getPosition());
			}
		});
	}

	private class CustomAdapter extends PagerAdapter
	{
		private final CharSequence[] labels;

		public CustomAdapter(CharSequence startTimeLabel, CharSequence endTimeLabel)
		{
			labels = new CharSequence[] {startTimeLabel, endTimeLabel};
		}

		@Override public int getCount()
		{
			return labels.length;
		}

		@Override public boolean isViewFromObject(View view, Object object)
		{
			if(object.equals(START_TIME_TAB)) return view == startPicker;
			if(object.equals(END_TIME_TAB)) return view == endPickerContainer;
			return false;
		}

		@Override public void destroyItem(ViewGroup container, int position, Object object)
		{
			if(position == START_TIME_TAB) container.removeView(startPicker);
			if(position == END_TIME_TAB) container.removeView(endPickerContainer);
		}

		@Override public Object instantiateItem(ViewGroup container, int position)
		{
			if(position == START_TIME_TAB) container.addView(startPicker);
			if(position == END_TIME_TAB) container.addView(endPickerContainer);
			return position;
		}

		@Override public CharSequence getPageTitle(int position)
		{
			return labels[position];
		}
	}

	@Override public void show()
	{
		super.show();
		// to override the default OK=dismiss() behavior
		getButton(BUTTON_POSITIVE).setOnClickListener(this);
	}

	@Override public void onClick(View v)
	{
		switch(tabLayout.getSelectedTabPosition())
		{
			case START_TIME_TAB:
				viewPager.setCurrentItem(END_TIME_TAB);
				break;

			case END_TIME_TAB:
				applyAndDismiss();
				break;
		}
	}

	private void applyAndDismiss()
	{
		if (listener != null)
		{
			listener.onTimeRangeChange(
					new TimeRange(getMinutesStart(), getMinutesEnd(), openEndCheckbox.isChecked()));
		}
		dismiss();
	}

	private int getMinutesStart()
	{
		return startPicker.getCurrentHour() * 60 + startPicker.getCurrentMinute();
	}

	private int getMinutesEnd()
	{
		return endPicker.getCurrentHour() * 60 + endPicker.getCurrentMinute();
	}
}

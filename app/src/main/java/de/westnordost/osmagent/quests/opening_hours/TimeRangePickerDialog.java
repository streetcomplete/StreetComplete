package de.westnordost.osmagent.quests.opening_hours;

import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

import de.westnordost.osmagent.R;

public class TimeRangePickerDialog extends AlertDialog
		implements TimePicker.OnTimeChangedListener, View.OnClickListener
{
	private static final int
			START_TIME_TAB = 0,
			END_TIME_TAB = 1;

	private final TimePicker startPicker, endPicker;
	private final ViewPager viewPager;
	private final TabLayout tabLayout;

	private final View error;

	private final OnTimeRangeChangeListener listener;

	public interface OnTimeRangeChangeListener
	{
		void onTimeRangeChange(int start, int end);
	}

	public TimeRangePickerDialog(Context context, OnTimeRangeChangeListener listener,
								 CharSequence startTimeLabel, CharSequence endTimeLabel,
								 Integer startTime, Integer endTime, CharSequence errorInvalidTime)
	{
		super(context);

		this.listener = listener;

		final LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.time_range_picker_dialog, null);
		setView(view);

		setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), (OnClickListener) null);
		setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), (OnClickListener) null);

		error = view.findViewById(R.id.error_group);
		error.setVisibility(View.INVISIBLE);

		TextView errorText = (TextView) view.findViewById(R.id.error_text);
		errorText.setText(errorInvalidTime);
		AttributeSet a;
		startPicker = (TimePicker) inflater.inflate(R.layout.time_range_picker_picker, null);
		startPicker.setIs24HourView(true);
		endPicker = (TimePicker) inflater.inflate(R.layout.time_range_picker_picker, null);
		endPicker.setIs24HourView(true);
		if(startTime != null)
		{
			startPicker.setCurrentHour(startTime / 60);
			startPicker.setCurrentMinute(startTime % 60);
		}
		if(endTime != null)
		{
			endPicker.setCurrentHour(endTime / 60);
			endPicker.setCurrentMinute(endTime % 60);
		}
		startPicker.setOnTimeChangedListener(this);
		endPicker.setOnTimeChangedListener(this);

		viewPager = (ViewPager) view.findViewById(R.id.view_pager);
		viewPager.setAdapter(new CustomAdapter(startTimeLabel, endTimeLabel));
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

			@Override public void onPageSelected(int position)
			{
				updateButtonEnablement();
			}

			@Override public void onPageScrollStateChanged(int state) { }
		});

		tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
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
			if(object.equals(END_TIME_TAB)) return view == endPicker;
			return false;
		}

		@Override public void destroyItem(ViewGroup container, int position, Object object)
		{
			if(position == START_TIME_TAB) container.removeView(startPicker);
			if(position == END_TIME_TAB) container.removeView(endPicker);
		}

		@Override public Object instantiateItem(ViewGroup container, int position)
		{
			if(position == START_TIME_TAB) container.addView(startPicker);
			if(position == END_TIME_TAB) container.addView(endPicker);
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

	@Override public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
	{
		error.setVisibility(isValid() ? View.INVISIBLE : View.VISIBLE);
		updateButtonEnablement();
	}

	private void updateButtonEnablement()
	{
		getButton(BUTTON_POSITIVE).setEnabled(
				isValid() || tabLayout.getSelectedTabPosition() != END_TIME_TAB);
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
			listener.onTimeRangeChange( getMinutesStart(), getMinutesEnd() );
		}
		dismiss();
	}

	private boolean isValid()
	{
		return getMinutesStart() <= getMinutesEnd();
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

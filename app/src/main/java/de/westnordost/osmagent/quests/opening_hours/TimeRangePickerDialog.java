package de.westnordost.osmagent.quests.opening_hours;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

import de.westnordost.osmagent.R;

public class TimeRangePickerDialog extends AlertDialog
		implements TabHost.OnTabChangeListener, TimePicker.OnTimeChangedListener, View.OnClickListener
{
	private static final String
			START_TIME_TAB = "start",
			END_TIME_TAB = "end";

	private final TimePicker startPicker, endPicker;
	private final TabHost tabs;
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

		startPicker = (TimePicker) view.findViewById(R.id.start_time_picker);
		startPicker.setIs24HourView(true);
		endPicker = (TimePicker) view.findViewById(R.id.end_time_picker);
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

		tabs = (TabHost) view.findViewById(R.id.tab_host);
		tabs.setup();

		TabHost.TabSpec startTimePage = tabs.newTabSpec(START_TIME_TAB);
		startTimePage.setContent(R.id.start_time_picker);
		startTimePage.setIndicator(startTimeLabel);
		tabs.addTab(startTimePage);

		TabHost.TabSpec endTimePage = tabs.newTabSpec(END_TIME_TAB);
		endTimePage.setContent(R.id.end_time_picker);
		endTimePage.setIndicator(endTimeLabel);
		tabs.addTab(endTimePage);

		tabs.setOnTabChangedListener(this);

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

	@Override public void onTabChanged(String tabId)
	{
		updateButtonEnablement();
	}

	private void updateButtonEnablement()
	{
		getButton(BUTTON_POSITIVE).setEnabled(
				isValid() || !(tabs.getCurrentTabTag().equals(END_TIME_TAB)));
	}

	@Override public void onClick(View v)
	{
		switch(tabs.getCurrentTabTag())
		{
			case START_TIME_TAB:
				tabs.setCurrentTabByTag(END_TIME_TAB);
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

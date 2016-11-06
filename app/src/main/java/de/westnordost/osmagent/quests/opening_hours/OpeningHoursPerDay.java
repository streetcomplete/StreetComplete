package de.westnordost.osmagent.quests.opening_hours;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmagent.R;

public class OpeningHoursPerDay extends LinearLayout
{
	// this could go into per-country localization when this page (or any other source)
	// https://en.wikipedia.org/wiki/Shopping_hours contains more information about typical
	// opening hours per country
	private static final CircularSection
			TYPICAL_OPENING_TIMES = new CircularSection(8 * 60, 18 * 60),
			TYPICAL_AM_OPENING_TIMES = new CircularSection(8 * 60, 12 * 60),
			TYPICAL_PM_OPENING_TIMES = new CircularSection(14 * 60, 18 * 60);

	private Map<TextView, CircularSection> ranges = new HashMap<>();

	private Button btnAdd;
	private ViewGroup rows;

	private OnOpeningTimesDefinedListener listener;

	public interface OnOpeningTimesDefinedListener
	{
		void onOpeningTimesDefined(boolean defined);
	}

	public OpeningHoursPerDay(Context context)
	{
		super(context);
		init();
	}

	public OpeningHoursPerDay(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.list_with_bottom_add_btn, this, true);

		btnAdd = (Button) findViewById(R.id.btn_add);
		btnAdd.setText(R.string.quest_openingHours_add_hours);
		btnAdd.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				add();
			}
		});
		rows = (ViewGroup) findViewById(R.id.rows);
	}

	public void setOnOpeningTimesDefinedListener(OnOpeningTimesDefinedListener listener)
	{
		this.listener = listener;
	}

	public void add()
	{
		CircularSection range = getOpeningHoursSuggestion();

		openSetTimeRangeDialog(new TimeRangePickerDialog.OnTimeRangeChangeListener()
		{
			@Override public void onTimeRangeChange(int start, int end)
			{
				add(start, end);
			}
		}, range.getStart(), range.getEnd());
	}

	public void add(int start, int end)
	{
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		final ViewGroup row = (ViewGroup) inflater.inflate(R.layout.quest_opening_hours_times_row, rows, false);
		View btnDelete = row.findViewById(R.id.delete);
		btnDelete.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				remove(row);
			}
		});

		final TextView fromTo = (TextView) row.findViewById(R.id.hours_from_to);
		fromTo.setOnClickListener( new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				CircularSection range = ranges.get(fromTo);

				openSetTimeRangeDialog(	new TimeRangePickerDialog.OnTimeRangeChangeListener()
				{
					@Override public void onTimeRangeChange(int start, int end)
					{
						putTime(fromTo, start, end);
					}
				}, range.getStart(), range.getEnd());
			}
		});

		putTime(fromTo, start, end);
		rows.addView(row);
		if(listener != null)
		{
			listener.onOpeningTimesDefined(true);
		}
	}

	public ArrayList<CircularSection> getAll()
	{
		return new ArrayList<>(ranges.values());
	}

	public void addAll(ArrayList<CircularSection> data)
	{
		for (CircularSection range : data)
		{
			add(range.getStart(), range.getEnd());
		}
	}

	public void remove(ViewGroup view)
	{
		final TextView child = (TextView) view.findViewById(R.id.hours_from_to);
		ranges.remove(child);
		rows.removeView(view);
		if(ranges.isEmpty() && listener != null)
		{
			listener.onOpeningTimesDefined(false);
		}
	}

	@SuppressLint("DefaultLocale") private static String timeOfDayToString(int minutes)
	{
		return String.format("%02d:%02d", minutes / 60, minutes % 60);
	}

	private void openSetTimeRangeDialog(TimeRangePickerDialog.OnTimeRangeChangeListener callback,
										Integer startTime, Integer endTime)
	{
		String startLabel = getResources().getString(R.string.quest_openingHours_start_time);
		String endLabel = getResources().getString(R.string.quest_openingHours_end_time);
		String errorText = getResources().getString(R.string.quest_openingHours_invalid_hours_range);

		new TimeRangePickerDialog(
				getContext(), callback, startLabel, endLabel, startTime, endTime, errorText).show();
	}

	public String getOpeningHoursString()
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for(CircularSection range : ranges.values())
		{
			if(!first)	result.append(", ");
			else		first = false;

			result.append(timeOfDayToString(range.getStart()));
			result.append("-");
			int end = range.getEnd();
			if(end == 0) end = 60*24;
			result.append(timeOfDayToString(end));
		}

		return result.toString();
	}

	private void putTime(TextView view, int start, int end)
	{
		if(end == 0) end = 60*24;
		view.setText(timeOfDayToString(start) + " – " + timeOfDayToString(end));
		ranges.put(view, new CircularSection(start, end));
	}

	private @NonNull CircularSection getOpeningHoursSuggestion()
	{
		if(ranges.values().size() == 1)
		{
			CircularSection openingTime = ranges.values().iterator().next();

			boolean isAm = openingTime.intersects(TYPICAL_AM_OPENING_TIMES);
			boolean isPm = openingTime.intersects(TYPICAL_PM_OPENING_TIMES);

			if(isAm && !isPm)
			{
				return TYPICAL_PM_OPENING_TIMES;
			}
			if(isPm && !isAm)
			{
				return TYPICAL_AM_OPENING_TIMES;
			}
		}
		return TYPICAL_OPENING_TIMES;
	}
}

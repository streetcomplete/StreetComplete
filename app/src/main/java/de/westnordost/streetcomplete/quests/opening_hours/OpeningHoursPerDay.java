package de.westnordost.streetcomplete.quests.opening_hours;

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

import de.westnordost.streetcomplete.R;

public class OpeningHoursPerDay extends LinearLayout
{
	// this could go into per-country localization when this page (or any other source)
	// https://en.wikipedia.org/wiki/Shopping_hours contains more information about typical
	// opening hours per country
	private static final TimeRange
			TYPICAL_OPENING_TIMES = new TimeRange(8 * 60, 18 * 60, false),
			TYPICAL_AM_OPENING_TIMES = new TimeRange(8 * 60, 12 * 60, false),
			TYPICAL_PM_OPENING_TIMES = new TimeRange(14 * 60, 18 * 60, false);

	private Map<TextView, TimeRange> ranges = new HashMap<>();

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
		TimeRange range = getOpeningHoursSuggestion();

		openSetTimeRangeDialog(new TimeRangePickerDialog.OnTimeRangeChangeListener()
		{
			@Override public void onTimeRangeChange(TimeRange timeRange)
			{
				add(timeRange);
			}
		}, range);
	}

	public void add(TimeRange timeRange)
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
				TimeRange range = ranges.get(fromTo);

				openSetTimeRangeDialog(	new TimeRangePickerDialog.OnTimeRangeChangeListener()
				{
					@Override public void onTimeRangeChange(TimeRange timeRange)
					{
						putTime(fromTo, timeRange);
					}
				}, range);
			}
		});

		putTime(fromTo, timeRange);
		rows.addView(row);
		if(listener != null)
		{
			listener.onOpeningTimesDefined(true);
		}
	}

	public ArrayList<TimeRange> getAll()
	{
		return new ArrayList<>(ranges.values());
	}

	public void addAll(ArrayList<TimeRange> data)
	{
		for (TimeRange range : data)
		{
			add(range);
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

	private void openSetTimeRangeDialog(TimeRangePickerDialog.OnTimeRangeChangeListener callback,
										TimeRange timeRange)
	{
		String startLabel = getResources().getString(R.string.quest_openingHours_start_time);
		String endLabel = getResources().getString(R.string.quest_openingHours_end_time);

		new TimeRangePickerDialog(
				getContext(), callback, startLabel, endLabel, timeRange).show();
	}

	public String getOpeningHoursString()
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for(TimeRange range : ranges.values())
		{
			if(!first)	result.append(",");
			else		first = false;
			result.append(range.toStringUsing("-"));
		}

		return result.toString();
	}

	private void putTime(TextView view, TimeRange timeRange)
	{
		view.setText(timeRange.toStringUsing("–"));
		ranges.put(view, timeRange);
	}

	private @NonNull TimeRange getOpeningHoursSuggestion()
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

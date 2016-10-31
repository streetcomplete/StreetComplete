package de.westnordost.osmagent.dialogs.opening_hours;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.Injector;
import de.westnordost.osmagent.R;
import de.westnordost.osmagent.data.meta.CurrentCountry;
import de.westnordost.osmagent.data.meta.WorkWeek;

public class OpeningHoursPerWeek extends LinearLayout
{
	// in the order of Calendar.MONDAY, TUESDAY etc...
	private static final String[] OSM_ABBR_WEEKDAYS = {"", "Su","Mo","Tu","We","Th","Fr","Sa"};

	private static final int MAX_WEEKDAY_INDEX = 6;

	@Inject CurrentCountry currentCountry;

	private Map<OpeningHoursPerDay, CircularSection> ranges = new HashMap<>();

	private Button btnAdd;
	private ViewGroup rows;

	public OpeningHoursPerWeek(Context context)
	{
		super(context);
		init();
	}

	public OpeningHoursPerWeek(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		Injector.instance.getApplicationComponent().inject(this);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.list_with_bottom_add_btn, this, true);

		btnAdd = (Button) findViewById(R.id.btn_add);
		btnAdd.setText(R.string.quest_openingHours_add_weekdays);
		btnAdd.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				add();
			}
		});
		rows = (ViewGroup) findViewById(R.id.rows);
	}

	/** Open dialog to let the user specify the range and add this new row*/
	public void add()
	{
		CircularSection range = getRangeSuggestion();
		openSetRangeDialog(
				new RangePickerDialog.OnRangeChangeListener()
				{
					@Override public void onRangeChange(int startIndex, int endIndex)
					{
						add(startIndex, endIndex);
					}
				}, range.getStart(), range.getEnd()
		);
	}

	/** add a new row with the given range */
	public OpeningHoursPerDay add(int startIndex, int endIndex)
	{
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		final LinearLayout row = (LinearLayout) inflater.inflate(
				R.layout.quest_opening_hours_weekday_row, rows, false);

		final View btnDelete = row.findViewById(R.id.delete);
		final OpeningHoursPerDay openingHoursPerDay =
				(OpeningHoursPerDay) row.findViewById(R.id.hours_select_container);
		final TextView fromTo = (TextView) row.findViewById(R.id.weekday_from_to);

		btnDelete.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				remove(row);
			}
		});

		if(rows.getChildCount() == 0)
		{
			btnDelete.setVisibility(GONE);
		}
		else
		{
			openingHoursPerDay.setOnOpeningTimesDefinedListener(
					new OpeningHoursPerDay.OnOpeningTimesDefinedListener()
					{
						@Override public void onOpeningTimesDefined(boolean defined)
						{
							btnDelete.setVisibility(defined ? GONE : VISIBLE);
						}
					});
		}

		fromTo.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				CircularSection range = ranges.get(openingHoursPerDay);

				openSetRangeDialog(new RangePickerDialog.OnRangeChangeListener()
				{
					@Override public void onRangeChange(int startIndex, int endIndex)
					{
						putData(fromTo, openingHoursPerDay, startIndex, endIndex);
					}
				}, range.getStart(), range.getEnd());
			}
		});

		putData(fromTo, openingHoursPerDay, startIndex, endIndex);
		rows.addView(row);
		return openingHoursPerDay;
	}

	public HashMap<CircularSection, ArrayList<CircularSection>> getAll()
	{
		HashMap<CircularSection, ArrayList<CircularSection>> result = new HashMap<>(2);
		for (Map.Entry<OpeningHoursPerDay, CircularSection> e : ranges.entrySet())
		{
			result.put(e.getValue(), e.getKey().getAll());
		}
		return result;
	}

	public void addAll(HashMap<CircularSection, ArrayList<CircularSection>> data)
	{
		ArrayList<CircularSection> sortedKeys = new ArrayList<>(data.keySet());
		Collections.sort(sortedKeys);
		for(CircularSection range : sortedKeys)
		{
			OpeningHoursPerDay dayView = add(range.getStart(), range.getEnd());
			dayView.addAll(data.get(range));
		}
	}

	public void remove(ViewGroup view)
	{
		OpeningHoursPerDay child =	(OpeningHoursPerDay) view.findViewById(R.id.hours_select_container);
		ranges.remove(child);
		rows.removeView(view);
	}

	private @NonNull CircularSection getRangeSuggestion()
	{
		List<CircularSection> weekdays = getUnmentionedWeekdays();
		if(weekdays.isEmpty())
		{
			return new CircularSection(0,0);
		}
		return weekdays.get(0);
	}

	private List<CircularSection> getUnmentionedWeekdays()
	{
		return new NumberSystem(0,MAX_WEEKDAY_INDEX).complement(ranges.values());
	}

	private void openSetRangeDialog(RangePickerDialog.OnRangeChangeListener callback,
									Integer startIndex, Integer endIndex)
	{
		String[] weekdays = normalizeWeekdays(DateFormatSymbols.getInstance().getWeekdays());
		String selectWeekdays = getResources().getString(R.string.quest_openingHours_chooseWeekdaysTitle);

		new RangePickerDialog(getContext(), callback, weekdays, startIndex, endIndex, selectWeekdays).show();
	}

	public String getOpeningHoursString()
	{
		return getOpeningHoursString("");
	}

	public String getOpeningHoursString(String prepend)
	{
		String[] weekdays = normalizeWeekdays(OSM_ABBR_WEEKDAYS);

		StringBuilder result = new StringBuilder();
		boolean first = true;

		for(Map.Entry<OpeningHoursPerDay, CircularSection> entry : ranges.entrySet())
		{
			String hourlyOpeningHours = entry.getKey().getOpeningHoursString();

			// while it is legal to specify opening hours without actual opening hours (just the
			// days), this form deliberately does not accept it. If it is really open the whole day
			// and night, the user must select the time 0-24
			if(hourlyOpeningHours.isEmpty()) continue;

			CircularSection range = entry.getValue();

			if(!first)	result.append("; ");
			else		first = false;

			result.append(prepend);

			boolean isAllWeek = range.getStart() == 0 && range.getEnd() == weekdays.length-1;
			if(!isAllWeek)
			{
				result.append(weekdays[range.getStart()]);
				if(range.getStart() != range.getEnd())
				{
					result.append("-").append(weekdays[range.getEnd()]);
				}
				result.append(" ");
			}

			result.append(hourlyOpeningHours);
		}

		return result.toString();
	}

	private void putData(TextView view, OpeningHoursPerDay openingHoursPerDay, int startIndex, int endIndex)
	{
		String[] abbrWeekdays = normalizeWeekdays(DateFormatSymbols.getInstance().getShortWeekdays());
		StringBuilder fromToText = new StringBuilder();
		fromToText.append(abbrWeekdays[startIndex]);
		if(startIndex != endIndex)
		{
			fromToText.append(" – ");
			fromToText.append(abbrWeekdays[endIndex]);
		}
		view.setText(fromToText.toString());
		ranges.put(openingHoursPerDay, new CircularSection(startIndex, endIndex));
	}

	/** order the days so that the first workday of the current country is at the front (which may
	 *  be a different day than the firt day in the week on the calendar) */
	private String[] normalizeWeekdays(String[] calendarWeekdays)
	{
		int j = WorkWeek.getFirstDay(currentCountry.getLocale());
		String[] result = new String[7];
		for(int i=0; i<result.length; ++i)
		{
			result[i] = calendarWeekdays[j++];
			if(j >= calendarWeekdays.length)
			{
				j = Calendar.SUNDAY; // sunday is 1, 0=first elem in array is undefined
			}
		}
		return result;
	}
}

package de.westnordost.streetcomplete.quests.opening_hours;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.util.SerializedSavedState;

public class OpeningHoursPerMonth extends LinearLayout
{
	private static final int MAX_MONTH_INDEX = 11;

	private Map<OpeningHoursPerWeek, CircularSection> ranges = new HashMap<>();
	private Button btnAdd;
	private ViewGroup rows;

	public OpeningHoursPerMonth(Context context)
	{
		super(context);
		init();
	}

	public OpeningHoursPerMonth(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	@Override protected Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		return new SerializedSavedState(superState, getAll());
	}

	@Override protected void onRestoreInstanceState(Parcelable state)
	{
		SerializedSavedState savedState = (SerializedSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		addAll((HashMap) savedState.get(HashMap.class));
	}

	@Override protected void dispatchSaveInstanceState(SparseArray<Parcelable> container)
	{
		super.dispatchFreezeSelfOnly(container);
	}

	@Override protected void dispatchRestoreInstanceState(SparseArray container)
	{
		super.dispatchThawSelfOnly(container);
	}

	private void init()
	{
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.list_with_bottom_add_btn, this, true);

		btnAdd = (Button) findViewById(R.id.btn_add);
		btnAdd.setText(R.string.quest_openingHours_add_months);
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
						add(startIndex, endIndex).addDefault();
					}
				}, range.getStart(), range.getEnd()
		);
	}

	/** add default row (fills weekdays per months also with its default) */
	public void addDefault()
	{
		CircularSection range = getRangeSuggestion();
		add(range.getStart(), range.getEnd()).addDefault();
	}

	/** add a new row with the given range */
	public OpeningHoursPerWeek add(int startIndex, int endIndex)
	{
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		final ViewGroup row = (ViewGroup) inflater.inflate(R.layout.quest_opening_hours_month_row, rows, false);

		View btnDelete = row.findViewById(R.id.delete);
		btnDelete.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				remove(row);
			}
		});
		if(rows.getChildCount() == 0)
		{
			btnDelete.setVisibility(INVISIBLE);
		}

		final TextView fromTo = (TextView) row.findViewById(R.id.months_from_to);
		final OpeningHoursPerWeek openingHoursPerWeek =
				(OpeningHoursPerWeek) row.findViewById(R.id.weekday_select_container);

		fromTo.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				CircularSection range = ranges.get(openingHoursPerWeek);

				openSetRangeDialog(	new RangePickerDialog.OnRangeChangeListener()
				{
					@Override public void onRangeChange(int startIndex, int endIndex)
					{
						putData(fromTo, openingHoursPerWeek, startIndex, endIndex);
					}
				}, range.getStart(), range.getEnd());
			}
		});

		putData(fromTo, openingHoursPerWeek, startIndex, endIndex);
		rows.addView(row);
		return openingHoursPerWeek;
	}

	public void remove(ViewGroup view)
	{
		OpeningHoursPerWeek child =	(OpeningHoursPerWeek) view.findViewById(R.id.weekday_select_container);
		ranges.remove(child);
		rows.removeView(view);
		updateAddButtonVisibility();
	}

	public HashMap<CircularSection, HashMap<Weekdays, ArrayList<CircularSection>>> getAll()
	{
		HashMap<CircularSection, HashMap<Weekdays, ArrayList<CircularSection>>> result =
				new HashMap<>(2); // madness!!
		for (Map.Entry<OpeningHoursPerWeek, CircularSection> e : ranges.entrySet())
		{
			result.put(e.getValue(), e.getKey().getAll());
		}
		return result;
	}

	public void addAll(
			HashMap<CircularSection, HashMap<Weekdays, ArrayList<CircularSection>>> data)
	{
		ArrayList<CircularSection> sortedKeys = new ArrayList<>(data.keySet());
		Collections.sort(sortedKeys);
		for(CircularSection range : sortedKeys)
		{
			OpeningHoursPerWeek weekView = add(range.getStart(), range.getEnd());
			weekView.addAll(data.get(range));
		}
	}

	private @NonNull CircularSection getRangeSuggestion()
	{
		List<CircularSection> months = getUnmentionedMonths();
		if(months.isEmpty())
		{
			return new CircularSection(0,MAX_MONTH_INDEX);
		}
		return months.get(0);
	}

	private void updateAddButtonVisibility()
	{
		boolean visible = !getUnmentionedMonths().isEmpty();
		btnAdd.setVisibility(visible ? VISIBLE : GONE);
	}

	private List<CircularSection> getUnmentionedMonths()
	{
		return new NumberSystem(0,MAX_MONTH_INDEX).complemented(ranges.values());
	}

	public String getOpeningHoursString()
	{
		// the US locale is important here as this is the OSM format for dates
		String[] months = DateFormatSymbols.getInstance(Locale.US).getShortMonths();

		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (Map.Entry<OpeningHoursPerWeek, CircularSection> entry : ranges.entrySet())
		{
			CircularSection range = entry.getValue();

			boolean isAllYear = range.getStart() == 0 && range.getEnd() == months.length-1;

			StringBuilder monthsBuilder = new StringBuilder();
			if(!isAllYear)
			{
				// the month(s) must be prepended to each weekday rule
				monthsBuilder.append(months[range.getStart()]);
				if(range.getStart() != range.getEnd())
				{
					monthsBuilder.append("-").append(months[range.getEnd()]);
				}
				monthsBuilder.append(": ");
			}

			OpeningHoursPerWeek weeklyOpeningHours = entry.getKey();

			String monthRangeString = weeklyOpeningHours.getOpeningHoursString(monthsBuilder.toString());

			if(!monthRangeString.isEmpty())
			{
				if(!first)	result.append("; ");
				else		first = false;

				result.append(monthRangeString);
			}
		}

		return result.toString();
	}

	private void openSetRangeDialog(RangePickerDialog.OnRangeChangeListener callback,
									Integer startIndex, Integer endIndex)
	{
		String[] months = DateFormatSymbols.getInstance().getMonths();
		String selectMonths = getResources().getString(R.string.quest_openingHours_chooseMonthsTitle);
		new RangePickerDialog(getContext(), callback, months, startIndex, endIndex, selectMonths).show();
	}

	private void putData(TextView view, OpeningHoursPerWeek child, int startIndex, int endIndex)
	{
		String[] months = DateFormatSymbols.getInstance().getMonths();
		StringBuilder fromToText = new StringBuilder();
		if(startIndex == 0 && endIndex == months.length-1)
		{
			fromToText.append(getResources().getString(R.string.quest_openingHours_allYear));
		}
		else
		{
			fromToText.append(months[startIndex]);
			if(startIndex != endIndex)
			{
				fromToText.append(" – ");
				fromToText.append(months[endIndex]);
			}
		}
		fromToText.append(":");
		view.setText(fromToText.toString());
		ranges.put(child, new CircularSection(startIndex, endIndex));

		updateAddButtonVisibility();
	}
}

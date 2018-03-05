package de.westnordost.streetcomplete.quests.collection_times;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class OpeningMonths
{
	public static final int MAX_MONTH_INDEX = 11;

	CircularSection months;
	ArrayList<OpeningWeekdays> weekdaysList;

	public OpeningMonths()
	{
		months = new CircularSection(0, MAX_MONTH_INDEX);
		weekdaysList = new ArrayList<>();
	}

	public OpeningMonths(CircularSection months, OpeningWeekdays initialWeekdays)
	{
		this.months = months;
		weekdaysList = new ArrayList<>();
		weekdaysList.add(initialWeekdays);
	}

	public String getLocalizedMonthsString()
	{
		return getMonthsString(DateFormatSymbols.getInstance().getMonths(), "–") + ": ";
	}

	public String toString()
	{
		// the US locale is important here as this is the OSM format for dates
		String monthsString = "";
		if(!isWholeYear())
		{
			monthsString = getMonthsString(DateFormatSymbols.getInstance(Locale.US).getShortMonths(), "-") + ": ";
		}

		StringBuilder result = new StringBuilder();
		boolean firstDays = true;

		Weekdays lastWeekdays = null;
		for (OpeningWeekdays ow : weekdaysList)
		{
			boolean isSameWeekdays = lastWeekdays != null && lastWeekdays.equals(ow.weekdays);
			if(!isSameWeekdays)
			{
				if(!firstDays)	result.append(", ");
				else		firstDays = false;

				result.append(monthsString);
				result.append(ow.weekdays.toString());
				result.append(" ");
				result.append(ow.time.toStringUsing("-"));
			}
			else
			{
				result.append(",");
				result.append(ow.time.toStringUsing("-"));
			}
			lastWeekdays = ow.weekdays;
		}

		return result.toString();
	}

	private boolean isWholeYear()
	{
		NumberSystem aYear = new NumberSystem(0,OpeningMonths.MAX_MONTH_INDEX);
		return aYear.complemented(Collections.singletonList(months)).isEmpty();
	}

	private String getMonthsString(String[] names, String range)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(names[months.getStart()]);
		if(months.getStart() != months.getEnd())
		{
			sb.append(range);
			sb.append(names[months.getEnd()]);
		}
		return sb.toString();
	}
}

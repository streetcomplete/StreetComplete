package de.westnordost.streetcomplete.quests.opening_hours.model;

import android.content.res.Resources;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;

public class Weekdays
{
	// in ISO 8601 order
	private static final String[] OSM_ABBR_WEEKDAYS = {"Mo","Tu","We","Th","Fr","Sa","Su","PH"};
	private static final int PUBLIC_HOLIDAY = 7;
	private static final int WEEKDAYS_VALUES = OSM_ABBR_WEEKDAYS.length;

	private static final NumberSystem WEEKDAY_NUMBER_SYSTEM = new NumberSystem(0, 6);

	private boolean[] data = new boolean[WEEKDAYS_VALUES];

	public static String[] getNames(Resources r)
	{
		DateFormatSymbols symbols = DateFormatSymbols.getInstance();
		String[] result = Arrays.copyOf(toIso8601Order(symbols.getWeekdays()),WEEKDAYS_VALUES);
		result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays);
		return result;
	}

	public static String[] getShortNames(Resources r)
	{
		DateFormatSymbols symbols = DateFormatSymbols.getInstance();
		String[] result = Arrays.copyOf(toIso8601Order(symbols.getShortWeekdays()),WEEKDAYS_VALUES);
		result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays_short);
		return result;
	}

	public static int getWeekdayIndex(String name)
	{
		return Arrays.asList(OSM_ABBR_WEEKDAYS).indexOf(name);
	}

	private static String[] toIso8601Order(String[] javaCalendarOrder)
	{
		String[] result = new String[7];
		int shift = 1;
		for(int i = 0; i < 7; ++i)
		{
			result[i] = javaCalendarOrder[1+(i+shift)%7];
		}
		return result;
	}

	public Weekdays()
	{

	}

	public Weekdays(boolean[] selection)
	{
		for(int i=0; i<selection.length && i <data.length; ++i)
		{
			data[i] = selection[i];
		}
	}

	public boolean[] getSelection()
	{
		return Arrays.copyOf(data, data.length);
	}

	@Override public String toString()
	{
		return toStringUsing(OSM_ABBR_WEEKDAYS, ",", "-");
	}

	public String toLocalizedString(Resources r)
	{
		return toStringUsing(Weekdays.getShortNames(r), ", ", "–");
	}

	private String toStringUsing(String[] names, String seperator, String range)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		for(CircularSection section : getWeekdaysAsCircularSections())
		{
			if(!first)	sb.append(seperator);
			else		first = false;

			sb.append(names[section.getStart()]);
			if (section.getStart() != section.getEnd())
			{
				// i.e. Mo-We
				if(WEEKDAY_NUMBER_SYSTEM.getSize(section) > 2)
				{
					sb.append(range);
				}
				// Mo,Tu
				else
				{
					sb.append(seperator);
				}
				sb.append(names[section.getEnd()]);
			}
		}

		// the rest (special days). Currently only "PH"
		for(int i = 7; i < data.length; ++i)
		{
			if(!data[i]) continue;

			if(!first)	sb.append(seperator);
			else		first = false;

			sb.append(names[i]);
		}

		return sb.toString();
	}

	private List<CircularSection> getWeekdaysAsCircularSections()
	{
		List<CircularSection> result = new ArrayList<>();
		Integer currentStart = null;
		for(int i = 0; i < 7; ++i)
		{
			if(currentStart == null)
			{
				if(data[i]) currentStart = i;
			}
			else
			{
				if(!data[i])
				{
					result.add(new CircularSection(currentStart, i-1));
					currentStart = null;
				}
			}
		}
		// section that goes until the end
		if(currentStart != null)
		{
			result.add(new CircularSection(currentStart, 6));
		}

		return WEEKDAY_NUMBER_SYSTEM.merged(result);
	}

	public boolean intersects(Weekdays other)
	{
		for (int i = 0; i < data.length; i++)
		{
			if(data[i] && other.data[i]) return true;
		}
		return false;
	}

	public boolean isSelectionEmpty()
	{
		for (boolean day : data)
		{
			if (day) return false;
		}
		return true;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		return Arrays.equals(data, ((Weekdays) o).data);
	}

	@Override public int hashCode()
	{
		return Arrays.hashCode(data);
	}
}

package de.westnordost.streetcomplete.quests.opening_hours.model;

import java.text.DateFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OpeningMonths
{
	public static final int MAX_MONTH_INDEX = 11;

	public CircularSection months;
	public List<List<OpeningWeekdays>> weekdaysClusters;

	public OpeningMonths(CircularSection months, List<List<OpeningWeekdays>> weekdaysClusters)
	{
		this.months = months;
		this.weekdaysClusters = weekdaysClusters;
	}

	public String toString()
	{
		// the US locale is important here as this is the OSM format for dates
		String monthsString = "";
		if(!isWholeYear())
		{
			monthsString = months.toStringUsing(DateFormatSymbols.getInstance(Locale.US).getShortMonths(), "-") + ": ";
		}

		StringBuilder result = new StringBuilder();
		boolean firstCluster = true;
		for (List<OpeningWeekdays> weekdaysCluster : weekdaysClusters)
		{
			if (!firstCluster) result.append("; ");
			else firstCluster = false;

			boolean firstDays = true;
			for (OpeningWeekdays ow : weekdaysCluster)
			{
				if (!firstDays) result.append(", ");
				else firstDays = false;

				result.append(monthsString);
				result.append(ow.weekdays.toString());
				result.append(" ");
				boolean firstHours = true;
				for (TimeRange timeRange : ow.timeRanges)
				{
					if (!firstHours) result.append(",");
					else firstHours = false;
					result.append(timeRange.toStringUsing("-"));
				}
			}
		}

		return result.toString();
	}

	private boolean isWholeYear()
	{
		NumberSystem aYear = new NumberSystem(0,OpeningMonths.MAX_MONTH_INDEX);
		return aYear.complemented(Collections.singletonList(months)).isEmpty();
	}

	public boolean containsSelfIntersectingOpeningWeekdays()
	{
		for (List<OpeningWeekdays> weekdaysCluster : weekdaysClusters)
		{
			for (OpeningWeekdays openingWeekdays : weekdaysCluster)
			{
				if(openingWeekdays.isSelfIntersecting()) return true;
			}
		}
		return false;
	}
}

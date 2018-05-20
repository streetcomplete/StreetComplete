package de.westnordost.streetcomplete.quests.opening_hours.adapter;

import java.util.ArrayList;

import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection;

public class OpeningMonthsRow
{
	private static final int MAX_MONTH_INDEX = 11;

	CircularSection months;
	ArrayList<OpeningWeekdaysRow> weekdaysList;

	public OpeningMonthsRow()
	{
		months = new CircularSection(0, MAX_MONTH_INDEX);
		weekdaysList = new ArrayList<>();
	}

	public OpeningMonthsRow(CircularSection months, OpeningWeekdaysRow initialWeekdays)
	{
		this.months = months;
		weekdaysList = new ArrayList<>();
		weekdaysList.add(initialWeekdays);
	}
}

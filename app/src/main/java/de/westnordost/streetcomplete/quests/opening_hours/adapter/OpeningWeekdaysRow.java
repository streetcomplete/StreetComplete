package de.westnordost.streetcomplete.quests.opening_hours.adapter;

import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange;
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;

public class OpeningWeekdaysRow
{
	public OpeningWeekdaysRow(Weekdays weekdays, TimeRange timeRange)
	{
		this.weekdays = weekdays;
		this.timeRange = timeRange;
	}

	public Weekdays weekdays;
	public TimeRange timeRange;
}

package de.westnordost.streetcomplete.quests.postbox_collection_times;

import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;

public class WeekdaysTimes
{
	public WeekdaysTimes(Weekdays weekdays, int minutes)
	{
		this.weekdays = weekdays;
		this.minutes = minutes;
	}

	public Weekdays weekdays;
	public int minutes;
}

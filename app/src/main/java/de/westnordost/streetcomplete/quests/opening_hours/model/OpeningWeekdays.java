package de.westnordost.streetcomplete.quests.opening_hours.model;

import java.util.List;

public class OpeningWeekdays
{
	public OpeningWeekdays(Weekdays weekdays, List<TimeRange> timeRanges)
	{
		this.weekdays = weekdays;
		this.timeRanges = timeRanges;
	}

	public Weekdays weekdays;
	public List<TimeRange> timeRanges;

	public boolean intersects(OpeningWeekdays other)
	{
		if(!weekdays.intersects(other.weekdays)) return false;
		for (TimeRange timeRange : timeRanges)
		{
			for (TimeRange otherTimeRange : other.timeRanges)
			{
				if(timeRange.intersects(otherTimeRange)) return true;
			}
		}
		return false;
	}

	public boolean isSelfIntersecting()
	{
		for (int i = 0; i < timeRanges.size(); i++)
		{
			for (int j = i+1; j < timeRanges.size(); j++)
			{
				if(timeRanges.get(i).intersects(timeRanges.get(j))) return true;
			}
		}
		return false;
	}
}

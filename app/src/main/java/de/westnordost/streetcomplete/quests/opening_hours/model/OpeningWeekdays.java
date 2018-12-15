package de.westnordost.streetcomplete.quests.opening_hours.model;

import java.util.ArrayList;
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
		OpeningWeekdays[] these = timeExtendsToNextDay() ? splitAtMidnight() : new OpeningWeekdays[]{this};
		OpeningWeekdays[] others = other.timeExtendsToNextDay() ? other.splitAtMidnight() : new OpeningWeekdays[]{other};

		for (OpeningWeekdays i : these)
		{
			for(OpeningWeekdays it : others)
			{
				if (i.intersectsWhenNoTimeExtendsToNextDay(it)) return true;
			}
		}
		return false;
	}


	private boolean intersectsWhenNoTimeExtendsToNextDay(OpeningWeekdays other)
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

	public boolean intersectsWeekdays(OpeningWeekdays other)
	{
		return weekdays.intersects(other.weekdays)
			|| timeExtendsToNextDay() && createNextDayWeekdays().intersects(other.weekdays)
			|| other.timeExtendsToNextDay() && other.createNextDayWeekdays().intersects(weekdays);
	}

	/** for example "20:00-03:00" */
	private boolean timeExtendsToNextDay()
	{
		for (TimeRange timeRange : timeRanges)
		{
			if(timeRange.loops()) return true;
		}
		return false;
	}

	private OpeningWeekdays[] splitAtMidnight()
	{
		List<TimeRange> beforeMidnight = new ArrayList<>();
		List<TimeRange> afterMidnight = new ArrayList<>();
		for (TimeRange timeRange : timeRanges)
		{
			if(timeRange.loops())
			{
				beforeMidnight.add(new TimeRange(timeRange.getStart(), 24*60));
				afterMidnight.add(new TimeRange(0, timeRange.getEnd(), timeRange.isOpenEnded));
			} else {
				beforeMidnight.add(timeRange);
			}
		}
		return new OpeningWeekdays[] {
			new OpeningWeekdays(weekdays, beforeMidnight),
			new OpeningWeekdays(createNextDayWeekdays(), afterMidnight)
		};
	}

	/** For example creates a "Th, Su" for a "Mo-We, Sa" */
	private Weekdays createNextDayWeekdays()
	{
		boolean[] selection = weekdays.getSelection();
		int days = 7;
		boolean[] result = new boolean[days];
		for (int i = days-1; i >= 0; i--)
		{
			result[i] = selection[i > 0 ? i-1 : days-1];
		}
		return new Weekdays(result);
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

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OpeningWeekdays that = (OpeningWeekdays) o;
		return weekdays.equals(that.weekdays) && timeRanges.equals(that.timeRanges);
	}

	@Override public int hashCode()
	{
		return 31 * weekdays.hashCode() + timeRanges.hashCode();
	}
}

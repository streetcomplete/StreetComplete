package de.westnordost.streetcomplete.quests.collection_times;

import android.annotation.SuppressLint;

import de.westnordost.streetcomplete.quests.opening_hours.CircularSection;

public class TimeRange extends de.westnordost.streetcomplete.quests.opening_hours.CircularSection
{
	public final boolean isOpenEnded;

	public TimeRange(int minutesStart, int minutesEnd, boolean openEnded)
	{
		super(minutesStart, minutesEnd);
		isOpenEnded = openEnded;
	}

	@Override public boolean intersects(CircularSection other)
	{
		if(super.intersects(other)) return true;

		if(isOpenEnded && other.getStart() >= getStart())
			return true;
		if(other instanceof TimeRange)
		{
			if(((TimeRange) other).isOpenEnded && getStart() >= other.getStart())
				return true;
		}
		return false;
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof TimeRange)) return false;
		TimeRange o = (TimeRange) other;
		return o.isOpenEnded == isOpenEnded && super.equals(o);
	}

	@Override public int hashCode()
	{
		return super.hashCode() * 2 + (isOpenEnded ? 1 : 0);
	}

	@Override public String toString()
	{
		return toStringUsing("-");
	}

	public String toStringUsing(String range)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(timeOfDayToString(getStart()));
		int end = getEnd();
		if(end == 0) end = 60*24;
		if(getStart() != getEnd())
		{
			sb.append(range);
			sb.append(timeOfDayToString(end));
		}
		if(isOpenEnded)
		{
			sb.append("+");
		}
		return sb.toString();
	}

	@SuppressLint("DefaultLocale") private static String timeOfDayToString(int minutes)
	{
		return String.format("%02d:%02d", minutes / 60, minutes % 60);
	}
}

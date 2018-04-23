package de.westnordost.streetcomplete.quests.opening_hours.model;

import android.annotation.SuppressLint;

/** A time range from [start,end).
 */
public class TimeRange extends CircularSection
{
	public final boolean isOpenEnded;

	public TimeRange(int minutesStart, int minutesEnd)
	{
		this(minutesStart, minutesEnd, false);
	}

	public TimeRange(int minutesStart, int minutesEnd, boolean openEnded)
	{
		super(minutesStart, minutesEnd);
		isOpenEnded = openEnded;
	}

	@Override public boolean intersects(CircularSection o)
	{
		if(!(o instanceof TimeRange)) return false;
		TimeRange other = (TimeRange) o;

		if(isOpenEnded && other.getStart() >= getStart())
			return true;
		if(other.isOpenEnded && getStart() >= other.getStart())
			return true;
		if(loops() && other.loops())
			return true;
		if(loops() || other.loops())
			return other.getEnd() > getStart() || other.getStart() < getEnd();
		return other.getEnd() > getStart() && other.getStart() < getEnd();
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
		if(getStart() != getEnd() || !isOpenEnded)
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

package de.westnordost.osmagent.dialogs.opening_hours;

import android.support.annotation.NonNull;
// TODO test

// actually closed-open: [start,end+1). ie.e Jun-Jul = 6 to 7 = both June and July
public class CircularSection implements Comparable<CircularSection>
{
	private final int start, end;

	public CircularSection(int start, int end)
	{
		this.start = start;
		this.end = end;
	}

	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}

	public boolean intersects(CircularSection other)
	{
		if(loops() && other.loops())
			return true;
		if(loops() || other.loops())
			return other.end >= start || other.start <= end;
		return other.end >= start && other.start <= end;
	}

	public boolean loops()
	{
		return end < start;
	}

	@Override public int compareTo(@NonNull CircularSection other)
	{
		// loopers come first,
		if(loops() && !other.loops()) return -1;
		if(!loops() && other.loops()) return +1;
		// then by start
		int result = start - other.start;
		if(result != 0) return result;
		// then by end
		return end - other.end;
	}


}

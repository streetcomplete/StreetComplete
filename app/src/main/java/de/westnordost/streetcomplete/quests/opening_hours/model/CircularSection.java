package de.westnordost.streetcomplete.quests.opening_hours.model;

import android.support.annotation.NonNull;

/** An integer range that defines a section in a circle. The range that is defined is actually
   closed-open: [start,end+1). i.e Jun-Jul (= start:6 end:7) shall be both June and July. If start
   is bigger than end, it means that the section crosses the upper boundary. Think degrees.
  */
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

	@Override public boolean equals(Object other)
	{
		if(this == other) return true;
		if(other == null || !(other instanceof CircularSection)) return false;
		CircularSection o = (CircularSection) other;
		return start == o.start && end == o.end;
	}

	public String toStringUsing(String[] names, String range)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(names[start]);
		if(start != end)
		{
			sb.append(range);
			sb.append(names[end]);
		}
		return sb.toString();
	}

	@Override public int hashCode()
	{
		return 31 * start + end;
	}
}

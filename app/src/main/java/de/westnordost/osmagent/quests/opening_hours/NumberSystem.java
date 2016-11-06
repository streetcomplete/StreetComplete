package de.westnordost.osmagent.quests.opening_hours;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** aka number range / Zahlenraum */
public class NumberSystem
{
	private int min, max;

	public NumberSystem(int min, int max)
	{
		if(max < min) throw new IllegalArgumentException("min must be smaller or equal to max");
		this.min = min;
		this.max = max;
	}

	public List<CircularSection> complement(Collection<CircularSection> ranges)
	{
		List<CircularSection> rangeList = canonicalize(ranges);
		List<CircularSection> complementList = new ArrayList<>();
		int start = min;
		for(CircularSection range : rangeList)
		{
			if (range.getStart() > start)
			{
				complementList.add(new CircularSection(start, range.getStart() - 1));
			}
			start = Math.max(start, range.getEnd() + 1);
			if(start > max) break;
		}
		if(start <= max) complementList.add(new CircularSection(start, max));

		if(complementList.size() > 1)
		{
			int lastIndex = complementList.size() - 1;
			CircularSection first = complementList.get(0);
			CircularSection last = complementList.get(lastIndex);
			if(first.getStart() == min && last.getEnd() == max)
			{
				complementList.remove(lastIndex);
				complementList.remove(0);
				complementList.add(mergeAlongBounds(first, last));
			}
		}

		return complementList;
	}

	private CircularSection mergeAlongBounds(CircularSection lowerSection, CircularSection upperSection)
	{
		return new CircularSection(upperSection.getStart(), lowerSection.getEnd());
	}

	private List<CircularSection> splitAlongBounds(CircularSection range)
	{
		ArrayList<CircularSection> result = new ArrayList<>(2);
		CircularSection upperSection = new CircularSection(range.getStart(), max);
		if(!upperSection.loops()) result.add(upperSection);

		CircularSection lowerSections = new CircularSection(min, range.getEnd());
		if(!lowerSections.loops()) result.add(lowerSections);
		return result;
	}

	private List<CircularSection> canonicalize(Collection<CircularSection> ranges)
	{
		// to calculate with circular StartEnds is so complicated, lets dumb it down here
		ArrayList<CircularSection> rangeList = new ArrayList<>();
		for(CircularSection range : ranges)
		{
			if(range.loops())
			{
				rangeList.addAll(splitAlongBounds(range));
			}
			// leave out those which are not in the max range anyway
			else if(min <= range.getEnd() || max >= range.getStart())
			{
				rangeList.add(range);
			}
		}
		Collections.sort(rangeList);
		return rangeList;
	}
}

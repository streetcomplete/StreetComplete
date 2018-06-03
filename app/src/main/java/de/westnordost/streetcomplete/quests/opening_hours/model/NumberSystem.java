package de.westnordost.streetcomplete.quests.opening_hours.model;

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

	public int getSize(CircularSection section)
	{
		int s = Math.max(min,section.getStart());
		int e = Math.min(section.getEnd(), max);
		if(s <= e) return e-s+1;
		return max-s+1 + e-min+1;
	}

	/** @return the complemented of the given ranges */
	public List<CircularSection> complemented(Collection<CircularSection> ranges)
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

		mergeFirstAndLastSection(complementList);

		return complementList;
	}

	public List<CircularSection> merged(List<CircularSection> ranges)
	{
		List<CircularSection> result = new ArrayList<>(ranges);
		Collections.sort(result);
		mergeFirstAndLastSection(result);
		return result;
	}

	private void mergeFirstAndLastSection(List<CircularSection> ranges)
	{
		if(ranges.size() > 1)
		{
			int lastIndex = ranges.size() - 1;
			CircularSection first = ranges.get(0);
			CircularSection last = ranges.get(lastIndex);
			if(first.getStart() == min && last.getEnd() == max)
			{
				ranges.remove(lastIndex);
				ranges.remove(0);
				ranges.add(mergeAlongBounds(first, last));
			}
		}
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

package de.westnordost.streetcomplete.quests.oneway;

import android.support.annotation.NonNull;

import de.westnordost.osmapi.map.data.LatLon;

public class TrafficFlowSegment
{
	@NonNull public final LatLon fromPosition, toPosition;

	public TrafficFlowSegment(@NonNull LatLon fromPosition, @NonNull LatLon toPosition)
	{
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrafficFlowSegment that = (TrafficFlowSegment) o;
		return fromPosition.equals(that.fromPosition) && toPosition.equals(that.toPosition);
	}

	@Override public int hashCode()
	{
		int result = fromPosition.hashCode();
		result = 31 * result + toPosition.hashCode();
		return result;
	}
}

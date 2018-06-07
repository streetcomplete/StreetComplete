package de.westnordost.streetcomplete.quests.oneway;

public class TrafficFlowSegment
{
	public final long fromNodeId, toNodeId;

	public TrafficFlowSegment(long fromNodeId, long toNodeId)
	{
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrafficFlowSegment that = (TrafficFlowSegment) o;
		return fromNodeId == that.fromNodeId && toNodeId == that.toNodeId;
	}

	@Override public int hashCode()
	{
		long hash = 31 * fromNodeId + toNodeId;
		return (int) (hash ^ (hash >>> 32));
	}
}

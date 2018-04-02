package de.westnordost.streetcomplete.quests.oneway;

public class DirectionOfFlow
{
	public final long wayId, fromNodeId, toNodeId;

	public DirectionOfFlow(long wayId, long fromNodeId, long toNodeId)
	{
		this.wayId = wayId;
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
	}
}

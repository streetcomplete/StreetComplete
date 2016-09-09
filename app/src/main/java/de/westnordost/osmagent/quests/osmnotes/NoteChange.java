package de.westnordost.osmagent.quests.osmnotes;

import de.westnordost.osmapi.map.data.LatLon;

public class NoteChange
{
	public enum Action
	{
		OPEN, COMMENT, CLOSE
	}

	public Action action;
	public String text;
	/** only non-null for a newly to be created note */
	public LatLon position;

	public NoteChange(Action action, String text)
	{
		this.action = action;
		this.text = text;
		position = null;
	}

	public NoteChange(LatLon position, String text)
	{
		this.action = Action.OPEN;
		this.text = text;
		this.position = position;
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof NoteChange)) return false;
		if(other == this) return true;
		NoteChange o = (NoteChange) other;
		return	action.equals(o.action) && text.equals(o.text) &&
				(position == null ? o.position == null : position.equals(o.position));
	}
}

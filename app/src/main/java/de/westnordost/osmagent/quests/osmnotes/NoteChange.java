package de.westnordost.osmagent.quests.osmnotes;

public class NoteChange
{
	public enum Action
	{
		OPEN, COMMENT, CLOSE
	}

	public Action action;
	public String text;
}

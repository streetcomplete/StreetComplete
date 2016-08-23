package de.westnordost.osmagent.quests.persist;

public class NoteChange
{
	public enum Action
	{
		OPEN, COMMENT, CLOSE
	}

	public Action action;
	public String text;
}

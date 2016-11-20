package de.westnordost.osmagent.data.osmnotes;

public interface VisibleOsmNoteQuestListener
{
	void onQuestCreated(OsmNoteQuest quest);
	void onNoteQuestRemoved(long questId);
}

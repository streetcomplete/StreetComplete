package de.westnordost.streetcomplete.data.osmnotes;

public interface VisibleOsmNoteQuestListener
{
	void onQuestCreated(OsmNoteQuest quest);
	void onNoteQuestRemoved(long questId);
}

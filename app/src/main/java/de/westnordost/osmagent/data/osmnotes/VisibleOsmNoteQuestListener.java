package de.westnordost.osmagent.data.osmnotes;

import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.notes.Note;

public interface VisibleOsmNoteQuestListener
{
	void onQuestCreated(OsmNoteQuest quest);
	void onQuestRemoved(OsmNoteQuest quest);
}

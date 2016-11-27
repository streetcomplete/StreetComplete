package de.westnordost.streetcomplete.data;

import de.westnordost.streetcomplete.data.osm.VisibleOsmQuestListener;
import de.westnordost.streetcomplete.data.osmnotes.VisibleOsmNoteQuestListener;

public interface VisibleQuestListener extends VisibleOsmNoteQuestListener, VisibleOsmQuestListener
{
	// just those two together
}

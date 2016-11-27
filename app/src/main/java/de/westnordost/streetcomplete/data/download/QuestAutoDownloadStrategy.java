package de.westnordost.streetcomplete.data.download;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

public interface QuestAutoDownloadStrategy
{
	/** returns true if quests should be downloaded automatically at this position now */
	boolean mayDownloadHere(LatLon pos);
	/** returns the bbox that should be downloaded at this position (if mayDownloadHere returned true) */
	BoundingBox getDownloadBoundingBox(LatLon pos);
	/** returns the number of quest types to retrieve in one run */
	int getQuestTypeDownloadCount();
}

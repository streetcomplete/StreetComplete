package de.westnordost.osmagent.quests;

import de.westnordost.osmapi.map.data.LatLon;

/** Represents one task for the user to complete/correct */
public interface Quest
{
	Long getId();

	LatLon getMarkerLocation();

	QuestType getType();

	QuestStatus getStatus();

	void setStatus(QuestStatus status);
}

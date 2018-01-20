package de.westnordost.streetcomplete.data.complete;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;

public abstract class CompleteQuestType implements QuestType
{
	public static final String ANSWER = "answer";

	/** @return the API id for this quest which will be used for the upload */
	public abstract int getApiId();
	/** @return the type of this quest (evaluation or translation) */
	public abstract String getCompleteType();

	public abstract int getIcon();
	public abstract int getTitle();
	public abstract Countries getEnabledForCountries();
	public abstract boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler);

	@Override public int getDefaultDisabledMessage() { return 0; }
}

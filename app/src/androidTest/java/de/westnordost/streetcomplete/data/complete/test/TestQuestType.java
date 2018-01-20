package de.westnordost.streetcomplete.data.complete.test;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.complete.CompleteQuestType;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class TestQuestType extends CompleteQuestType
{
	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return false;
	}
	@Override public AbstractQuestAnswerFragment createForm()
	{
		return null;
	}
	@Override public int getIcon() { return 0; }
	@Override public int getTitle() { return 0; }

	@Override public String getCompleteType() { return null; }

	@Override public int getApiId() { return 0; }

	@Override public Countries getEnabledForCountries() { return Countries.ALL; }
}

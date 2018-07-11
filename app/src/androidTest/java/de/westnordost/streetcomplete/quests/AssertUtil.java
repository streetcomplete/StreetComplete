package de.westnordost.streetcomplete.quests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;


import de.westnordost.osmapi.map.data.BoundingBox;

import static junit.framework.Assert.fail;


public class AssertUtil {
	public static void verifyYieldsNoQuest(OsmElementQuestType quest, BoundingBox bbox) {
		MapDataWithGeometryHandler verifier = (element, geometry) ->
		{
			fail("Expected zero elements. Element returned: " +
				element.getType().name() + "#" + element.getId());
		};
		quest.download(bbox, verifier);
	}

	class ElementCounter implements  MapDataWithGeometryHandler{
		int count = 0;
		@Override
		public void handle(@NonNull Element element, @Nullable ElementGeometry geometry) {
			count += 1;
		}
	}

	public void verifyYieldsQuest(OsmElementQuestType quest, BoundingBox bbox) {
		ElementCounter counter = new ElementCounter();
		quest.download(bbox, counter);
		if(counter.count == 0) {
			fail("Expected nonzero elements. Elements not returned");
		}
	}
}

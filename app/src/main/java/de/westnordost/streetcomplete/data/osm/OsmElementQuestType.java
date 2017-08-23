package de.westnordost.streetcomplete.data.osm;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;

public interface OsmElementQuestType extends QuestType
{
	/** applies the data from answer to the given element */
	void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes);

	/** whether a quest of this quest type could be created out of the given element. If the
	 *  element alone does not suffice to find this out, this should return false */
	boolean appliesTo(Element element);

	/** @return the commit message to be used for this quest type */
	String getCommitMessage();

	/** Downloads map data for this quest type
	 *
	 * @param bbox the area in which it should be downloaded
	 * @param handler called for each element for which this quest type applies
	 * @return true if successful (false if interrupted) */
	boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler);

	/** @return title resource for when the element has the specified tags. The tags are unmodifiable */
	int getTitle(@NonNull Map<String,String> tags);
}

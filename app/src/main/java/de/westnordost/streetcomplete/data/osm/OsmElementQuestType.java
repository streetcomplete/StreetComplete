package de.westnordost.streetcomplete.data.osm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
	 *  element alone does not suffice to find this out, this should return null */
	@Nullable Boolean isApplicableTo(Element element);

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

	// the below could also go up into QuestType interface, but then they should be accounted for
	// in the respective download/upload classes as well

	/** @return for which countries the quest should be shown */
	@NonNull Countries getEnabledForCountries();

	/** The quest type can clean it's metadata here, if any */
	void cleanMetadata();
}

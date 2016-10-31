package de.westnordost.osmagent.data.osm.download;

import java.util.Arrays;
import java.util.List;

import de.westnordost.osmagent.quests.AddOpeningHours;
import de.westnordost.osmagent.quests.AddRoadName;
import de.westnordost.osmagent.data.osm.OverpassQuestType;

/** Should do the same as ReflectionQuestTypeListCreator, only Reflection does not work on Android.
 *  So, this is (hopefully) an intermittent solution
 */

public class OverpassQuestTypeList
{
	public static final List<OverpassQuestType> quests = Arrays.asList(
			new AddRoadName(),
			new AddOpeningHours()
	);
}

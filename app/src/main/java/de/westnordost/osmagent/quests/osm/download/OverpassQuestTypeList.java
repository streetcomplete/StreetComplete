package de.westnordost.osmagent.quests.osm.download;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import de.westnordost.osmagent.quests.osm.types.AddOpeningHours;
import de.westnordost.osmagent.quests.osm.types.AddRoadName;
import de.westnordost.osmagent.quests.osm.types.OverpassQuestType;

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

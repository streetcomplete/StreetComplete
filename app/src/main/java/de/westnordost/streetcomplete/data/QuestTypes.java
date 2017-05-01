package de.westnordost.streetcomplete.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.road_name.AddRoadName;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape;

/** Every osm quest needs to be registered here.
 *
 *  Could theoretically be done with Reflection, but that doesn't really work on Android
 */

public class QuestTypes
{
	public static QuestType[] TYPES = new QuestType[]
	{
		new AddRoadName(),
		new AddOpeningHours(),
		new AddBuildingLevels(),
		new AddRoofShape(),
		// new AddPlaceName(), doesn't make sense as long as the app cannot tell the generic name of elements
		new AddRoadSurface(),
        new AddBusStopShelter(),
        new AddBikeParkingCapacity()
	};

	private final List<QuestType> types;
	private final Map<String, QuestType> typeMap;

	public QuestTypes(QuestType[] questTypes)
	{
		types = Arrays.asList(questTypes);
		Collections.sort(types, new QuestImportanceComparator());

		typeMap = new HashMap<>();
		for (QuestType questType : types)
		{
			String questTypeName = questType.getClass().getSimpleName();
			if(typeMap.containsKey(questTypeName))
			{
				throw new RuntimeException("A quest type's name must be unique! \"" +
						questTypeName + "\" is defined twice!");
			}
			typeMap.put(questTypeName, questType);
		}
	}

	public int getAmount()
	{
		return types.size();
	}

	private static class QuestImportanceComparator implements Comparator<QuestType>
	{
		@Override public int compare(QuestType lhs, QuestType rhs)
		{
			return lhs.importance() - rhs.importance();
		}
	}

	public QuestType forName(String typeName)
	{
		return typeMap.get(typeName);
	}

	public List<QuestType> getQuestTypesSortedByImportance()
	{
		return Collections.unmodifiableList(types);
	}
}

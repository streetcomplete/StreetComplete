package de.westnordost.streetcomplete.quests;

import java.util.Arrays;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypes;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.road_name.AddRoadName;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape;

@Module
public class QuestModule
{
	@Provides @Singleton public static QuestTypes questTypeList(OverpassMapDataDao o)
	{
		QuestType[] questTypes = {
				new AddRoadName(o),
				new AddOpeningHours(o),
				new AddBuildingLevels(o),
				new AddRoofShape(o),
				// new AddPlaceName(), doesn't make sense as long as the app cannot tell the generic name of elements
				new AddRoadSurface(o),
				new AddBusStopShelter(o),
				new AddHousenumber(o),
				new AddBikeParkingCapacity(o)
		};

		return new QuestTypes(Arrays.asList(questTypes));
	}
}

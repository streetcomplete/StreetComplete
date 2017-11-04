package de.westnordost.streetcomplete.quests;

import java.util.Arrays;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable;
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity;
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover;
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter;
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType;
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType;
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType;
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial;
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.road_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk;
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability;
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.road_name.AddRoadName;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape;
import de.westnordost.streetcomplete.quests.sport.AddSport;
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessPublicTransport;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;

@Module
public class QuestModule
{
	@Provides @Singleton public static QuestTypeRegistry questTypeRegistry(
			OsmNoteQuestType osmNoteQuestType, OverpassMapDataDao o,
			RoadNameSuggestionsDao roadNameSuggestionsDao,
			PutRoadNameSuggestionsHandler putRoadNameSuggestionsHandler)
	{
		QuestType[] questTypesOrderedByImportance = {
				// ↓ 1. notes
				osmNoteQuestType,

				// ↓ 2. definitely shown as errors in QA tools

				// ↓ 3. may be shown as missing in QA tools
				new AddRoadName(o, roadNameSuggestionsDao, putRoadNameSuggestionsHandler),

				// ↓ 4. may be shown as possibly missing in QA tools
				new AddHousenumber(o),
				// new AddPlaceName(o), doesn't make sense as long as the app cannot tell the generic name of elements
				new AddRecyclingType(o),
				new AddCycleway(o),
				// ↓ 5. important data that is used by many data consumers
				new AddMaxSpeed(o),
				new AddRoadSurface(o),
				new AddOpeningHours(o),

				// ↓ 6. useful data that is used by some data consumers
				new AddSport(o),
				new AddBikeParkingCapacity(o), // cycle map layer on osm.org
				new AddOrchardProduce(o),
				new AddCrossingType(o),
				new AddBuildingLevels(o),
				new AddBusStopShelter(o), // at least OsmAnd

				// ↓ 7. data useful for only a specific use case
				new AddRoofShape(o),
				new AddWheelChairAccessPublicTransport(o),
				new AddTactilePavingBusStop(o),
				new AddTactilePavingCrosswalk(o),
				new AddWayLit(o),
				new AddWheelchairAccessBusiness(o),
				new AddToiletAvailability(o),

				// ↓ 8. defined in the wiki, but not really used by anyone yet. Just collected for
				//      the sake of mapping it in case it makes sense later
				new AddBikeParkingCover(o),
				new AddToiletsFee(o),
				new AddBabyChangingTable(o),
				new AddFireHydrantType(o),
				new AddParkingType(o),
        		new AddPowerPolesMaterial(o)
		};

		return new QuestTypeRegistry(Arrays.asList(questTypesOrderedByImportance));
	}

	@Provides @Singleton public static OsmNoteQuestType osmNoteQuestType()
	{
		return new OsmNoteQuestType();
	}
}

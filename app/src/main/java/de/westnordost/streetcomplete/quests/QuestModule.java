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
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType;
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway;
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType;
import de.westnordost.streetcomplete.quests.localized_name.AddBusStopName;
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter;
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType;
import de.westnordost.streetcomplete.quests.construction.MarkCompletedBuildingConstruction;
import de.westnordost.streetcomplete.quests.construction.MarkCompletedHighwayConstruction;
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType;
import de.westnordost.streetcomplete.quests.diet_type.AddVegan;
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian;
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType;
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess;
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight;
import de.westnordost.streetcomplete.quests.oneway.AddOneway;
import de.westnordost.streetcomplete.quests.oneway.TrafficFlowSegmentsDao;
import de.westnordost.streetcomplete.quests.oneway.WayTrafficFlowDao;
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess;
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee;
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType;
import de.westnordost.streetcomplete.quests.playground_access.AddPlaygroundAccess;
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes;
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial;
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce;
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrier;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship;
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine;
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao;
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation;
import de.westnordost.streetcomplete.quests.surface.AddPathSurface;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk;
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability;
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.localized_name.AddRoadName;
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape;
import de.westnordost.streetcomplete.quests.sport.AddSport;
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessPublicTransport;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessToilets;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest;

@Module
public class QuestModule
{
	@Provides @Singleton public static QuestTypeRegistry questTypeRegistry(
		OsmNoteQuestType osmNoteQuestType, OverpassMapDataDao o,
		RoadNameSuggestionsDao roadNameSuggestionsDao,
		PutRoadNameSuggestionsHandler putRoadNameSuggestionsHandler,
		TrafficFlowSegmentsDao trafficFlowSegmentsDao, WayTrafficFlowDao trafficFlowDao)
	{
		QuestType[] questTypesOrderedByImportance = {
				// ↓ 1. notes
				osmNoteQuestType,

				// ↓ 2. important data that is used by many data consumers
				new AddRoadName(o, roadNameSuggestionsDao, putRoadNameSuggestionsHandler),
				new AddOneway(o, trafficFlowSegmentsDao, trafficFlowDao),
				new AddHousenumber(o),
				new MarkCompletedHighwayConstruction(o),
				// new AddPlaceName(o), doesn't make sense as long as the app cannot tell the generic name of elements

				// ↓ 3. useful data that is used by some data consumers
				new AddRecyclingType(o),
				new AddRoadSurface(o),
				new AddMaxSpeed(o), // should best be after road surface because it excludes unpaved roads
				new AddMaxHeight(o),
				new AddRailwayCrossingBarrier(o), // useful for routing
				new AddReligionToPlaceOfWorship(o), // icon on maps are different
				new AddPostboxCollectionTimes(o),
				new AddOpeningHours(o),
				new AddSport(o),
				new AddBikeParkingCapacity(o), // cycle map layer on osm.org
				new AddOrchardProduce(o),
				new AddCycleway(o),
				new AddCrossingType(o),
				new AddBuildingLevels(o),
				new AddBusStopShelter(o), // at least OsmAnd
				new AddVegetarian(o),
				new AddVegan(o),
				new AddInternetAccess(o),
				new AddParkingAccess(o),
				new AddParkingFee(o),
				new AddBusStopName(o),
				new AddPathSurface(o),
				new AddBikeParkingType(o), // used by OsmAnd
				new AddPlaygroundAccess(o), //late as in many areas all needed access=private is already mapped

				// ↓ 4. definitely shown as errors in QA tools

				// ↓ 5. may be shown as missing in QA tools
				new AddBuildingType(o), // because housenumber, building levels etc. depend on it

				// ↓ 6. may be shown as possibly missing in QA tools

				// ↓ 7. data useful for only a specific use case
				new AddRoofShape(o),
				new AddWheelChairAccessPublicTransport(o),
				new AddTactilePavingBusStop(o),
				new AddTactilePavingCrosswalk(o),
				new AddWayLit(o),
				new AddWheelchairAccessBusiness(o),
				new AddToiletAvailability(o),
				new AddBridgeStructure(o),
				new AddWheelChairAccessToilets(o),
				new AddReligionToWaysideShrine(o),
				new AddCyclewaySegregation(o),
				new MarkCompletedBuildingConstruction(o),

				// ↓ 8. defined in the wiki, but not really used by anyone yet. Just collected for
				//      the sake of mapping it in case it makes sense later
				new AddBikeParkingCover(o),
				new AddToiletsFee(o),
				new AddBabyChangingTable(o),
				new AddFireHydrantType(o),
				new AddParkingType(o),
				new AddPowerPolesMaterial(o),
				new AddCarWashType(o),
				new AddBenchBackrest(o),
		};

		return new QuestTypeRegistry(Arrays.asList(questTypesOrderedByImportance));
	}

	@Provides @Singleton public static OsmNoteQuestType osmNoteQuestType()
	{
		return new OsmNoteQuestType();
	}
}

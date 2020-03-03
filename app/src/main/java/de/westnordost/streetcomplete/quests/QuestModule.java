package de.westnordost.streetcomplete.quests;

import java.util.Arrays;
import java.util.concurrent.FutureTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmfeatures.FeatureDictionary;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash;
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable;
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity;
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover;
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType;
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway;
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType;
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground;
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians;
import de.westnordost.streetcomplete.quests.general_fee.AddGeneralFee;
import de.westnordost.streetcomplete.quests.handrail.AddHandrail;
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType;
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
import de.westnordost.streetcomplete.quests.max_weight.AddMaxWeight;
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity;
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover;
import de.westnordost.streetcomplete.quests.oneway.AddOneway;
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao;
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao;
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess;
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee;
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType;
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName;
import de.westnordost.streetcomplete.quests.playground_access.AddPlaygroundAccess;
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes;
import de.westnordost.streetcomplete.quests.postbox_ref.AddPostboxRef;
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial;
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce;
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrier;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.recycling_glass.DetermineRecyclingGlass;
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials;
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship;
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine;
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao;
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation;
import de.westnordost.streetcomplete.quests.self_service.AddSelfServiceLaundry;
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk;
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface;
import de.westnordost.streetcomplete.quests.surface.AddFootwayPartSurface;
import de.westnordost.streetcomplete.quests.surface.AddPathSurface;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk;
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability;
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee;
import de.westnordost.streetcomplete.quests.tourism_information.AddInformationToTourism;
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.localized_name.AddRoadName;
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape;
import de.westnordost.streetcomplete.quests.sport.AddSport;
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound;
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton;
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToilets;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessOutside;
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle;
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToiletsPart;

@Module
public class QuestModule
{
	@Provides @Singleton public static QuestTypeRegistry questTypeRegistry(
		OsmNoteQuestType osmNoteQuestType, OverpassMapDataAndGeometryDao o,
		RoadNameSuggestionsDao roadNameSuggestionsDao,
		TrafficFlowSegmentsDao trafficFlowSegmentsDao, WayTrafficFlowDao trafficFlowDao,
		FutureTask<FeatureDictionary> featureDictionaryFuture)
	{
		QuestType<?>[] questTypesOrderedByImportance = {
				// ↓ 1. notes
				osmNoteQuestType,

				// ↓ 2. important data that is used by many data consumers
				new AddRoadName(o, roadNameSuggestionsDao),
				new AddPlaceName(o, featureDictionaryFuture),
				new AddOneway(o, trafficFlowSegmentsDao, trafficFlowDao),
				new AddBusStopName(o),
				new AddIsBuildingUnderground(o), //to avoid asking AddHousenumber and other for underground buildings
				new AddHousenumber(o),
				new MarkCompletedHighwayConstruction(o),
				new AddReligionToPlaceOfWorship(o), // icons on maps are different - OSM Carto, mapy.cz, OsmAnd, Sputnik etc
				new AddParkingAccess(o), //OSM Carto, mapy.cz, OSMand, Sputnik etc

				// ↓ 3. useful data that is used by some data consumers
				new AddRecyclingType(o),
				new AddRecyclingContainerMaterials(o),
				new AddSport(o),
				new AddRoadSurface(o),
				new AddMaxSpeed(o), // should best be after road surface because it excludes unpaved roads
				new AddMaxHeight(o),
				new AddRailwayCrossingBarrier(o), // useful for routing
				new AddPostboxCollectionTimes(o),
				new AddOpeningHours(o, featureDictionaryFuture),
				new AddBikeParkingCapacity(o), // cycle map layer on osm.org
				new AddOrchardProduce(o),
				new AddBuildingType(o), // because housenumber, building levels etc. depend on it
				new AddCycleway(o), // SLOW QUERY
				new AddSidewalk(o), // SLOW QUERY
				new AddProhibitedForPedestrians(o), // uses info from AddSidewalk quest, should be after it
				new AddCrossingType(o),
				new AddBuildingLevels(o),
				new AddBusStopShelter(o), // at least OsmAnd
				new AddVegetarian(o),
				new AddVegan(o),
				new AddInternetAccess(o),
				new AddParkingFee(o),
				new AddMotorcycleParkingCapacity(o),
				new AddPathSurface(o),
				new AddTracktype(o),
				new AddMaxWeight(o),
				new AddForestLeafType(o), // used by OSM Carto
				new AddBikeParkingType(o), // used by OsmAnd
				new AddWheelchairAccessToilets(o), // used by wheelmap, OsmAnd, MAPS.ME
				new AddPlaygroundAccess(o), //late as in many areas all needed access=private is already mapped
				new AddWheelchairAccessBusiness(o), // used by wheelmap, OsmAnd, MAPS.ME
				new AddToiletAvailability(o), //OSM Carto, shown in OsmAnd descriptions
				new AddFerryAccessPedestrian(o),
				new AddFerryAccessMotorVehicle(o),
				new AddAcceptsCash(o),

				// ↓ 4. definitely shown as errors in QA tools

				// ↓ 5. may be shown as missing in QA tools
				new DetermineRecyclingGlass(o), // because most recycling:glass=yes is a tagging mistake

				// ↓ 6. may be shown as possibly missing in QA tools

				// ↓ 7. data useful for only a specific use case
				new AddWayLit(o), //  used by OsmAnd if "Street lighting" is enabled. (Configure map, Map rendering, Details)
				new AddToiletsFee(o), // used by OsmAnd in the object description
				new AddBabyChangingTable(o), // used by OsmAnd in the object description
				new AddBikeParkingCover(o), // used by OsmAnd in the object description
				new AddTactilePavingCrosswalk(o), // Paving can be completed while waiting to cross
				new AddTrafficSignalsSound(o), // Sound needs to be done as or after you're crossing
				new AddRoofShape(o),
				new AddWheelchairAccessPublicTransport(o),
				new AddWheelchairAccessOutside(o),
				new AddTactilePavingBusStop(o),
				new AddBridgeStructure(o),
				new AddReligionToWaysideShrine(o),
				new AddCyclewaySegregation(o),
				new MarkCompletedBuildingConstruction(o),
				new AddGeneralFee(o),
				new AddSelfServiceLaundry(o),
				new AddHandrail(o), // for accessibility of pedestrian routing
				new AddInformationToTourism(o),

				// ↓ 8. defined in the wiki, but not really used by anyone yet. Just collected for
				//      the sake of mapping it in case it makes sense later
				new AddCyclewayPartSurface(o),
				new AddFootwayPartSurface(o),
				new AddMotorcycleParkingCover(o),
				new AddFireHydrantType(o),
				new AddParkingType(o),
				new AddPostboxRef(o),
				new AddWheelchairAccessToiletsPart(o),
				new AddPowerPolesMaterial(o),
				new AddCarWashType(o),
				new AddBenchBackrest(o),
				new AddTrafficSignalsButton(o),
		};

		return new QuestTypeRegistry(Arrays.asList(questTypesOrderedByImportance));
	}

	@Provides @Singleton public static OsmNoteQuestType osmNoteQuestType()
	{
		return new OsmNoteQuestType();
	}
/*
	@Provides @Singleton public static QuestController questController(
		OsmQuestDao osmQuestDao, UndoOsmQuestDao undoOsmQuestDao, MergedElementDao mergedElementDao,
		ElementGeometryDao elementGeometryDao, OsmNoteQuestDao osmNoteQuestDao,
		OsmQuestSplitWayDao osmQuestSplitWayDao, CreateNoteDao createNoteDao,
		SharedPreferences prefs, OrderedVisibleQuestTypesProvider questTypesProvider,
		Context context
	)
	{
		return new QuestController(osmQuestDao, undoOsmQuestDao, mergedElementDao,
			elementGeometryDao, osmNoteQuestDao, osmQuestSplitWayDao, createNoteDao, prefs,
			questTypesProvider, context);
	}
	*/
}

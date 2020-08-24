package de.westnordost.streetcomplete.quests

import dagger.Module
import dagger.Provides
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.address.AddAddressStreet
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway
import de.westnordost.streetcomplete.quests.board_type.AddBoardType
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType
import de.westnordost.streetcomplete.quests.construction.MarkCompletedBuildingConstruction
import de.westnordost.streetcomplete.quests.construction.MarkCompletedHighwayConstruction
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.general_fee.AddGeneralFee
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType
import de.westnordost.streetcomplete.quests.localized_name.AddBusStopName
import de.westnordost.streetcomplete.quests.localized_name.AddRoadName
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed
import de.westnordost.streetcomplete.quests.max_weight.AddMaxWeight
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.playground_access.AddPlaygroundAccess
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes
import de.westnordost.streetcomplete.quests.postbox_ref.AddPostboxRef
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrier
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.quests.recycling_glass.DetermineRecyclingGlass
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation
import de.westnordost.streetcomplete.quests.self_service.AddSelfServiceLaundry
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.sport.AddSport
import de.westnordost.streetcomplete.quests.surface.*
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.tourism_information.AddInformationToTourism
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.*
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore
import java.util.concurrent.FutureTask
import javax.inject.Singleton


@Module
object QuestModule
{
    @Provides @Singleton fun questTypeRegistry(
        osmNoteQuestType: OsmNoteQuestType,
        o: OverpassMapDataAndGeometryApi,
        r: ResurveyIntervalsStore,
        roadNameSuggestionsDao: RoadNameSuggestionsDao,
        trafficFlowSegmentsApi: TrafficFlowSegmentsApi, trafficFlowDao: WayTrafficFlowDao,
        featureDictionaryFuture: FutureTask<FeatureDictionary>
    ): QuestTypeRegistry = QuestTypeRegistry(listOf(

        // ↓ 1. notes
        osmNoteQuestType,

        // ↓ 2. important data that is used by many data consumers
        AddRoadName(o, roadNameSuggestionsDao),
        AddPlaceName(o, featureDictionaryFuture),
        AddOneway(o, trafficFlowSegmentsApi, trafficFlowDao),
        AddBusStopName(o),
        AddIsBuildingUnderground(o), //to avoid asking AddHousenumber and other for underground buildings
        AddHousenumber(o),
        AddAddressStreet(o, roadNameSuggestionsDao),
        MarkCompletedHighwayConstruction(o, r),
        AddReligionToPlaceOfWorship(o), // icons on maps are different - OSM Carto, mapy.cz, OsmAnd, Sputnik etc
        AddParkingAccess(o), //OSM Carto, mapy.cz, OSMand, Sputnik etc

        // ↓ 3. useful data that is used by some data consumers
        AddRecyclingType(o),
        AddRecyclingContainerMaterials(o, r),
        AddSport(o),
        AddRoadSurface(o, r), // used by BRouter, OsmAnd, OSRM, graphhopper, HOT map style
        AddMaxSpeed(o), // should best be after road surface because it excludes unpaved roads
        AddMaxHeight(o), // OSRM and other routing engines
        AddRailwayCrossingBarrier(o, r), // useful for routing
        AddPostboxCollectionTimes(o, r),
        AddOpeningHours(o, featureDictionaryFuture, r),
        DetailRoadSurface(o), // used by BRouter, OsmAnd, OSRM, graphhopper
        AddBikeParkingCapacity(o, r), // used by cycle map layer on osm.org, OsmAnd
        AddOrchardProduce(o),
        AddBuildingType(o), // because housenumber, building levels etc. depend on it
        AddCycleway(o), // SLOW QUERY
        AddSidewalk(o), // SLOW QUERY
        AddProhibitedForPedestrians(o), // uses info from AddSidewalk quest, should be after it
        AddCrossingType(o, r),
        AddBuildingLevels(o),
        AddBusStopShelter(o, r), // at least OsmAnd
        AddVegetarian(o, r),
        AddVegan(o, r),
        AddInternetAccess(o, r), // used by OsmAnd
        AddParkingFee(o, r), // used by OsmAnd
        AddMotorcycleParkingCapacity(o, r),
        AddPathSurface(o, r), // used by OSM Carto, OsmAnd
        AddTracktype(o, r), // widely used in map rendering - OSM Carto, OsmAnd...
        AddMaxWeight(o), // used by OSRM and other routing engines
        AddForestLeafType(o), // used by OSM Carto
        AddBikeParkingType(o), // used by OsmAnd
        AddWheelchairAccessToilets(o, r), // used by wheelmap, OsmAnd, MAPS.ME
        AddPlaygroundAccess(o), //late as in many areas all needed access=private is already mapped
        AddWheelchairAccessBusiness(o), // used by wheelmap, OsmAnd, MAPS.ME
        AddToiletAvailability(o), //OSM Carto, shown in OsmAnd descriptions
        AddFerryAccessPedestrian(o),
        AddFerryAccessMotorVehicle(o),
        AddAcceptsCash(o),

        // ↓ 4. definitely shown as errors in QA tools

        // ↓ 5. may be shown as missing in QA tools
        DetermineRecyclingGlass(o), // because most recycling:glass=yes is a tagging mistake

        // ↓ 6. may be shown as possibly missing in QA tools

        // ↓ 7. data useful for only a specific use case
        AddWayLit(o, r), //  used by OsmAnd if "Street lighting" is enabled. (Configure map, Map rendering, Details)
        AddToiletsFee(o), // used by OsmAnd in the object description
        AddBabyChangingTable(o), // used by OsmAnd in the object description
        AddBikeParkingCover(o), // used by OsmAnd in the object description
        AddTactilePavingCrosswalk(o, r), // Paving can be completed while waiting to cross
        AddTrafficSignalsSound(o, r), // Sound needs to be done as or after you're crossing
        AddRoofShape(o),
        AddWheelchairAccessPublicTransport(o, r),
        AddWheelchairAccessOutside(o, r),
        AddTactilePavingBusStop(o, r),
        AddBridgeStructure(o),
        AddReligionToWaysideShrine(o),
        AddCyclewaySegregation(o, r),
        MarkCompletedBuildingConstruction(o, r),
        AddGeneralFee(o),
        AddSelfServiceLaundry(o),
        AddHandrail(o, r), // for accessibility of pedestrian routing
        AddInformationToTourism(o),

        // ↓ 8. defined in the wiki, but not really used by anyone yet. Just collected for
        //      the sake of mapping it in case it makes sense later
        AddCyclewayPartSurface(o, r),
        AddFootwayPartSurface(o, r),
        AddMotorcycleParkingCover(o),
        AddFireHydrantType(o),
        AddParkingType(o),
        AddPostboxRef(o),
        AddWheelchairAccessToiletsPart(o, r),
        AddBoardType(o),
        AddPowerPolesMaterial(o),
        AddCarWashType(o),
        AddBenchBackrest(o),
        AddTrafficSignalsButton(o)
    ))

    @Provides @Singleton fun osmNoteQuestType(): OsmNoteQuestType = OsmNoteQuestType()
}

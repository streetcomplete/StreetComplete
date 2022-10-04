package de.westnordost.streetcomplete.quests

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.quests.accepts_cards.AddAcceptsCards
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.access_point_ref.AddAccessPointRef
import de.westnordost.streetcomplete.quests.address.AddAddressStreet
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.air_conditioning.AddAirConditioning
import de.westnordost.streetcomplete.quests.air_pump.AddAirCompressor
import de.westnordost.streetcomplete.quests.air_pump.AddBicyclePump
import de.westnordost.streetcomplete.quests.atm_cashin.AddAtmCashIn
import de.westnordost.streetcomplete.quests.atm_operator.AddAtmOperator
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.AddBicycleBarrierType
import de.westnordost.streetcomplete.quests.barrier_type.AddBarrierOnPath
import de.westnordost.streetcomplete.quests.barrier_type.AddBarrierOnRoad
import de.westnordost.streetcomplete.quests.barrier_type.AddBarrierType
import de.westnordost.streetcomplete.quests.barrier_type.AddStileType
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.bike_rental_capacity.AddBikeRentalCapacity
import de.westnordost.streetcomplete.quests.bike_rental_type.AddBikeRentalType
import de.westnordost.streetcomplete.quests.bike_shop.AddBikeRepairAvailability
import de.westnordost.streetcomplete.quests.bike_shop.AddSecondHandBicycleAvailability
import de.westnordost.streetcomplete.quests.board_type.AddBoardType
import de.westnordost.streetcomplete.quests.bollard_type.AddBollardType
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure
import de.westnordost.streetcomplete.quests.building_entrance.AddEntrance
import de.westnordost.streetcomplete.quests.building_entrance_reference.AddEntranceReference
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground
import de.westnordost.streetcomplete.quests.bus_stop_bench.AddBenchStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_bin.AddBinStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_lit.AddBusStopLit
import de.westnordost.streetcomplete.quests.bus_stop_name.AddBusStopName
import de.westnordost.streetcomplete.quests.bus_stop_ref.AddBusStopRef
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.camera_type.AddCameraType
import de.westnordost.streetcomplete.quests.camping.AddCampDrinkingWater
import de.westnordost.streetcomplete.quests.camping.AddCampPower
import de.westnordost.streetcomplete.quests.camping.AddCampShower
import de.westnordost.streetcomplete.quests.camping.AddCampType
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType
import de.westnordost.streetcomplete.quests.charging_station_capacity.AddChargingStationCapacity
import de.westnordost.streetcomplete.quests.charging_station_operator.AddChargingStationOperator
import de.westnordost.streetcomplete.quests.clothing_bin_operator.AddClothingBinOperator
import de.westnordost.streetcomplete.quests.construction.MarkCompletedBuildingConstruction
import de.westnordost.streetcomplete.quests.construction.MarkCompletedHighwayConstruction
import de.westnordost.streetcomplete.quests.crossing.AddCrossing
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway
import de.westnordost.streetcomplete.quests.defibrillator.AddIsDefibrillatorIndoor
import de.westnordost.streetcomplete.quests.diet_type.AddHalal
import de.westnordost.streetcomplete.quests.diet_type.AddKosher
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.drinking_water.AddDrinkingWater
import de.westnordost.streetcomplete.quests.drinking_water_type.AddDrinkingWaterType
import de.westnordost.streetcomplete.quests.existence.CheckExistence
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.AddFireHydrantDiameter
import de.westnordost.streetcomplete.quests.fire_hydrant_position.AddFireHydrantPosition
import de.westnordost.streetcomplete.quests.fire_hydrant_ref.AddFireHydrantRef
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.fuel_service.AddFuelSelfService
import de.westnordost.streetcomplete.quests.general_fee.AddGeneralFee
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.incline_direction.AddBicycleIncline
import de.westnordost.streetcomplete.quests.incline_direction.AddStepsIncline
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess
import de.westnordost.streetcomplete.quests.kerb_height.AddKerbHeight
import de.westnordost.streetcomplete.quests.lanes.AddLanes
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType
import de.westnordost.streetcomplete.quests.level.AddLevel
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.max_height.AddMaxPhysicalHeight
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed
import de.westnordost.streetcomplete.quests.max_weight.AddMaxWeight
import de.westnordost.streetcomplete.quests.memorial_type.AddMemorialType
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.oneway_suspects.AddSuspectedOneway
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.opening_hours_signed.CheckOpeningHoursSigned
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce
import de.westnordost.streetcomplete.quests.parking_access.AddBikeParkingAccess
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess
import de.westnordost.streetcomplete.quests.parking_fee.AddBikeParkingFee
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType
import de.westnordost.streetcomplete.quests.picnic_table_cover.AddPicnicTableCover
import de.westnordost.streetcomplete.quests.pitch_lit.AddPitchLit
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.playground_access.AddPlaygroundAccess
import de.westnordost.streetcomplete.quests.police_type.AddPoliceType
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes
import de.westnordost.streetcomplete.quests.postbox_ref.AddPostboxRef
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.AddPostboxRoyalCypher
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrier
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.quests.recycling_glass.DetermineRecyclingGlass
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine
import de.westnordost.streetcomplete.quests.road_name.AddRoadName
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape
import de.westnordost.streetcomplete.quests.seating.AddSeating
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation
import de.westnordost.streetcomplete.quests.self_service.AddSelfServiceLaundry
import de.westnordost.streetcomplete.quests.shop_type.CheckShopType
import de.westnordost.streetcomplete.quests.shop_type.SpecifyShopType
import de.westnordost.streetcomplete.quests.shoulder.AddShoulder
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.smoking.AddSmoking
import de.westnordost.streetcomplete.quests.smoothness.AddPathSmoothness
import de.westnordost.streetcomplete.quests.smoothness.AddRoadSmoothness
import de.westnordost.streetcomplete.quests.sport.AddSport
import de.westnordost.streetcomplete.quests.step_count.AddStepCount
import de.westnordost.streetcomplete.quests.step_count.AddStepCountStile
import de.westnordost.streetcomplete.quests.steps_ramp.AddStepsRamp
import de.westnordost.streetcomplete.quests.summit.AddSummitCross
import de.westnordost.streetcomplete.quests.summit.AddSummitRegister
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddFootwayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddPitchSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.surface.AddSidewalkSurface
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingKerb
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.tourism_information.AddInformationToTourism
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype
import de.westnordost.streetcomplete.quests.traffic_calming_type.AddTrafficCalmingType
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound
import de.westnordost.streetcomplete.quests.traffic_signals_vibrate.AddTrafficSignalsVibration
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessOutside
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToilets
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToiletsPart
import de.westnordost.streetcomplete.quests.width.AddCyclewayWidth
import de.westnordost.streetcomplete.quests.width.AddRoadWidth
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.FutureTask

val questsModule = module {
    factory { RoadNameSuggestionsSource(get()) }
    factory { WayTrafficFlowDao(get()) }

    single { questTypeRegistry(
        get(),
        get(),
        get(named("FeatureDictionaryFuture")),
        get(),
        get(named("CountryBoundariesFuture")),
        get(),
    ) }
}

fun questTypeRegistry(
    trafficFlowSegmentsApi: TrafficFlowSegmentsApi,
    trafficFlowDao: WayTrafficFlowDao,
    featureDictionaryFuture: FutureTask<FeatureDictionary>,
    countryInfos: CountryInfos,
    countryBoundariesFuture: FutureTask<CountryBoundaries>,
    arSupportChecker: ArSupportChecker
) = QuestTypeRegistry(listOf(

    /* The quest types are primarily sorted by how easy they can be solved:
    1. quests that are solvable from a distance or while passing by (fast)
    2. quests that require to be right in front of it (e.g. because it is small, you need to
      look for it or read text)
    3. quests that require some exploration or walking around to check (e.g. walking down the
      whole road to find the cycleway is the same along the whole way)
    4. quests that require to go inside, i.e. deviate from your walking route by a lot just
      to solve the quest
    5. quests that come in heaps (are spammy) come last: e.g. building type etc.

    The ordering within this primary sort order shall be whatever is faster so solve first:

    a. Yes/No quests, easy selections first,
    b. number and text inputs later,
    c. complex inputs (opening hours, ...) last. Quests that e.g. often require the way to be
      split up first are in effect also slow to answer

    The order can be watered down somewhat if it means that quests that usually apply to the
    same elements are in direct succession because we want to avoid that users are half-done
    answering all the quests for one element and then can't solve the last anymore because it
    is visually obscured by another quest.

    Finally, importance of the quest can still play a factor, but only secondarily.
    */

    /* always first: notes - they mark a mistake in the data so potentially every quest for that
    element is based on wrong data while the note is not resolved */
    OsmNoteQuestType,

    /* ↓ 1. solvable from a distance or while passing by -----------------------------------  */

    // bus stop quests
    AddBusStopShelter(), // used by at least OsmAnd
    AddBenchStatusOnBusStop(), // can be seen from across the street
    AddBinStatusOnBusStop(), // can be seen from across the street
    AddTactilePavingBusStop(), // requires you to be very close to it
    AddBusStopName(), // requires text input
    AddBusStopRef(), // requires text input
    AddBusStopLit(), // at least during day requires to stand in it to see if there is a light in the shelter

    AddRailwayCrossingBarrier(), // useful for routing

    AddCarWashType(),

    AddBenchBackrest(),
    AddPicnicTableCover(),

    AddBridgeStructure(),

    MarkCompletedBuildingConstruction(), // unlocks AddBuildingType which unlocks address and building detail quests

    // sport pitches
    AddSport(),
    AddPitchSurface(),
    AddPitchLit(),

    // parking
    AddParkingType(),
    AddParkingAccess(), // used by OSM Carto, mapy.cz, OSMand, Sputnik etc
    AddParkingFee(), // used by OsmAnd

    AddTrafficCalmingType(),

    // steps
    AddHandrail(), // for accessibility of pedestrian routing, can be gathered when walking past
    AddStepsRamp(),
    AddStepsIncline(), // can be gathered while walking perpendicular to the way e.g. the other side of the road or when running/cycling past, confuses some people, so not as high as it theoretically should be

    AddBicycleIncline(),

    AddMemorialType(), // sometimes a bit hard to decide between the different types (something something sculpture)

    AddReligionToPlaceOfWorship(), // icons on maps are different - OSM Carto, mapy.cz, OsmAnd, Sputnik etc
    AddReligionToWaysideShrine(),

    AddPowerPolesMaterial(),

    AddIsBuildingUnderground(), // should be before AddHousenumber to avoid asking for underground buildings

    // motorcycle parking
    AddMotorcycleParkingCover(),
    AddMotorcycleParkingCapacity(), // counting + number input required but usually well visible

    // air pump, may require some checking within a garage forecourt
    AddAirCompressor(),

    // recycling containers
    AddRecyclingType(),
    DetermineRecyclingGlass(), // because most recycling:glass=yes is a tagging mistake
    AddRecyclingContainerMaterials(),

    // kerbs
    AddKerbHeight(), /* deliberately before AddTactilePavingKerb:
            * - Also should be visible while waiting to cross
            * - Some people are not interpreting flush or lowered kerb as a kerb on their own,
            * and would be confused about asking about tactile status on kerb without kerb
            * but with this quest first they are OK with such interpretation
            */
    AddTactilePavingKerb(), // Paving can be completed while waiting to cross

    // crossing quests: A little later because they are not all solvable from a distance
    AddCrossing(),
    AddCrossingIsland(), // can be done at a glance
    AddCrossingType(),
    AddTactilePavingCrosswalk(),
    AddTrafficSignalsSound(), // Sound needs to be done as or after you're crossing
    AddTrafficSignalsButton(),
    AddTrafficSignalsVibration(),

    /* ↓ 2.solvable when right in front of it ----------------------------------------------- */
    AddInformationToTourism(), // OSM Carto

    AddPoliceType(),

    AddPlaygroundAccess(),

    /* pulled up in priority to be before CheckExistence because this is basically the check
       whether the postbox is still there in countries in which it is enabled */
    AddPostboxCollectionTimes(),
    CheckExistence(featureDictionaryFuture),

    AddBoardType(),

    AddBarrierType(), // basically any more detailed rendering and routing: OSM Carto, mapy.cz, OSMand for start
    AddBarrierOnPath(),
    AddBarrierOnRoad(),
    AddBicycleBarrierType(),
    AddStileType(),
    AddStepCountStile(), // here to keep stile quest together - this quest will appear in low quest density anyway

    AddBollardType(), // useful for first responders

    AddSelfServiceLaundry(),

    AddGeneralFee(),

    AddDrinkingWater(), // used by AnyFinder
    AddDrinkingWaterType(),

    AddCameraType(),

    AddFireHydrantType(),
    AddFireHydrantPosition(),
    AddFireHydrantDiameter(),
    AddFireHydrantRef(),

    /* ↓ 2.solvable when right in front of it but takes longer to input --------------------- */

    // bike parking/rental: would be higher up if not for bike parking/rental capacity which is usually not solvable when moving past
    AddBikeParkingCover(), // used by OsmAnd in the object description
    AddBikeRentalType(), // generally less overlap of possible types/fewer choices/simpler to answer
    AddBikeParkingType(), // used by OsmAnd
    AddBikeParkingAccess(),
    AddBikeParkingFee(),
    AddBikeRentalCapacity(), // less ambiguous than bike parking
    AddBikeParkingCapacity(), // used by cycle map layer on osm.org, OsmAnd

    // address: usually only visible when just in front + sometimes requires to take "other answer"
    AddHousenumber(),
    AddAddressStreet(),

    // shops: text input / opening hours input take longer than other quests
    CheckOpeningHoursSigned(featureDictionaryFuture),
    SpecifyShopType(), // above add place name as some brand presets will set the name too
    CheckShopType(),
    AddPlaceName(featureDictionaryFuture),
    AddOpeningHours(featureDictionaryFuture),
    AddSeating(), // easily visible from outside, but only seasonally
    AddBicyclePump(), // visible from the outside, but only during opening hours

    AddAtmOperator(),
    AddAtmCashIn(),

    AddClothingBinOperator(),

    AddChargingStationCapacity(),
    AddChargingStationOperator(),

    // postboxes (collection times are further up, see comment)
    AddPostboxRoyalCypher(), // can be glanced across the road (if postbox facing the right way)
    AddPostboxRef(), // requires text input and to be very close to the collection plate

    AddAccessPointRef(), // requires text input and to be very close to the collection plate

    AddWheelchairAccessOutside(),

    // road but information is visible usually at the beginning of the marked stretch of way
    AddMaxWeight(), // used by OSRM and other routing engines
    AddMaxHeight(), // OSRM and other routing engines
    AddMaxPhysicalHeight(arSupportChecker), // same as above, best if it appears right after (if enabled)
    AddRoadName(),
    AddOneway(),
    AddSuspectedOneway(trafficFlowSegmentsApi, trafficFlowDao),

    AddEntrance(),
    AddEntranceReference(),

    /* ↓ 3.quests that may need some exploration / walking around --------------------------- */

    // ferry: usually visible from looking at the boat, but not always...
    AddFerryAccessPedestrian(),
    AddFerryAccessMotorVehicle(),

    AddProhibitedForPedestrians(), // need to understand the pedestrian situation

    MarkCompletedHighwayConstruction(), // need to look the whole way

    AddSummitCross(), // summit markings are not necessarily directly at the peak, need to look around
    AddSummitRegister(), // register is harder to find than cross

    AddForestLeafType(), // need to walk around in the highlighted section

    AddOrchardProduce(), // difficult to find out if the orchard does not carry fruits right now

    AddLevel(), // requires to search for the place on several levels (or at least find a mall map)

    AddAirConditioning(), // often visible from the outside across the street, if not, visible/feelable inside

    AddSmoking(), // often marked on the entrance, if not, visible/smellable inside

    /* ↓ 4.quests that may need to go inside ------------------------------------------------ */

    AddWheelchairAccessPublicTransport(), // need to look out for lifts etc, maybe even enter the station

    AddIsDefibrillatorIndoor(), // need to go inside in case it is inside (or gone)

    // inside camping sites
    AddCampType(),
    AddCampDrinkingWater(),
    AddCampShower(),
    AddCampPower(),

    // toilets
    AddToiletAvailability(), // OSM Carto, shown in OsmAnd descriptions
    AddToiletsFee(), // used by OsmAnd in the object description
    AddBabyChangingTable(), // used by OsmAnd in the object description
    AddWheelchairAccessToiletsPart(),
    AddWheelchairAccessToilets(), // used by wheelmap, OsmAnd, Organic Maps

    // shop
    AddBikeRepairAvailability(),
    AddSecondHandBicycleAvailability(),
    AddVegetarian(), // menus are often posted externally
    AddVegan(),
    AddHalal(), // there are ~ 100 times more Muslims than Jews
    AddKosher(),
    AddWheelchairAccessBusiness(), // used by wheelmap, OsmAnd, Organic Maps
    AddInternetAccess(), // used by OsmAnd
    AddAcceptsCards(), // this will often involve going inside and near the till
    AddAcceptsCash(),

    AddFuelSelfService(),

    /* ↓ 5.quests that are very numerous ---------------------------------------------------- */

    // roads
    AddSidewalk(), // for any pedestrian routers, needs minimal thinking
    AddRoadSurface(), // used by BRouter, OsmAnd, OSRM, graphhopper, HOT map style... - sometimes requires way to be split
    AddTracktype(), // widely used in map rendering - OSM Carto, OsmAnd...
    AddCycleway(countryInfos, countryBoundariesFuture), // for any cyclist routers (and cyclist maps)
    AddLanes(), // abstreet, certainly most routing engines - often requires way to be split
    AddShoulder(), // needs minimal thinking
    AddRoadWidth(arSupportChecker),
    AddRoadSmoothness(),
    AddPathSmoothness(),

    // footways
    AddPathSurface(), // used by OSM Carto, BRouter, OsmAnd, OSRM, graphhopper...
    AddCyclewaySegregation(), // Cyclosm, Valhalla, Bike Citizens Bicycle Navigation...
    AddFootwayPartSurface(),
    AddCyclewayPartSurface(),
    AddSidewalkSurface(),
    AddCyclewayWidth(arSupportChecker), // should be after cycleway segregation

    /* should best be after road surface because it excludes unpaved roads, also, need to search
    *  for the sign which is one reason why it is disabled by default */
    AddMaxSpeed(),

    // buildings
    AddBuildingType(),
    AddBuildingLevels(),
    AddRoofShape(countryInfos, countryBoundariesFuture),

    AddStepCount(), // can only be gathered when walking along this way, also needs the most effort and least useful

    /* at the very last because it can be difficult to ascertain during day. used by OsmAnd if "Street lighting" is enabled. (Configure map, Map rendering, Details) */
    AddWayLit(),
))

package de.westnordost.streetcomplete.data.user.achievements

import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.address.AddAddressStreet
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.atm_operator.AddAtmOperator
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.barrier_type.AddBarrierType
import de.westnordost.streetcomplete.quests.barrier_type.AddStileType
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.board_type.AddBoardType
import de.westnordost.streetcomplete.quests.bollard_type.AddBollardType
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground
import de.westnordost.streetcomplete.quests.bus_stop_bench.AddBenchStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_bin.AddBinStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_lit.AddBusStopLit
import de.westnordost.streetcomplete.quests.bus_stop_name.AddBusStopName
import de.westnordost.streetcomplete.quests.bus_stop_ref.AddBusStopRef
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
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
import de.westnordost.streetcomplete.quests.diet_type.AddKosher
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.drinking_water.AddDrinkingWater
import de.westnordost.streetcomplete.quests.existence.CheckExistence
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.general_fee.AddGeneralFee
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess
import de.westnordost.streetcomplete.quests.kerb_height.AddKerbHeight
import de.westnordost.streetcomplete.quests.lanes.AddLanes
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed
import de.westnordost.streetcomplete.quests.max_weight.AddMaxWeight
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.parking_access.AddBikeParkingAccess
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess
import de.westnordost.streetcomplete.quests.parking_fee.AddBikeParkingFee
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType
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
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation
import de.westnordost.streetcomplete.quests.self_service.AddSelfServiceLaundry
import de.westnordost.streetcomplete.quests.shop_type.CheckShopType
import de.westnordost.streetcomplete.quests.shop_type.SpecifyShopType
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.sport.AddSport
import de.westnordost.streetcomplete.quests.step_count.AddStepCount
import de.westnordost.streetcomplete.quests.steps_incline.AddStepsIncline
import de.westnordost.streetcomplete.quests.steps_ramp.AddStepsRamp
import de.westnordost.streetcomplete.quests.summit_register.AddSummitRegister
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddFootwayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddPitchSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingKerb
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.tourism_information.AddInformationToTourism
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound
import de.westnordost.streetcomplete.quests.traffic_signals_vibrate.AddTrafficSignalsVibration
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessOutside
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToilets
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToiletsPart
import javax.inject.Named

@Module
object AchievementsModule {

    @Provides @Named("Links") fun links(): List<Link> = links
    @Provides @Named("Achievements") fun achievements(): List<Achievement> = achievements
    @Provides @Named("QuestAliases") fun questAliases(): List<Pair<String, String>> = questAliases

    // list of quest synonyms (this alternate name is mentioned to aid searching for this code)
    private val questAliases = listOf(
        "AddAccessibleForPedestrians"        to AddProhibitedForPedestrians::class.simpleName!!,
        "AddWheelChairAccessPublicTransport" to AddWheelchairAccessPublicTransport::class.simpleName!!,
        "AddWheelChairAccessToilets"         to AddWheelchairAccessToilets::class.simpleName!!,
        "AddSidewalks"                       to AddSidewalk::class.simpleName!!,
        "DetailRoadSurface"                  to AddRoadSurface::class.simpleName!!,
        "AddTrafficSignalsBlindFeatures"     to AddTrafficSignalsVibration::class.simpleName!!,
        "AddSuspectedOneway"                 to AddOneway::class.simpleName!!,
    )

    private val links = listOf(

        /* ---------------------------------------- Intro ----------------------------------------*/
        Link(
            "wiki",
            "https://wiki.openstreetmap.org",
            "OpenStreetMap Wiki",
            LinkCategory.INTRO,
            R.drawable.ic_link_wiki,
            R.string.link_wiki_description
        ),
        Link(
            "welcomemat",
            "https://welcome.openstreetmap.org",
            "Welcome Mat",
            LinkCategory.INTRO,
            R.drawable.ic_link_welcome_mat,
            R.string.link_welcome_mat_description
        ),
        Link(
            "learnosm",
            "https://learnosm.org/en/beginner/",
            "learnOSM",
            LinkCategory.INTRO,
            R.drawable.ic_link_learnosm,
            R.string.link_learnosm_description
        ),
        Link(
            "weeklyosm",
            "https://weeklyosm.eu/",
            "weeklyOSM",
            LinkCategory.INTRO,
            R.drawable.ic_link_weeklyosm,
            R.string.link_weeklyosm_description
        ),
        Link(
            "neis-one",
            "https://resultmaps.neis-one.org/",
            "ResultMaps",
            LinkCategory.INTRO,
            R.drawable.ic_link_neis_one,
            R.string.link_neis_one_description
        ),
        Link(
            "disaster.ninja",
            "https://disaster.ninja/live/#position=11,46;zoom=3",
            "disaster.ninja",
            LinkCategory.INTRO,
            R.drawable.ic_link_kontur,
            R.string.link_disaster_ninja_description
        ),

        Link(
            "mapillary",
            "https://www.mapillary.com",
            "Mapillary",
            LinkCategory.INTRO,
            R.drawable.ic_link_mapillary,
            R.string.link_mapillary_description
        ),

        Link(
            "kartaview",
            "https://kartaview.org/map/",
            "KartaView",
            LinkCategory.INTRO,
            R.drawable.ic_link_kartaview,
            R.string.link_openstreetcam_description
        ),

        /* --------------------------------------- Editors ---------------------------------------*/
        Link(
            "pic4review",
            "https://pic4review.pavie.info",
            "Pic4Review",
            LinkCategory.EDITORS,
            R.drawable.ic_link_pic4review,
            R.string.link_pic4review_description
        ),
        Link(
            "ideditor",
            "http://ideditor.com",
            "iD",
            LinkCategory.EDITORS,
            R.drawable.ic_link_ideditor,
            R.string.link_ideditor_description
        ),
        Link(
            "vespucci",
            "https://vespucci.io",
            "Vespucci",
            LinkCategory.EDITORS,
            R.drawable.ic_link_vespucci,
            R.string.link_vespucci_description
        ),
        Link(
            "josm",
            "https://josm.openstreetmap.de",
            "JOSM",
            LinkCategory.EDITORS,
            R.drawable.ic_link_josm,
            R.string.link_josm_description
        ),

        /* ---------------------------------------- Maps -----------------------------------------*/

        Link(
            "openstreetbrowser",
            "https://openstreetbrowser.org",
            "OpenStreetBrowser",
            LinkCategory.MAPS,
            R.drawable.ic_link_openstreetbrowser,
            R.string.link_openstreetbrowser_description
        ),
        Link(
            "qwant_maps",
            "https://www.qwant.com/maps/",
            "Qwant Maps",
            LinkCategory.MAPS,
            R.drawable.ic_link_qwant,
            R.string.link_qwant_maps_description
        ),
        Link(
            "organic_maps",
            "https://organicmaps.app/",
            "Organic Maps",
            LinkCategory.MAPS,
            R.drawable.ic_link_organic_maps,
            R.string.link_organic_maps_description
        ),
        Link(
            "cyclosm",
            "https://www.cyclosm.org",
            "CyclOSM",
            LinkCategory.MAPS,
            R.drawable.ic_link_cyclosm,
            R.string.link_cyclosm_description
        ),
        Link(
            "öpnvkarte",
            "https://öpnvkarte.de",
            "ÖPNVKarte",
            LinkCategory.MAPS,
            R.drawable.ic_link_opnvkarte,
            R.string.link_opnvkarte_description
        ),
        Link(
            "wheelmap",
            "https://wheelmap.org",
            "wheelmap.org",
            LinkCategory.MAPS,
            R.drawable.ic_link_wheelmap,
            R.string.link_wheelmap_description
        ),
        Link(
            "openvegemap",
            "https://openvegemap.netlib.re",
            "OpenVegeMap",
            LinkCategory.MAPS,
            R.drawable.ic_link_openvegemap,
            R.string.link_openvegemap_description
        ),
        Link(
            "osm_buildings",
            "https://osmbuildings.org",
            "OSM Buildings",
            LinkCategory.MAPS,
            R.drawable.ic_link_osm_buildings,
            R.string.link_osm_buildings_description
        ),
        Link(
            "mapy_tactile",
            "https://hapticke.mapy.cz/?x=14.4343228&y=50.0652972&z=19&lang=en",
            "Mapy.cz Tactile",
            LinkCategory.MAPS,
            R.drawable.ic_link_mapy_tactile,
            R.string.link_mapy_tactile_description
        ),
        Link( // TODO no achievement yet
            "openinframap",
            "https://openinframap.org",
            "Open Infrastructure Map",
            LinkCategory.MAPS,
            null,
            R.string.link_openinframap_description
        ),
        Link(
            "indoorequal",
            "https://indoorequal.org",
            "indoor=",
            LinkCategory.MAPS,
            R.drawable.ic_link_indoorequal,
            R.string.link_indoorequal_description
        ),

        /* -------------------------------------- Showcase ---------------------------------------*/
        Link(
            "brouter",
            "https://brouter.de/brouter-web/",
            "BRouter",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_brouter,
            R.string.link_brouter_description
        ),

        Link(
            "openrouteservice-wheelchair",
            "https://classic-maps.openrouteservice.org/directions?b=3",
            "Openrouteservice (Wheelchair)",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_heigit,
            R.string.link_openrouteservice_wheelchair_description
        ),
        Link(
            "touch_mapper",
            "https://touch-mapper.org",
            "Touch Mapper",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_touch_mapper,
            R.string.link_touch_mapper_description
        ),
        Link(
            "nominatim",
            "https://nominatim.openstreetmap.org",
            "Nominatim",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_openstreetmap,
            R.string.link_nominatim_description
        ),
        Link(
            "photon",
            "https://photon.komoot.io",
            "Photon",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_photon,
            R.string.link_photon_description
        ),
        Link(
            "osrm",
            "https://map.project-osrm.org",
            "OSRM",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_osrm,
            R.string.link_osrm_description
        ),
        Link(
            "openrouteservice",
            "https://maps.openrouteservice.org",
            "Openrouteservice",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_heigit,
            R.string.link_openrouteservice_description
        ),
        Link(
            "graphhopper",
            "https://graphhopper.com/maps/",
            "GraphHopper",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_graphhopper,
            R.string.link_graphhopper_description
        ),


        /* -------------------------------------- Goodies ----------------------------------------*/
        Link(
            "umap",
            "https://umap.openstreetmap.fr",
            "uMap",
            LinkCategory.GOODIES,
            R.drawable.ic_link_umap,
            R.string.link_umap_description
        ),
        Link(
            "city_roads",
            "https://anvaka.github.io/city-roads",
            "city roads",
            LinkCategory.GOODIES,
            R.drawable.ic_link_city_roads,
            R.string.link_city_roads_description
        ),
        Link(
            "myosmatic",
            "https://print.get-map.org",
            "MyOSMatic",
            LinkCategory.GOODIES,
            R.drawable.ic_link_myosmatic,
            R.string.link_myosmatic_description
        ),
        Link(
            "show_me_the_way",
            "https://osmlab.github.io/show-me-the-way",
            "show-me-the-way",
            LinkCategory.GOODIES,
            R.drawable.ic_link_osmlab,
            R.string.link_show_me_the_way_description
        ),
        Link(
            "osm-haiku",
            "https://satellitestud.io/osm-haiku/app",
            "OpenStreetMap Haiku",
            LinkCategory.GOODIES,
            R.drawable.ic_link_haiku,
            R.string.link_osm_haiku_description
        ),
        Link(
            "openorienteeringmap",
            "https://oomap.co.uk/global/",
            "OpenOrienteeringMap",
            LinkCategory.GOODIES,
            null,
            R.string.link_openorienteeringmap_description
        ),
        Link(
            "figuregrounder",
            "https://hanshack.com/figuregrounder/",
            "Figuregrounder",
            LinkCategory.GOODIES,
            R.drawable.ic_link_figuregrounder,
            R.string.link_figuregrounder_description
        )
    )

    private val linksById = links.associateBy { it.id }

    private val achievements = listOf(

        Achievement(
            "first_edit",
            R.drawable.ic_achievement_first_edit,
            R.string.achievement_first_edit_title,
            R.string.achievement_first_edit_description,
            TotalSolvedQuests,
            { 1 },
            mapOf(),
            1
        ),

        Achievement(
            "surveyor",
            R.drawable.ic_achievement_surveyor,
            R.string.achievement_surveyor_title,
            R.string.achievement_surveyor_solved_X,
            TotalSolvedQuests,
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                /* Achievements rewarded for general activity should first cover introduction to OSM
                   and then most of all goodies and general (OSM) showcases */
                1 to links("wiki"), // most important link
                2 to links("welcomemat"),

                4 to links("show_me_the_way"),

                6 to links("myosmatic"),

                8 to links("osm-haiku"),

                10 to links("umap")
            )
        ),

        Achievement(
            "regular",
            R.drawable.ic_achievement_regular,
            R.string.achievement_regular_title,
            R.string.achievement_regular_description,
            DaysActive,
            // levels: 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, ...
            { 4 },
            mapOf(
                /* Achievements rewarded for regular activity should focus mostly on introducing
                   user to the community and other editors. Introducing editors should be delayed
                   because users should not get sidetracked too early - best first show community
                   intro links */
                1 to links("weeklyosm"), // newspaper first
                2 to links("pic4review"), // mentioning it early because it is very easy to use
                3 to links("neis-one"), // who-is-around-me, leaderboards etc fits into "community intro"
                4 to links("ideditor"),
                5 to links("learnosm"), // learnosm mostly concerns itself with tutorials about how to use editors
                6 to links("disaster.ninja"),
                7 to links("vespucci", "josm") // together because both are full-featured-editors for each their platform
            )
        ),

        Achievement(
            "rare",
            R.drawable.ic_achievement_rare,
            R.string.achievement_rare_title,
            R.string.achievement_rare_solved_X,
            SolvedQuestsOfTypes(listOf(
                AddSummitRegister::class.simpleName!!, // 1
                AddWheelchairAccessToiletsPart::class.simpleName!!, // 38
                AddWheelchairAccessOutside::class.simpleName!!, // 154
                AddFerryAccessPedestrian::class.simpleName!!, // 66
                AddFerryAccessMotorVehicle::class.simpleName!!, // 103
                AddInformationToTourism::class.simpleName!!, // 137
                AddBoardType::class.simpleName!!, // 188
            )),
            // levels: 3, 9, 18, 30, 45, 63, ...
            { lvl -> (lvl + 1)*3 },
            mapOf()
        ),

        Achievement(
            "car",
            R.drawable.ic_achievement_car,
            R.string.achievement_car_title,
            R.string.achievement_car_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddRoadName::class.simpleName!!,
                    AddOneway::class.simpleName!!,
                    MarkCompletedHighwayConstruction::class.simpleName!!,
                    AddTracktype::class.simpleName!!,
                    AddRoadSurface::class.simpleName!!,
                    AddMaxSpeed::class.simpleName!!,
                    AddMaxHeight::class.simpleName!!,
                    AddMaxWeight::class.simpleName!!,
                    AddRailwayCrossingBarrier::class.simpleName!!,
                    AddParkingAccess::class.simpleName!!,
                    AddParkingFee::class.simpleName!!,
                    AddParkingType::class.simpleName!!,
                    AddMotorcycleParkingCapacity::class.simpleName!!,
                    AddMotorcycleParkingCover::class.simpleName!!,
                    AddFerryAccessMotorVehicle::class.simpleName!!,
                    AddCarWashType::class.simpleName!!,
                    AddChargingStationOperator::class.simpleName!!,
                    AddChargingStationCapacity::class.simpleName!!,
                    AddLanes::class.simpleName!!,
                    AddBarrierType::class.simpleName!!,
                    AddBollardType::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                2 to links("city_roads"), // put it here because it shows roads


                5 to links("osrm"), // routing engines are not that interesting for end users
                6 to links("openrouteservice"),
                7 to links("graphhopper"),
                12 to links("kartaview", "mapillary") // useful to OSM, but not directly OSM and interesting only to extreme enthusiasts
            )
        ),

        Achievement(
            "veg",
            R.drawable.ic_achievement_veg,
            R.string.achievement_veg_title,
            R.string.achievement_veg_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddVegetarian::class.simpleName!!,
                    AddVegan::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("openvegemap")
            )
        ),

        Achievement(
            "pedestrian",
            R.drawable.ic_achievement_pedestrian,
            R.string.achievement_pedestrian_title,
            R.string.achievement_pedestrian_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddRoadName::class.simpleName!!,
                    AddWayLit::class.simpleName!!,
                    AddHandrail::class.simpleName!!,
                    AddStepsIncline::class.simpleName!!,
                    AddStepCount::class.simpleName!!,
                    AddStepsRamp::class.simpleName!!,
                    AddFootwayPartSurface::class.simpleName!!,
                    AddBenchBackrest::class.simpleName!!,
                    AddTrafficSignalsButton::class.simpleName!!,
                    AddFerryAccessPedestrian::class.simpleName!!,
                    AddPathSurface::class.simpleName!!,
                    AddCrossingType::class.simpleName!!,
                    AddProhibitedForPedestrians::class.simpleName!!,
                    AddSidewalk::class.simpleName!!,
                    AddBusStopName::class.simpleName!!,
                    AddBusStopRef::class.simpleName!!,
                    AddBusStopShelter::class.simpleName!!,
                    AddBenchStatusOnBusStop::class.simpleName!!,
                    AddBusStopLit::class.simpleName!!,
                    AddCrossingIsland::class.simpleName!!,
                    AddBarrierType::class.simpleName!!,
                    AddCrossing::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("öpnvkarte")
            )
        ),

        Achievement(
            "building",
            R.drawable.ic_achievement_building,
            R.string.achievement_building_title,
            R.string.achievement_building_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddIsBuildingUnderground::class.simpleName!!,
                    AddBuildingType::class.simpleName!!,
                    AddBuildingLevels::class.simpleName!!,
                    MarkCompletedBuildingConstruction::class.simpleName!!,
                    AddPowerPolesMaterial::class.simpleName!!,
                    AddBridgeStructure::class.simpleName!!,
                    AddRoofShape::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("osm_buildings"),
                2 to links("figuregrounder")
            )
        ),

        Achievement(
            "postman",
            R.drawable.ic_achievement_postman,
            R.string.achievement_postman_title,
            R.string.achievement_postman_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddHousenumber::class.simpleName!!,
                    AddRoadName::class.simpleName!!,
                    AddAddressStreet::class.simpleName!!,
                    AddPostboxRef::class.simpleName!!,
                    AddPostboxCollectionTimes::class.simpleName!!,
                    AddPostboxRoyalCypher::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                // Nominatim should not appear too early because it is a bit technical, the functionality
                // itself is often integrated into other services
                3 to links("nominatim"),
                5 to links("photon")
            )
        ),

        Achievement(
            "blind",
            R.drawable.ic_achievement_blind,
            R.string.achievement_blind_title,
            R.string.achievement_blind_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddTactilePavingCrosswalk::class.simpleName!!,
                    AddTrafficSignalsSound::class.simpleName!!,
                    AddTrafficSignalsVibration::class.simpleName!!,
                    AddTactilePavingBusStop::class.simpleName!!,
                    AddCrossingIsland::class.simpleName!!,
                    AddKerbHeight::class.simpleName!!,
                    AddTactilePavingKerb::class.simpleName!!,
                    AddBarrierType::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("touch_mapper"),
                2 to links("mapy_tactile")
            )
        ),

        Achievement(
            "wheelchair",
            R.drawable.ic_achievement_wheelchair,
            R.string.achievement_wheelchair_title,
            R.string.achievement_wheelchair_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddWheelchairAccessBusiness::class.simpleName!!,
                    AddWheelchairAccessOutside::class.simpleName!!,
                    AddWheelchairAccessPublicTransport::class.simpleName!!,
                    AddWheelchairAccessToilets::class.simpleName!!,
                    AddWheelchairAccessToiletsPart::class.simpleName!!,
                    AddFootwayPartSurface::class.simpleName!!,
                    AddPathSurface::class.simpleName!!,
                    AddStepsRamp::class.simpleName!!,
                    AddHandrail::class.simpleName!!,
                    AddKerbHeight::class.simpleName!!,
                    AddBarrierType::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("wheelmap"),
                2 to links("openrouteservice-wheelchair")
            )
        ),

        Achievement(
            "bicyclist",
            R.drawable.ic_achievement_bicyclist,
            R.string.achievement_bicyclist_title,
            R.string.achievement_bicyclist_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    AddCycleway::class.simpleName!!,
                    AddCyclewayPartSurface::class.simpleName!!,
                    AddBikeParkingCapacity::class.simpleName!!,
                    AddBikeParkingCover::class.simpleName!!,
                    AddBikeParkingType::class.simpleName!!,
                    AddBikeParkingAccess::class.simpleName!!,
                    AddBikeParkingFee::class.simpleName!!,
                    AddCyclewaySegregation::class.simpleName!!,
                    AddPathSurface::class.simpleName!!,
                    AddStepsRamp::class.simpleName!!,
                    AddKerbHeight::class.simpleName!!,
                    AddBarrierType::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("cyclosm"),
                2 to links("brouter")
            )
        ),

        Achievement(
            "citizen",
            R.drawable.ic_achievement_citizen,
            R.string.achievement_citizen_title,
            R.string.achievement_citizen_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    CheckExistence::class.simpleName!!,
                    CheckShopType::class.simpleName!!,
                    SpecifyShopType::class.simpleName!!,
                    AddSelfServiceLaundry::class.simpleName!!,
                    AddToiletAvailability::class.simpleName!!,
                    AddToiletsFee::class.simpleName!!,
                    AddPlaceName::class.simpleName!!,
                    DetermineRecyclingGlass::class.simpleName!!,
                    AddRecyclingContainerMaterials::class.simpleName!!,
                    AddClothingBinOperator::class.simpleName!!,
                    AddBinStatusOnBusStop::class.simpleName!!,
                    AddBabyChangingTable::class.simpleName!!,
                    AddOpeningHours::class.simpleName!!,
                    AddAtmOperator::class.simpleName!!,
                    AddPlaygroundAccess::class.simpleName!!,
                    AddReligionToPlaceOfWorship::class.simpleName!!,
                    AddRecyclingType::class.simpleName!!,
                    AddAcceptsCash::class.simpleName!!,
                    AddVegetarian::class.simpleName!!,
                    AddVegan::class.simpleName!!,
                    AddKosher::class.simpleName!!,
                    AddPoliceType::class.simpleName!!,
                    // tourist related
                    AddInformationToTourism::class.simpleName!!,
                    AddBoardType::class.simpleName!!,
                    AddInternetAccess::class.simpleName!!,
                    AddGeneralFee::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("openstreetbrowser"),
                2 to links("qwant_maps"),
                3 to links("organic_maps"),
                4 to links("indoorequal")
            )
        ),

        Achievement(
            "outdoors",
            R.drawable.ic_achievement_outdoors,
            R.string.achievement_outdoors_title,
            R.string.achievement_outdoors_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    CheckExistence::class.simpleName!!,
                    AddSport::class.simpleName!!,
                    AddPitchSurface::class.simpleName!!,
                    AddPitchLit::class.simpleName!!,
                    AddSummitRegister::class.simpleName!!,
                    AddReligionToWaysideShrine::class.simpleName!!,
                    AddDrinkingWater::class.simpleName!!,
                    // from pedestrian
                    AddPathSurface::class.simpleName!!,
                    AddCyclewaySegregation::class.simpleName!!,
                    AddCyclewayPartSurface::class.simpleName!!,
                    AddFootwayPartSurface::class.simpleName!!,
                    AddBenchBackrest::class.simpleName!!,
                    AddBarrierType::class.simpleName!!,
                    AddStileType::class.simpleName!!,
                    // information boards
                    AddInformationToTourism::class.simpleName!!,
                    AddBoardType::class.simpleName!!,
                )
            ),
            // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
            { lvl -> (lvl + 1)*10 },
            mapOf(
                1 to links("openorienteeringmap")
            )
        )

    )

    private fun links(vararg linksKeys: String = emptyArray()): List<Link> =
        linksKeys.map { linksById.getValue(it) }

    // Quests not mentioned in any achievements:

    // maybe "emergency"
    // AddFireHydrantType AddIsDefibrillatorIndoor
    //
}

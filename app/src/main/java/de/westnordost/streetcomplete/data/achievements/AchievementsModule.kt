package de.westnordost.streetcomplete.data.achievements

import dagger.Module
import de.westnordost.streetcomplete.R

@Module
object AchievementsModule {

    val links = mapOf(

        /* ---------------------------------------- Intro ----------------------------------------*/
        "wiki" to Link(
            "https://wiki.openstreetmap.org",
            "OpenStreetMap Wiki",
            LinkCategory.INTRO,
            R.drawable.ic_link_wiki,
            R.string.link_wiki_description
        ),
        "learnosm" to Link(
            "https://learnosm.org/en/beginner/",
            "learnOSM",
            LinkCategory.INTRO,
            R.drawable.ic_link_learnosm,
            R.string.link_learnosm_description
        ),

        /* --------------------------------------- Editors ---------------------------------------*/
        "pic4review" to Link(
            "https://pic4review.pavie.info",
            "Pic4Review",
            LinkCategory.EDITORS,
            R.drawable.ic_link_pic4review,
            R.string.link_pic4review_description
        ),
        "ideditor" to Link(
            "http://ideditor.com",
            "iD",
            LinkCategory.EDITORS,
            R.drawable.ic_link_ideditor,
            R.string.link_ideditor_description
        ),
        "vespucci" to Link(
            "https://vespucci.io",
            "Vespucci",
            LinkCategory.EDITORS,
            R.drawable.ic_link_vespucci,
            R.string.link_vespucci_description
        ),
        "josm" to Link(
            "https://josm.openstreetmap.de",
            "JOSM",
            LinkCategory.EDITORS,
            R.drawable.ic_link_josm,
            R.string.link_josm_description
        ),

        /* -------------------------------------- Showcase ---------------------------------------*/
        "cyclosm" to Link(
            "https://www.cyclosm.org",
            "CyclOSM",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_cyclosm,
            R.string.link_cyclosm_description
        ),
        "brouter" to Link(
            "https://brouter.de/brouter-web/",
            "BRouter",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_brouter,
            R.string.link_brouter_description
        ),

        "öpnvkarte" to Link(
            "https://öpnvkarte.de",
            "ÖPNVKarte",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_opnvkarte,
            R.string.link_opnvkarte_description
        ),

        "wheelmap" to Link(
            "https://wheelmap.org",
            "wheelmap.org",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_wheelmap,
            R.string.link_wheelmap_description
        ),
        "openrouteservice-wheelchair" to Link(
            "https://maps.openrouteservice.org/directions?b=3",
            "Openrouteservice (Wheelchair)",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_heigit,
            R.string.link_openrouteservice_wheelchair_description
        ),

        "openvegemap" to Link(
            "https://openvegemap.netlib.re",
            "OpenVegeMap",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_openvegemap,
            R.string.link_openvegemap_description
        ),

        "osm_buildings" to Link(
            "https://osmbuildings.org",
            "OSM Buildings",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_osm_buildings,
            R.string.link_osm_buildings_description
        ),

        "touch_mapper" to Link(
            "https://touch-mapper.org",
            "Touch Mapper",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_touch_mapper,
            R.string.link_touch_mapper_description
        ),
        "mapy_tactile" to Link(
            "https://hapticke.mapy.cz/?x=14.4343228&y=50.0652972&z=19&lang=en",
            "Mapy.cz Tactile",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_mapy_tactile,
            R.string.link_mapy_tactile_description
        ),

        "nominatim" to Link(
            "https://nominatim.openstreetmap.org",
            "Nominatim",
            LinkCategory.SHOWCASE,
            R.drawable.ic_link_openstreetmap,
            R.string.link_nominatim_description
        ),

        /* -------------------------------------- Goodies ----------------------------------------*/
        "umap" to Link(
            "https://umap.openstreetmap.fr",
            "uMap",
            LinkCategory.GOODIES,
            R.drawable.ic_link_umap,
            R.string.link_umap_description
        ),
        "city_roads" to Link(
            "https://anvaka.github.io/city-roads",
            "city roads",
            LinkCategory.GOODIES,
            R.drawable.ic_link_avatar_anvaka_on_github,
            R.string.link_city_roads_description
        ),
        "myosmatic" to Link(
            "https://print.get-map.org",
            "MyOSMatic",
            LinkCategory.GOODIES,
            R.drawable.ic_link_myosmatic,
            R.string.link_myosmatic_description
        ),
        "show_me_the_way" to Link(
            "https://osmlab.github.io/show-me-the-way",
            "show-me-the-way",
            LinkCategory.GOODIES,
            R.drawable.ic_link_osmlab,
            R.string.link_show_me_the_way_description
        )
    )

// TODO intro achievements / based on time...

    val achievements = mapOf(

        "first_edit" to Achievement(
            R.drawable.ic_achievement_first_edit,
            R.string.achievement_first_edit_title,
            R.string.achievement_first_edit_description,
            TotalSolvedQuests,
            listOf(
                level(1)
            )
        ),

        "surveyor" to Achievement(
            R.drawable.ic_achievement_surveyor,
            R.string.achievement_surveyor_title,
            R.string.achievement_surveyor_solved_X,
            TotalSolvedQuests,
            listOf(
                level(10, listOf("wiki")),
                level(20, listOf("umap")),
                level(30, listOf("learnosm")),
                level(50, listOf("show_me_the_way")),
                level(80, listOf())
            )
        ),

        "regular" to Achievement(
            R.drawable.ic_achievement_regular,
            R.string.achievement_regular_title,
            R.string.achievement_regular_description,
            DaysActive,
            listOf(
                level(3, listOf("pic4review")),
                level(6, listOf("ideditor")),
                level(12, listOf("vespucci", "josm"))
            )
        ),

        "car" to Achievement(
            R.drawable.ic_achievement_car,
            R.string.achievement_car_title,
            R.string.achievement_car_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddRoadName",
                    "AddOneway",
                    "MarkCompletedHighwayConstruction",
                    "AddTracktype",
                    "AddRoadSurface",
                    "AddMaxSpeed",
                    "AddMaxHeight",
                    "AddMaxWeight",
                    "AddRailwayCrossingBarrier",
                    "AddParkingAccess",
                    "AddParkingFee",
                    "AddParkingType",
                    "AddMotorcycleParkingCapacity",
                    "AddMotorcycleParkingCover",
                    "AddFerryAccessMotorVehicle",
                    "AddCarWashType"
                )
            ),
            listOf(
                level(100, listOf("myosmatic")),
                level(200, listOf("city_roads"))
            )
        ),

        "veg" to Achievement(
            R.drawable.ic_achievement_veg,
            R.string.achievement_veg_title,
            R.string.achievement_veg_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddVegetarian",
                    "AddVegan"
                )
            ),
            listOf(
                level(100, listOf("openvegemap")),
                level(200)
            )
        ),

        "pedestrian" to Achievement(
            R.drawable.ic_achievement_pedestrian,
            R.string.achievement_pedestrian_title,
            R.string.achievement_pedestrian_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddRoadName",
                    "AddWayLit",
                    "AddHandrail",
                    "AddFootwayPartSurface",
                    "AddBenchBackrest",
                    "AddTrafficSignalsButton",
                    "AddFerryAccessPedestrian",
                    "AddPathSurface",
                    "AddCrossingType",
                    "AddProhibitedForPedestrians",
                    "AddSidewalk",
                    "AddBusStopName",
                    "AddBusStopShelter"
                )
            ),
            listOf(
                // TODO or own achievement for public transport?
                level(100, listOf("öpnvkarte")),
                level(200)
            )
        ),

        "building" to Achievement(
            R.drawable.ic_achievement_building,
            R.string.achievement_building_title,
            R.string.achievement_building_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddIsBuildingUnderground",
                    "AddBuildingType",
                    "AddBuildingLevels",
                    "MarkCompletedBuildingConstruction",
                    "AddPowerPolesMaterial",
                    "AddBridgeStructure",
                    "AddRoofShape"
                )
            ),
            listOf(
                level(100, listOf("osm_buildings")),
                level(200)
            )
        ),

        "postman" to Achievement(
            R.drawable.ic_achievement_postman,
            R.string.achievement_postman_title,
            R.string.achievement_postman_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddHousenumber",
                    "AddRoadName",
                    "AddPostboxRef",
                    "AddPostboxCollectionTimes"
                )
            ),
            listOf(
                level(30),
                // Nominatim should not appear too early because it is a bit technical, the functionality
                // itself is often integrated into other services
                level(100, listOf("nominatim"))
            )
        ),

        "blind" to Achievement(
            R.drawable.ic_achievement_blind,
            R.string.achievement_blind_title,
            R.string.achievement_blind_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddTactilePavingCrosswalk",
                    "AddTrafficSignalsSound",
                    "AddTactilePavingBusStop"
                )
            ),
            listOf(
                level(30),
                level(100, listOf("touch_mapper", "mapy_tactile"))
            )
        ),

        "wheelchair" to Achievement(
            R.drawable.ic_achievement_wheelchair,
            R.string.achievement_wheelchair_title,
            R.string.achievement_wheelchair_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddWheelchairAccessBusiness",
                    "AddWheelchairAccessOutside",
                    "AddWheelchairAccessPublicTransport",
                    "AddWheelchairAccessToilets",
                    "AddWheelchairAccessToiletsPart",
                    "AddFootwayPartSurface",
                    "AddPathSurface"
                )
            ),
            listOf(
                level(30, listOf("wheelmap")),
                level(50, listOf("openrouteservice-wheelchair"))
            )
        ),

        "bicyclist" to Achievement(
            R.drawable.ic_achievement_bicyclist,
            R.string.achievement_bicyclist_title,
            R.string.achievement_bicyclist_solved_X,
            SolvedQuestsOfTypes(
                listOf(
                    "AddCycleway",
                    "AddCyclewayPartSurface",
                    "AddBikeParkingCapacity",
                    "AddBikeParkingCover",
                    "AddBikeParkingType",
                    "AddCyclewaySegregation",
                    "AddPathSurface"
                )
            ),
            listOf(
                level(30, listOf("cyclosm")),
                level(50, listOf("brouter")),
            )
        )
    )

    private fun level(threshold: Int, linksKeys: List<String> = emptyList()) =
        AchievementLevel(threshold, linksKeys.map { links.getValue(it) })

    // Quests not mentioned in any achievements:
    // AddFireHydrantType AddGeneralFee AddSelfServiceLaundry AddReligionToWaysideShrine AddToiletsFee
    // AddBabyChangingTable AddPlaceName AddReligionToPlaceOfWorship AddRecyclingType
    // AddRecyclingContainerMaterials AddSport AddOpeningHours AddInternetAccess AddPlaygroundAccess
    // AddToiletAvailability DetermineRecyclingGlass

}

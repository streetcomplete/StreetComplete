package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.Item

enum class BuildingType(val item:Item<String>) {
    HOUSE         (Item("house",      R.drawable.ic_building_house,      R.string.quest_buildingType_house, R.string.quest_buildingType_house_description)),
    APARTMENTS    (Item("apartments", R.drawable.ic_building_apartments, R.string.quest_buildingType_apartments, R.string.quest_buildingType_apartments_description)),
    DETACHED      (Item("detached",   R.drawable.ic_building_detached,   R.string.quest_buildingType_detached, R.string.quest_buildingType_detached_description)),
    SEMI_DETACHED (Item("semidetached_house", R.drawable.ic_building_semi_detached, R.string.quest_buildingType_semi_detached, R.string.quest_buildingType_semi_detached_description)),
    TERRACE       (Item("terrace",    R.drawable.ic_building_terrace,    R.string.quest_buildingType_terrace, R.string.quest_buildingType_terrace_description)),
    HOTEL         (Item("hotel",      R.drawable.ic_building_hotel,      R.string.quest_buildingType_hotel)),
    DORMITORY     (Item("dormitory",  R.drawable.ic_building_dormitory,  R.string.quest_buildingType_dormitory)),
    HOUSEBOAT     (Item("houseboat",  R.drawable.ic_building_houseboat,  R.string.quest_buildingType_houseboat)),
    BUNGALOW      (Item("bungalow",   R.drawable.ic_building_bungalow,   R.string.quest_buildingType_bungalow, R.string.quest_buildingType_bungalow_description)),
    STATIC_CARAVAN(Item("static_caravan", R.drawable.ic_building_static_caravan, R.string.quest_buildingType_static_caravan)),
    HUT           (Item("hut",        R.drawable.ic_building_hut,        R.string.quest_buildingType_hut, R.string.quest_buildingType_hut_description)),

    INDUSTRIAL    (Item("industrial", R.drawable.ic_building_industrial, R.string.quest_buildingType_industrial, R.string.quest_buildingType_industrial_description)),
    RETAIL        (Item("retail",     R.drawable.ic_building_retail,     R.string.quest_buildingType_retail, R.string.quest_buildingType_retail_description)),
    OFFICE        (Item("office",     R.drawable.ic_building_office,     R.string.quest_buildingType_office)),
    WAREHOUSE     (Item("warehouse",  R.drawable.ic_building_warehouse,  R.string.quest_buildingType_warehouse)),
    KIOSK         (Item("kiosk",      R.drawable.ic_building_kiosk,      R.string.quest_buildingType_kiosk)),
    STORAGE_TANK  (Item("man_made=storage_tank", R.drawable.ic_building_storage_tank, R.string.quest_buildingType_storage_tank)),

    KINDERGARTEN  (Item("kindergarten", R.drawable.ic_building_kindergarten, R.string.quest_buildingType_kindergarten)),
    SCHOOL        (Item("school",     R.drawable.ic_building_school,     R.string.quest_buildingType_school)),
    COLLEGE       (Item("college",    R.drawable.ic_building_college,    R.string.quest_buildingType_college)),
    SPORTS_CENTRE (Item("sports_centre", R.drawable.ic_sport_volleyball, R.string.quest_buildingType_sports_centre)),
    HOSPITAL      (Item("hospital",   R.drawable.ic_building_hospital,   R.string.quest_buildingType_hospital)),
    STADIUM       (Item("stadium",    R.drawable.ic_sport_volleyball,    R.string.quest_buildingType_stadium)),
    TRAIN_STATION (Item("train_station", R.drawable.ic_building_train_station, R.string.quest_buildingType_train_station)),
    TRANSPORTATION(Item("transportation", R.drawable.ic_building_transportation, R.string.quest_buildingType_transportation)),
    UNIVERSITY    (Item("university", R.drawable.ic_building_university, R.string.quest_buildingType_university)),
    GOVERNMENT    (Item("government", R.drawable.ic_building_civic,      R.string.quest_buildingType_government)),

    CHURCH        (Item("church",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_church)),
    CHAPEL        (Item("chapel",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_chapel)),
    CATHEDRAL     (Item("cathedral",  R.drawable.ic_religion_christian,  R.string.quest_buildingType_cathedral)),
    MOSQUE        (Item("mosque",     R.drawable.ic_religion_muslim,     R.string.quest_buildingType_mosque)),
    TEMPLE        (Item("temple",     R.drawable.ic_building_temple,     R.string.quest_buildingType_temple)),
    PAGODA        (Item("pagoda",     R.drawable.ic_building_temple,     R.string.quest_buildingType_pagoda)),
    SYNAGOGUE     (Item("synagogue",  R.drawable.ic_religion_jewish,     R.string.quest_buildingType_synagogue)),
    SHRINE        (Item("shrine",     R.drawable.ic_building_temple,     R.string.quest_buildingType_shrine)),

    CARPORT       (Item("carport",    R.drawable.ic_building_carport,    R.string.quest_buildingType_carport, R.string.quest_buildingType_carport_description)),
    GARAGE        (Item("garage",     R.drawable.ic_building_garage,     R.string.quest_buildingType_garage)),
    GARAGES       (Item("garages",    R.drawable.ic_building_garages,    R.string.quest_buildingType_garages, R.string.quest_buildingType_garages_description)),
    PARKING       (Item("parking",    R.drawable.ic_building_parking,    R.string.quest_buildingType_parking)),

    FARM          (Item("farm",       R.drawable.ic_building_farm_house, R.string.quest_buildingType_farmhouse, R.string.quest_buildingType_farmhouse_description)),
    FARM_AUXILIARY(Item("farm_auxiliary", R.drawable.ic_building_barn,   R.string.quest_buildingType_farm_auxiliary, R.string.quest_buildingType_farm_auxiliary_description)),
    GREENHOUSE    (Item("greenhouse", R.drawable.ic_building_greenhouse, R.string.quest_buildingType_greenhouse)),

    SHED          (Item("shed",       R.drawable.ic_building_shed,       R.string.quest_buildingType_shed)),
    ROOF          (Item("roof",       R.drawable.ic_building_roof,       R.string.quest_buildingType_roof)),
    TOILETS       (Item("toilets",    R.drawable.ic_building_toilets,    R.string.quest_buildingType_toilets)),
    SERVICE       (Item("service",    R.drawable.ic_building_service,    R.string.quest_buildingType_service, R.string.quest_buildingType_service_description)),
    HANGAR        (Item("hangar",     R.drawable.ic_building_hangar,     R.string.quest_buildingType_hangar, R.string.quest_buildingType_hangar_description)),
    BUNKER        (Item("bunker",     R.drawable.ic_building_bunker,     R.string.quest_buildingType_bunker)),

    RESIDENTIAL   (Item("residential", R.drawable.ic_building_apartments, R.string.quest_buildingType_residential, R.string.quest_buildingType_residential_description,
        listOf( DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, HOUSE, FARM, HUT, BUNGALOW, HOUSEBOAT, STATIC_CARAVAN, DORMITORY).toItems())),

    COMMERCIAL    (Item("commercial", R.drawable.ic_building_office, R.string.quest_buildingType_commercial, R.string.quest_buildingType_commercial_generic_description,
        listOf( OFFICE, INDUSTRIAL, RETAIL, WAREHOUSE, KIOSK, HOTEL, STORAGE_TANK ).toItems())),

    CIVIC         (Item("civic", R.drawable.ic_building_civic, R.string.quest_buildingType_civic, R.string.quest_buildingType_civic_description,
        listOf( SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, TRAIN_STATION, TRANSPORTATION, COLLEGE, GOVERNMENT, STADIUM ).toItems())),

    RELIGIOUS     (Item("religious", R.drawable.ic_building_temple, R.string.quest_buildingType_religious, null,
        listOf( CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE ).toItems())),

    FOR_CARS      (Item(null, R.drawable.ic_building_car, R.string.quest_buildingType_cars, null,
        listOf( GARAGE, GARAGES, CARPORT, PARKING ).toItems())),

    FOR_FARMS     (Item(null, R.drawable.ic_building_farm, R.string.quest_buildingType_farm, null,
        listOf( FARM, FARM_AUXILIARY, GREENHOUSE, STORAGE_TANK ).toItems())),

    OTHER         (Item(null, R.drawable.ic_building_other, R.string.quest_buildingType_other, null,
        listOf( SHED, ROOF, SERVICE, HUT, TOILETS, HANGAR, BUNKER ).toItems()));

    companion object {
        fun getByTag(key: String, value: String): BuildingType? {
            var tag = if (key == "building") value else "$key=$value"
            // synonyms
            if (tag == "semi")        tag = "semidetached_house"
            else if (tag == "public") tag = "civic"

            return BuildingType.values().find { it.item.value == tag }
        }
    }
}

fun List<BuildingType>.toItems() = this.map { it.item }

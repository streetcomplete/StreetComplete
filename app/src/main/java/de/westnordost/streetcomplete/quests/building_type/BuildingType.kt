package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.GroupedItem

enum class BuildingType(
    override val value: String,
    override val drawableId: Int,
    override val titleId: Int,
    override val descriptionId: Int = 0)
    : GroupedItem {

    RESIDENTIAL   ("residential",R.drawable.ic_building_apartments, R.string.quest_buildingType_residential, R.string.quest_buildingType_residential_description),
    HOUSE         ("house",      R.drawable.ic_building_house,      R.string.quest_buildingType_house, R.string.quest_buildingType_house_description),
    APARTMENTS    ("apartments", R.drawable.ic_building_apartments, R.string.quest_buildingType_apartments, R.string.quest_buildingType_apartments_description),
    DETACHED      ("detached",   R.drawable.ic_building_detached,   R.string.quest_buildingType_detached, R.string.quest_buildingType_detached_description),
    SEMI_DETACHED ("semidetached_house", R.drawable.ic_building_semi_detached, R.string.quest_buildingType_semi_detached, R.string.quest_buildingType_semi_detached_description),
    TERRACE       ("terrace",    R.drawable.ic_building_terrace,    R.string.quest_buildingType_terrace, R.string.quest_buildingType_terrace_description),
    HOTEL         ("hotel",      R.drawable.ic_building_hotel,      R.string.quest_buildingType_hotel),
    DORMITORY     ("dormitory",  R.drawable.ic_building_dormitory,  R.string.quest_buildingType_dormitory),
    HOUSEBOAT     ("houseboat",  R.drawable.ic_building_houseboat,  R.string.quest_buildingType_houseboat),
    BUNGALOW      ("bungalow",   R.drawable.ic_building_bungalow,   R.string.quest_buildingType_bungalow, R.string.quest_buildingType_bungalow_description),
    STATIC_CARAVAN("static_caravan", R.drawable.ic_building_static_caravan, R.string.quest_buildingType_static_caravan),
    HUT           ("hut",        R.drawable.ic_building_hut,        R.string.quest_buildingType_hut, R.string.quest_buildingType_hut_description),

    COMMERCIAL    ("commercial", R.drawable.ic_building_office,     R.string.quest_buildingType_commercial, R.string.quest_buildingType_commercial_generic_description),
    INDUSTRIAL    ("industrial", R.drawable.ic_building_industrial, R.string.quest_buildingType_industrial, R.string.quest_buildingType_industrial_description),
    RETAIL        ("retail",     R.drawable.ic_building_retail,     R.string.quest_buildingType_retail, R.string.quest_buildingType_retail_description),
    OFFICE        ("office",     R.drawable.ic_building_office,     R.string.quest_buildingType_office),
    WAREHOUSE     ("warehouse",  R.drawable.ic_building_warehouse,  R.string.quest_buildingType_warehouse),
    KIOSK         ("kiosk",      R.drawable.ic_building_kiosk,      R.string.quest_buildingType_kiosk),
    STORAGE_TANK  ("man_made=storage_tank", R.drawable.ic_building_storage_tank, R.string.quest_buildingType_storage_tank),

    CIVIC         ("civic",      R.drawable.ic_building_civic,      R.string.quest_buildingType_civic, R.string.quest_buildingType_civic_description),
    KINDERGARTEN  ("kindergarten", R.drawable.ic_building_kindergarten, R.string.quest_buildingType_kindergarten),
    SCHOOL        ("school",     R.drawable.ic_building_school,     R.string.quest_buildingType_school),
    COLLEGE       ("college",    R.drawable.ic_building_college,    R.string.quest_buildingType_college),
    SPORTS_CENTRE ("sports_centre", R.drawable.ic_sport_volleyball, R.string.quest_buildingType_sports_centre),
    HOSPITAL      ("hospital",   R.drawable.ic_building_hospital,   R.string.quest_buildingType_hospital),
    STADIUM       ("stadium",    R.drawable.ic_sport_volleyball,    R.string.quest_buildingType_stadium),
    TRAIN_STATION ("train_station", R.drawable.ic_building_train_station, R.string.quest_buildingType_train_station),
    TRANSPORTATION("transportation", R.drawable.ic_building_transportation, R.string.quest_buildingType_transportation),
    UNIVERSITY    ("university", R.drawable.ic_building_university, R.string.quest_buildingType_university),
    GOVERNMENT    ("government", R.drawable.ic_building_civic,      R.string.quest_buildingType_government),

    RELIGIOUS     ("religious",  R.drawable.ic_building_temple,     R.string.quest_buildingType_religious),
    CHURCH        ("church",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_church),
    CHAPEL        ("chapel",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_chapel),
    CATHEDRAL     ("cathedral",  R.drawable.ic_religion_christian,  R.string.quest_buildingType_cathedral),
    MOSQUE        ("mosque",     R.drawable.ic_religion_muslim,     R.string.quest_buildingType_mosque),
    TEMPLE        ("temple",     R.drawable.ic_building_temple,     R.string.quest_buildingType_temple),
    PAGODA        ("pagoda",     R.drawable.ic_building_temple,     R.string.quest_buildingType_pagoda),
    SYNAGOGUE     ("synagogue",  R.drawable.ic_religion_jewish,     R.string.quest_buildingType_synagogue),
    SHRINE        ("shrine",     R.drawable.ic_building_temple,     R.string.quest_buildingType_shrine),

    CARPORT       ("carport",    R.drawable.ic_building_carport,    R.string.quest_buildingType_carport, R.string.quest_buildingType_carport_description),
    GARAGE        ("garage",     R.drawable.ic_building_garage,     R.string.quest_buildingType_garage),
    GARAGES       ("garages",    R.drawable.ic_building_garages,    R.string.quest_buildingType_garages),
    PARKING       ("parking",    R.drawable.ic_building_parking,    R.string.quest_buildingType_parking),

    FARM          ("farm",       R.drawable.ic_building_farm_house, R.string.quest_buildingType_farmhouse, R.string.quest_buildingType_farmhouse_description),
    FARM_AUXILIARY("farm_auxiliary", R.drawable.ic_building_barn,   R.string.quest_buildingType_farm_auxiliary, R.string.quest_buildingType_farm_auxiliary_description),
    GREENHOUSE    ("greenhouse", R.drawable.ic_building_greenhouse, R.string.quest_buildingType_greenhouse),

    SHED          ("shed",       R.drawable.ic_building_shed,       R.string.quest_buildingType_shed),
    ROOF          ("roof",       R.drawable.ic_building_roof,       R.string.quest_buildingType_roof),
    TOILETS       ("toilets",    R.drawable.ic_building_toilets,    R.string.quest_buildingType_toilets),
    SERVICE       ("service",    R.drawable.ic_building_service,    R.string.quest_buildingType_service, R.string.quest_buildingType_service_description),
    HANGAR        ("hangar",     R.drawable.ic_building_hangar,     R.string.quest_buildingType_hangar, R.string.quest_buildingType_hangar_description),
    BUNKER        ("bunker",     R.drawable.ic_building_bunker,     R.string.quest_buildingType_bunker);

    companion object {
        fun getByTag(key: String, value: String): BuildingType? {
            var tag = if (key == "building") value else "$key=$value"
            // synonyms
            if (tag == "semi")
                tag = "semidetached_house"
            else if (tag == "public") tag = "civic"

            return BuildingType.values().find { it.value == tag }
        }
    }
}
